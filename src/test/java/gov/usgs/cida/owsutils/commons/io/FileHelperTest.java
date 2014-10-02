package gov.usgs.cida.owsutils.commons.io;

import gov.usgs.cida.owsutils.commons.shapefile.utils.IterableShapefileReader;
import gov.usgs.cida.owsutils.commons.shapefile.utils.ShapeAndAttributes;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
public class FileHelperTest {

	private String sampleShapefileLocation = "gov/usgs/cida/owsutils/commons/sampleshapefiles/";
	private String validShapefileZipName = "valid_shapezip.zip";
	private String validShapefileZip2dbfName = "valid_shapezip_w_2dbf.zip";
	private String macZippedZipName = "valid_shapezip.zip";
	private String zipWithSubfolderZipName = "zip_with_subfolder.zip";
	private File validShapefileZip = null;
	private File validShapefileZip2dbf = null;
	private File macZippedZip = null;
	private File zipWithSubfolder = null;
	private FileInputStream fis = null;
	private File tempArea = null;

	public FileHelperTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void beforeTest() throws Exception {
		tempArea = new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString());
		FileUtils.forceMkdir(tempArea);

		// Valid shapefile
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL url = cl.getResource(sampleShapefileLocation + validShapefileZipName);
		FileUtils.copyFileToDirectory(new File(url.toURI()), tempArea);
		validShapefileZip = new File(tempArea, validShapefileZipName);

		// Valid shapefile w/ 2 dbf files
		cl = Thread.currentThread().getContextClassLoader();
		url = cl.getResource(sampleShapefileLocation + validShapefileZip2dbfName);
		FileUtils.copyFileToDirectory(new File(url.toURI()), tempArea);
		validShapefileZip2dbf = new File(tempArea, validShapefileZip2dbfName);

		// Valid shapefile
		cl = Thread.currentThread().getContextClassLoader();
		url = cl.getResource(sampleShapefileLocation + macZippedZipName);
		FileUtils.copyFileToDirectory(new File(url.toURI()), tempArea);
		macZippedZip = new File(tempArea, macZippedZipName);

		// Valid shapefile
		cl = Thread.currentThread().getContextClassLoader();
		url = cl.getResource(sampleShapefileLocation + zipWithSubfolderZipName);
		FileUtils.copyFileToDirectory(new File(url.toURI()), tempArea);
		zipWithSubfolder = new File(tempArea, zipWithSubfolderZipName);

	}

	@After
	public void afterTest() throws Exception {
		IOUtils.closeQuietly(fis);
		FileUtils.forceDelete(tempArea);
	}

	@Test
	public void testValidateValidShapefileZip() throws Exception {
		System.out.println("validateValidShapefileZip");
		FileHelper.validateShapefileZip(validShapefileZip);
		assertTrue(true);
	}

	/**
	 * Multiple dbf files are allowed b/c this is a common way++ for projects to
	 * stuff a bit more custom data into a shapefile package.
	 *
	 * ++OK, The Coastal Hazards project does this, but others may as well.
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateValidShapefileZipWithTwoDbfFiles() throws Exception {
		System.out.println("validShapefileZip2dbf");
		FileHelper.validateShapefileZip(validShapefileZip2dbf);
		assertTrue(true);
	}

	@Test
	public void testValidateMacShapefileZip() throws Exception {
		System.out.println("validateMacShapefileZip");
		FileHelper.validateShapefileZip(macZippedZip);
		assertTrue(true);
	}

	@Test
	public void testValidateZipWithSubfolderZip() throws Exception {
		System.out.println("validateZipWithSubfolderZip");
		FileHelper.flattenZipFile(zipWithSubfolder.getPath());
		FileHelper.validateShapefileZip(zipWithSubfolder);
		assertTrue(true);
	}

	@Test
	public void testFlattenZipWithSubfolderZip() throws Exception {
		System.out.println("flattenZipWithSubfolderZip");
		FileHelper.flattenZipFile(zipWithSubfolder.getPath());
		assertEquals(zipWithSubfolder.exists(), true);
	}

	@Test
	public void testFlattenAndVerifyZipWithSubfolderZip() throws Exception {
		System.out.println("flattenAndVerifyZipWithSubfolderZip");
		Boolean expResult = false;

		// At first this file has folders within the zip, which do not validate
		try {
			FileHelper.validateShapefileZip(zipWithSubfolder);
		} catch (IOException ioe) {
			assertNotNull(ioe);
		}

		// Flatten the zip file so all files come up to the root 
		FileHelper.flattenZipFile(zipWithSubfolder.getPath());
		assertEquals(zipWithSubfolder.exists(), true);

		// The shapefile should now be valid
		FileHelper.validateShapefileZip(zipWithSubfolder);
		assertTrue(true);
	}

	@Test
	public void testFlattenAndVerifyValidZip() throws Exception {
		System.out.println("flattenAndVerifyZipWithSubfolderZip");
		Boolean expResult = true;

		try {
			FileHelper.validateShapefileZip(zipWithSubfolder);
		} catch (IOException ioe) {
			assertNotNull(ioe);
		}

		// Flatten the zip file so all files come up to the root 
		FileHelper.flattenZipFile(validShapefileZip.getPath());
		assertEquals(validShapefileZip.exists(), expResult);

		// The shapefile should now be valid
		FileHelper.validateShapefileZip(validShapefileZip);
		assertTrue(true);
	}

	@Test
	public void testLoadShapefileIntoReader() throws IOException {
		System.out.println("testLoadShapefileIntoReader");
		File tempLoc = FileHelper.createTemporaryDirectory();
		FileHelper.unzipFile(tempLoc.getAbsolutePath(), validShapefileZip);

		try {
			IterableShapefileReader reader = FileHelper.loadShapefileFromDirectoryIntoReader(tempLoc);
			assertNotNull(reader);
			assertTrue(reader.iterator().hasNext());
			
			ShapeAndAttributes shapeAndAttr = reader.iterator().next();
			assertNotNull(shapeAndAttr);
		} finally {
			FileHelper.forceDelete(tempLoc);
		}

	}
}
