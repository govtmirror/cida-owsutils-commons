package gov.usgs.cida.owsutils.commons.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.opengis.referencing.FactoryException;

/**
 *
 * @author isuftin
 */
public class ProjectionUtilsTest {

    private String sampleShapefileLocation = "gov/usgs/cida/owsutils/commons/sampleshapefiles/";
    private String validShapefileZipName = "valid_shapezip.zip";
    private String macZippedZipName = "valid_shapezip.zip";
    private String zipWithSubfolderZipName = "zip_with_subfolder.zip";
    private File validShapefileZip = null;
    private File macZippedZip = null;
    private File zipWithSubfolder = null;
    private FileInputStream fis = null;
    private File tempArea = null;

    public ProjectionUtilsTest() {
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

    /**
     * Test of getProjectionFromShapefile method, of class ProjectionUtils.
     */
//    @Test
//    @Ignore
//    public void testGetProjection() {
//        System.out.println("getProjectionFromShapefile");
//        File shapefile = null;
//        String expResult = "";
//        String result = ProjectionUtils.getProjectionFromShapefile(shapefile);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of getPRJByteArrayFromShapefileZip method, of class ProjectionUtils.
     *
     * @throws Exception
     */
    @Test
    public void testGetPRJByteArrayFromShapefileZip() throws Exception {
        System.out.println("getPRJByteArrayFromShapefileZip");
        File shapefile = validShapefileZip;
        byte[] result = ProjectionUtils.getPRJByteArrayFromShapefileZip(shapefile);
        assertNotEquals(result.length, 0);
    }

    @Test
    public void testGetPRJByteArrayFromMacShapefileZip() throws Exception {
        System.out.println("getPRJByteArrayFromMacShapefileZip");
        File shapefile = macZippedZip;
        byte[] result = ProjectionUtils.getPRJByteArrayFromShapefileZip(shapefile);
        assertNotEquals(result.length, 0);
    }

    @Test
    public void testGetPRJByteArrayFromMultidirShapefileZip() throws Exception {
        System.out.println("getPRJByteArrayFromMultidirShapefileZip");
        File shapefile = zipWithSubfolder;
        byte[] result = ProjectionUtils.getPRJByteArrayFromShapefileZip(shapefile);
        assertNotEquals(result.length, 0);
    }

    @Test
    public void testGetProjectionFromShapefile() throws Exception {
        System.out.println("getPRJByteArrayFromMultidirShapefileZip");
        File shapefile = validShapefileZip;
        try {
            ProjectionUtils.getProjectionFromShapefileZip(shapefile, false);
        } catch (Exception ex)  {
            assertTrue(ex.getMessage().equals("Could not find EPSG code for prj definition. Please ensure proper projection and a valid PRJ file."));
        }
        
        String result;
        try {
            result = ProjectionUtils.getProjectionFromShapefileZip(shapefile, true);
            assertFalse(result.isEmpty());
        } catch (Exception ex)  {
            fail("Test reached unexpected exception. " + ex.getMessage());
        }
        
    }

    /**
     * Test of getDeclaredEPSGFromPrj method, of class ProjectionUtils.
     */
    @Test
    @Ignore
    public void testGetDeclaredEPSGFromPrj() throws Exception {
        System.out.println("getDeclaredEPSGFromPrj");
        File prjFile = null;
        String expResult = "";
        String result = ProjectionUtils.getDeclaredEPSGFromPrj(prjFile);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDeclaredEPSGFromWKT method, of class ProjectionUtils.
     */
    @Test
    @Ignore
    public void testGetDeclaredEPSGFromWKT_String() throws Exception {
        System.out.println("getDeclaredEPSGFromWKT");
        String wkt = "";
        String expResult = "";
        String result = ProjectionUtils.getDeclaredEPSGFromWKT(wkt);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDeclaredEPSGFromWKT method, of class ProjectionUtils.
     */
    @Test
    @Ignore
    public void testGetDeclaredEPSGFromWKT_String_boolean() throws Exception {
        System.out.println("getDeclaredEPSGFromWKT");
        String wkt = "";
        boolean useBaseCRSFailover = false;
        String expResult = "";
        String result = ProjectionUtils.getDeclaredEPSGFromWKT(wkt, useBaseCRSFailover);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}