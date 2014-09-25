package gov.usgs.cida.owsutils.commons.io;

import gov.usgs.cida.owsutils.commons.io.FileHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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

    /**
     * Test of base64Encode method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testBase64Encode_File() throws Exception {
        System.out.println("base64Encode");
        File input = null;
        byte[] expResult = null;
        byte[] result = FileHelper.base64Encode(input);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of base64Encode method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testBase64Encode_byteArr() {
        System.out.println("base64Encode");
        byte[] input = null;
        byte[] expResult = null;
        byte[] result = FileHelper.base64Encode(input);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getByteArrayFromFile method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testGetByteArrayFromFile() throws Exception {
        System.out.println("getByteArrayFromFile");
        File file = null;
        byte[] expResult = null;
        byte[] result = FileHelper.getByteArrayFromFile(file);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of renameFile method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testRenameFile() throws Exception {
        System.out.println("renameFile");
        File fromFile = null;
        String toFileName = "";
        boolean expResult = false;
        boolean result = FileHelper.renameFile(fromFile, toFileName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of copyFileToPath method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testCopyFileToPath_File_String() throws Exception {
        System.out.println("copyFileToPath");
        File inFile = null;
        String outFilePath = "";
        boolean expResult = false;
        boolean result = FileHelper.copyFileToPath(inFile, outFilePath);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of copyFileToPath method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testCopyFileToPath_3args() throws Exception {
        System.out.println("copyFileToPath");
        File inFile = null;
        String outPath = "";
        boolean deleteOriginalFile = false;
        boolean expResult = false;
        boolean result = FileHelper.copyFileToPath(inFile, outPath, deleteOriginalFile);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of copyInputStreamToFile method, of class FileHelper.
     */
    @Test
    public void copyInputStreamToFileNoOverwrite() throws Exception {
        System.out.println("copyInputStreamToFile");
        InputStream is = new FileInputStream(validShapefileZip);
        File destinationFile = File.createTempFile("deleteme", String.valueOf(new Date().getTime()));
		destinationFile.deleteOnExit();
        FileHelper.copyInputStreamToFile(is, destinationFile);
        assertTrue(destinationFile.exists());
		assertTrue(destinationFile.length() == validShapefileZip.length());
    }

    /**
     * Test of wipeOldFiles method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testWipeOldFiles() {
        System.out.println("wipeOldFiles");
        File directory = null;
        Long cutoffTime = null;
        boolean deleteDirectory = false;
        Collection expResult = null;
        Collection result = FileHelper.wipeOldFiles(directory, cutoffTime, deleteDirectory);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createDir method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testCreateDir_File() {
        System.out.println("createDir");
        File directory = null;
        boolean expResult = false;
        boolean result = FileHelper.createDir(directory);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createDir method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testCreateDir_String() {
        System.out.println("createDir");
        String directory = "";
        boolean expResult = false;
        boolean result = FileHelper.createDir(directory);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteDirRecursively method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testDeleteDirRecursively_File() throws Exception {
        System.out.println("deleteDirRecursively");
        File directory = null;
        boolean expResult = false;
        boolean result = FileHelper.deleteDirRecursively(directory);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteDirRecursively method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testDeleteDirRecursively_String() throws Exception {
        System.out.println("deleteDirRecursively");
        String directory = "";
        boolean expResult = false;
        boolean result = FileHelper.deleteDirRecursively(directory);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteFileQuietly method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testDeleteFileQuietly_String() {
        System.out.println("deleteFileQuietly");
        String filePath = "";
        boolean expResult = false;
        boolean result = FileHelper.deleteFileQuietly(filePath);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteFileQuietly method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testDeleteFileQuietly_File() {
        System.out.println("deleteFileQuietly");
        File file = null;
        boolean expResult = false;
        boolean result = FileHelper.deleteFileQuietly(file);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteFile method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testDeleteFile_String() {
        System.out.println("deleteFile");
        String filePath = "";
        boolean expResult = false;
        boolean result = FileHelper.deleteFile(filePath);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteFile method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testDeleteFile_File() {
        System.out.println("deleteFile");
        File file = null;
        boolean expResult = false;
        boolean result = FileHelper.deleteFile(file);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of doesDirectoryOrFileExist method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testDoesDirectoryOrFileExist() {
        System.out.println("doesDirectoryOrFileExist");
        String filePath = "";
        boolean expResult = false;
        boolean result = FileHelper.doesDirectoryOrFileExist(filePath);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findFile method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testFindFile() {
        System.out.println("findFile");
        String file = "";
        String rootPath = "";
        File expResult = null;
        File result = FileHelper.findFile(file, rootPath);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFileList method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testGetFileList_String_boolean() {
        System.out.println("getFileList");
        String filePath = "";
        boolean recursive = false;
        List expResult = null;
        List result = FileHelper.getFileList(filePath, recursive);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFileList method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testGetFileList_3args() {
        System.out.println("getFileList");
        String filePath = "";
        String[] extensions = null;
        boolean recursive = false;
        List expResult = null;
        List result = FileHelper.getFileList(filePath, extensions, recursive);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFileCollection method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testGetFileCollection_String_boolean() {
        System.out.println("getFileCollection");
        String filePath = "";
        boolean recursive = false;
        Collection expResult = null;
        Collection result = FileHelper.getFileCollection(filePath, recursive);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFileCollection method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testGetFileCollection_3args() {
        System.out.println("getFileCollection");
        String filePath = "";
        String[] extensions = null;
        boolean recursive = false;
        Collection expResult = null;
        Collection result = FileHelper.getFileCollection(filePath, extensions, recursive);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSystemTemp method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testGetSystemTemp() {
        System.out.println("getSystemTemp");
        String expResult = "";
        String result = FileHelper.getSystemTemp();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of unzipFile method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testUnzipFile() throws Exception {
        System.out.println("unzipFile");
        String outputDirectory = "";
        File zipFile = null;
        boolean expResult = false;
        boolean result = FileHelper.unzipFile(outputDirectory, zipFile);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createUserDirectory method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testCreateUserDirectory() {
        System.out.println("createUserDirectory");
        String applicationUserSpaceDir = "";
        String expResult = "";
        String result = FileHelper.createUserDirectory(applicationUserSpaceDir);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateTimestamp method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testUpdateTimestamp() throws Exception {
        System.out.println("updateTimestamp");
        String path = "";
        boolean recursive = false;
        boolean expResult = false;
        boolean result = FileHelper.updateTimestamp(path, recursive);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFilesOlderThan method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testGetFilesOlderThan() {
        System.out.println("getFilesOlderThan");
        File filePath = null;
        Long age = null;
        Boolean recursive = null;
        Collection expResult = null;
        Collection result = FileHelper.getFilesOlderThan(filePath, age, recursive);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of validateShapeFile method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testValidateShapeFile() {
        System.out.println("validateShapeFile");
        File shapeFile = null;
        Boolean expResult = null;
        Boolean result = FileHelper.validateShapeFile(shapeFile);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}