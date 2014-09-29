package gov.usgs.cida.owsutils.commons.shapefile.utils;

import gov.usgs.cida.owsutils.commons.io.FileHelper;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

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

	@Test
	@Ignore
	public void testGetDbfHeader() {
		System.out.println("getDbfHeader");
		IterableShapefileReader instance = null;
		DbaseFileHeader expResult = null;
		DbaseFileHeader result = instance.getDbfHeader();
		assertEquals(expResult, result);
		fail("The test case is a prototype.");
	}

	@Test
	@Ignore
	public void testGetShpFiles() {
		System.out.println("getShpFiles");
		IterableShapefileReader instance = null;
		ShpFiles expResult = null;
		ShpFiles result = instance.getShpFiles();
		assertEquals(expResult, result);
		fail("The test case is a prototype.");
	}

	@Test
	@Ignore
	public void testIterator() {
		System.out.println("iterator");
		IterableShapefileReader instance = null;
		Iterator<ShapeAndAttributes> expResult = null;
		Iterator<ShapeAndAttributes> result = instance.iterator();
		assertEquals(expResult, result);
		fail("The test case is a prototype.");
	}

	@Test
	@Ignore
	public void testHasNext() {
		System.out.println("hasNext");
		IterableShapefileReader instance = null;
		boolean expResult = false;
		boolean result = instance.hasNext();
		assertEquals(expResult, result);
		fail("The test case is a prototype.");
	}

	@Test
	@Ignore
	public void testNext() {
		System.out.println("next");
		IterableShapefileReader instance = null;
		ShapeAndAttributes expResult = null;
		ShapeAndAttributes result = instance.next();
		assertEquals(expResult, result);
		fail("The test case is a prototype.");
	}

	@Test
	@Ignore
	public void testRemove() {
		System.out.println("remove");
		IterableShapefileReader instance = null;
		instance.remove();
		fail("The test case is a prototype.");
	}

}
