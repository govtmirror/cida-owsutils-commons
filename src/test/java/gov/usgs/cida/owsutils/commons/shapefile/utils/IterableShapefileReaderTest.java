package gov.usgs.cida.owsutils.commons.shapefile.utils;

import gov.usgs.cida.owsutils.commons.io.FileHelper;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader.Row;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileReader.Record;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author isuftin
 */
public class IterableShapefileReaderTest {

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

	public IterableShapefileReaderTest() {
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
	public void loadShapefile() throws Exception {
		System.out.println("loadShapefile");
		File tmpDir = new File(workDir, String.valueOf(new Date().getTime()));
		FileUtils.forceMkdir(tmpDir);
		FileHelper.unzipFile(tmpDir.getAbsolutePath(), NJBaseline);
		try (IterableShapefileReader subject = new IterableShapefileReader(new ShpFiles(new File(tmpDir, "baseline.shp")))) {
			assertTrue(subject.hasNext());
		}
		try {
			FileUtils.deleteDirectory(tmpDir);
		} catch (IOException ex) {
			// meh
		}
	}

	@Test
	public void readDBFFile() throws Exception {
		System.out.println("readDBFFile");
		File tmpDir = new File(workDir, String.valueOf(new Date().getTime()));
		FileUtils.forceMkdir(tmpDir);
		FileHelper.unzipFile(tmpDir.getAbsolutePath(), NJBaseline);
		try (IterableShapefileReader subject = new IterableShapefileReader(new ShpFiles(new File(tmpDir, "baseline.shp")))) {
			DbaseFileHeader dbfHeader = subject.getDbfHeader();

			assertNotNull(dbfHeader);
			assertEquals(dbfHeader.getNumFields(), 2);
			assertEquals(dbfHeader.getFieldName(0), "Id");
			assertEquals(dbfHeader.getFieldName(1), "temp");
			try {
				assertEquals(dbfHeader.getFieldName(2), "This will be an exception");
			} catch (Exception ex) {
				assertEquals(ex.getClass(), ArrayIndexOutOfBoundsException.class);
			}
		}
		try {
			FileUtils.deleteDirectory(tmpDir);
		} catch (IOException ex) {
			// meh
		}
	}

	@Test
	public void readPointsFile() throws IOException, Exception {
		File tmpDir = new File(workDir, String.valueOf(new Date().getTime()));
		FileUtils.forceMkdir(tmpDir);
		FileHelper.unzipFile(tmpDir.getAbsolutePath(), pointsZipFile);
		try (IterableShapefileReader subject = new IterableShapefileReader(new ShpFiles(new File(tmpDir, "test_shorelines_pts.shp")))) {
			DbaseFileHeader dbfHeader = subject.getDbfHeader();
			assertNotNull(dbfHeader);
			assertEquals(dbfHeader.getNumRecords(), 3379);
			assertEquals(dbfHeader.getNumFields(), 6);

			Iterator<ShapeAndAttributes> iterator = subject.iterator();
			assertTrue(iterator.hasNext());

			ShapeAndAttributes saa = iterator.next();
			assertNotNull(saa);

			Record record = saa.record;
			assertEquals(record.type.name, "Point");
			assertEquals(record.maxX, record.minX, 0);
			assertEquals(record.maxY, record.minY, 0);
			assertEquals(record.number, 1);
			assertEquals(record.envelope().centre().x, -1.756861581602166E7, 0);
			assertEquals(record.envelope().centre().y, 2423192.79378892, 0);
			assertEquals(record.envelope().centre().z, Double.NaN, 0);

			Row row = saa.row;
			assertNull(row.read(0));
			assertNull(row.read(1));
			assertEquals(row.read(2), "01/01/1927");
			assertEquals(row.read(3), 4.795d);
			assertEquals(row.read(4), 1.0d);
			assertEquals(row.read(5), 1.0d);

			try {
				assertEquals(saa.row.read(6), "Nothing here");
			} catch (Exception ex) {
				assertEquals(ex.getClass(), java.lang.ArrayIndexOutOfBoundsException.class);
			}

			assertTrue("survived", true);
		}
		try {
			FileUtils.deleteDirectory(tmpDir);
		} catch (IOException ex) {
			// meh
		}
	}
}
