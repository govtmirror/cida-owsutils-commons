package gov.usgs.cida.owsutils.commons.shapefile.utils;

import gov.usgs.cida.owsutils.commons.io.FileHelper;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.geotools.data.crs.ReprojectFeatureResults;
import org.geotools.feature.FeatureCollection;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author isuftin
 */
public class FeatureCollectionFromShpTest {

	public FeatureCollectionFromShpTest() {
	}

	private static final String tempDir = System.getProperty("java.io.tmpdir");
	private static File workDir;
	private static File NJBaseline;
	private static File pointsZipFile;

	@BeforeClass
	public static void setUpClass() throws IOException {
		workDir = new File(tempDir, String.valueOf(new Date().getTime()));
		FileUtils.deleteQuietly(workDir);
		FileUtils.forceMkdir(workDir);
	}

	@AfterClass
	public static void tearDownClass() {
		FileUtils.deleteQuietly(workDir);
	}

	@Before
	public void setUp() throws URISyntaxException, IOException {
		String packagePath = "/gov/usgs/cida/owsutils/commons/sampleshapefiles";
		FileUtils.copyDirectory(new File(getClass().getResource(packagePath).toURI()), workDir);
		NJBaseline = new File(workDir, "NJ_baseline_w_orient.zip");
		pointsZipFile = new File(workDir, "test_shorelines_pts.zip");
	}

	@After
	public void tearDown() {
		for (File file : FileUtils.listFiles(workDir, null, true)) {
			FileUtils.deleteQuietly(file);
		}
	}

	@Test
	public void testGetFeatureCollectionFromShp() throws Exception {
		System.out.println("testGetFeatureCollectionFromShp");
		URL shp = null;
		
		File tmpDir = new File(workDir, String.valueOf(new Date().getTime()));
		FileUtils.forceMkdir(tmpDir);
		FileHelper.unzipFile(tmpDir.getAbsolutePath(), pointsZipFile);
		shp = new File(tmpDir, "test_shorelines_pts.shp").toURI().toURL();
		
		FeatureCollection<SimpleFeatureType, SimpleFeature> coll = FeatureCollectionFromShp.getFeatureCollectionFromShp(shp);
		
		CoordinateReferenceSystem crs = coll.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem();
		assertEquals(crs.getName().getCodeSpace(), "EPSG");
		assertEquals(crs.getName().getCode(), "WGS 84 / Pseudo-Mercator");
		assertEquals(CRS.toSRS(crs), "EPSG:3857");
		assertEquals(coll.getSchema().getAttributeCount(), 7);
		
		assertNotEquals(coll.size(), 0);
		assertEquals(coll.size(), 3379);
		
		ReprojectFeatureResults results = new ReprojectFeatureResults(coll, DefaultGeographicCRS.WGS84);
		assertEquals(CRS.toSRS(results.getOrigin().getSchema().getGeometryDescriptor().getCoordinateReferenceSystem()), "EPSG:3857");
		assertEquals(CRS.toSRS(results.getSchema().getCoordinateReferenceSystem()), "CRS:84");

		Iterator<SimpleFeature> iter = results.iterator();
		SimpleFeature sf = iter.next();
		assertEquals(sf.getFeatureType().getType("DATE_").getBinding(), java.lang.String.class);
		
		String dateString = (String) sf.getAttribute("DATE_");
		assertEquals(dateString, "01/01/1927");
		results.closeIterator(iter);
		
		try {
			FileUtils.deleteDirectory(tmpDir);
		} catch (IOException ex) {
			// meh
		}
	}

}
