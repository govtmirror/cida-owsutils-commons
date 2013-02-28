package gov.usgs.cida.owsutils.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
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
    private String macZippedZipName = "valid_shapezip.zip";
    private File validShapefileZip = null;
    private File macZippedZip = null;
    
    private File multiDirZip = null;
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
    public  void beforeTest() throws Exception {
        tempArea = new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString());
        FileUtils.forceMkdir(tempArea);
        
        // Valid shapefile
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(sampleShapefileLocation + validShapefileZipName);
        FileUtils.copyFileToDirectory(new File(url.toURI()), tempArea);
        validShapefileZip = new File(tempArea, validShapefileZipName);
        
        // Valid shapefile
        cl = Thread.currentThread().getContextClassLoader();
        url = cl.getResource(sampleShapefileLocation + macZippedZipName);
        FileUtils.copyFileToDirectory(new File(url.toURI()), tempArea);
        macZippedZip = new File(tempArea, macZippedZipName);
        
    }
    
    @After
    public  void afterTest() throws Exception {
        IOUtils.closeQuietly(fis);
        FileUtils.forceDelete(tempArea);
    }
    
        /**
     * Test of validateShapefileZip method, of class FileHelper.
     */
    @Test
    public void testValidateValidShapefileZip() throws Exception {
        System.out.println("validateValidShapefileZip");
        File shapeZip = null;
        Boolean expResult = true;
        Boolean result = FileHelper.validateShapefileZip(validShapefileZip);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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
     * Test of saveFileFromRequest method, of class FileHelper.
     */
    @Test
    @Ignore
    public void testSaveFileFromRequest() throws Exception {
        System.out.println("saveFileFromRequest");
        InputStream is = null;
        File destinationFile = null;
        FileHelper.saveFileFromRequest(is, destinationFile);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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