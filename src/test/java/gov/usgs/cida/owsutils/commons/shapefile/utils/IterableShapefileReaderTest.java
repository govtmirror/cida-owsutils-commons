package gov.usgs.cida.owsutils.commons.shapefile.utils;

import gov.usgs.cida.owsutils.commons.io.FileHelper;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
	}

	@After
	public void tearDown() {
		for (File file : FileUtils.listFiles(workDir, null, true)) {
			FileUtils.deleteQuietly(file);
		}
	}

	@Test
	public void loadShapefile() throws IOException {
		System.out.println("loadShapefile");
		File tmpDir = new File(workDir, String.valueOf(new Date().getTime()));
		FileUtils.forceMkdir(tmpDir);
		FileHelper.unzipFile(tmpDir.getAbsolutePath(), NJBaseline);
		IterableShapefileReader subject = new IterableShapefileReader(new File(tmpDir, "baseline.shp"));
		assertTrue(subject.hasNext());
		try {
			FileUtils.deleteDirectory(tmpDir);
		} catch (IOException ex) {
			// meh
		}
	}
	
	
	@Test
	public void readDBFFile() throws IOException {
		System.out.println("readDBFFile");
		File tmpDir = new File(workDir, String.valueOf(new Date().getTime()));
		FileUtils.forceMkdir(tmpDir);
		FileHelper.unzipFile(tmpDir.getAbsolutePath(), NJBaseline);
		IterableShapefileReader subject = new IterableShapefileReader(new File(tmpDir, "baseline.shp"));
		
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
		
		try {
			FileUtils.deleteDirectory(tmpDir);
		} catch (IOException ex) {
			// meh
		}
	}
}
