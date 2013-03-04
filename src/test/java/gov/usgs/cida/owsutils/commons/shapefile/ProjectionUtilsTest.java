package gov.usgs.cida.owsutils.commons.shapefile;

import gov.usgs.cida.owsutils.commons.io.FileHelper;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
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
public class ProjectionUtilsTest {

    private String sampleShapefileLocation = "gov/usgs/cida/owsutils/commons/sampleshapefiles/";
    private String validShapefileZipName = "valid_shapezip.zip";
    private String macZippedZipName = "valid_shapezip.zip";
    private String zipWithSubfolderZipName = "zip_with_subfolder.zip";
    private String epsg26917ZipName = "epsg_26917.zip";
    private String epsg4326ZipName = "epsg_4326.zip";
    private String epsg5070ZipName = "epsg_5070.zip";
    private String noProjDefault4326ZipName = "no_proj_default_to_epsg_4326.zip";
    private String multiShpZipName = "multiple_shapefiles.zip";
    private String NJShpZipName = "NJ_baseline_w_orient.zip";
    private File validShapefileZip = null;
    private File macZippedZip = null;
    private File epsg26917Zip = null;
    private File epsg4326Zip = null;
    private File epsg5070Zip = null;
    private File noProjDefault4326Zip = null;
    private File zipWithSubfolderZip = null;
    private File multiShpZipNameZip = null;
    private File NJShpZipNameZip = null;
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

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(sampleShapefileLocation + validShapefileZipName);
        FileUtils.copyFileToDirectory(new File(url.toURI()), tempArea);
        validShapefileZip = new File(tempArea, validShapefileZipName);

        url = cl.getResource(sampleShapefileLocation + macZippedZipName);
        FileUtils.copyFileToDirectory(new File(url.toURI()), tempArea);
        macZippedZip = new File(tempArea, macZippedZipName);

        url = cl.getResource(sampleShapefileLocation + zipWithSubfolderZipName);
        FileUtils.copyFileToDirectory(new File(url.toURI()), tempArea);
        zipWithSubfolderZip = new File(tempArea, zipWithSubfolderZipName);

        url = cl.getResource(sampleShapefileLocation + epsg26917ZipName);
        FileUtils.copyFileToDirectory(new File(url.toURI()), tempArea);
        epsg26917Zip = new File(tempArea, epsg26917ZipName);

        url = cl.getResource(sampleShapefileLocation + epsg4326ZipName);
        FileUtils.copyFileToDirectory(new File(url.toURI()), tempArea);
        epsg4326Zip = new File(tempArea, epsg4326ZipName);

        url = cl.getResource(sampleShapefileLocation + epsg5070ZipName);
        FileUtils.copyFileToDirectory(new File(url.toURI()), tempArea);
        epsg5070Zip = new File(tempArea, epsg5070ZipName);

        url = cl.getResource(sampleShapefileLocation + noProjDefault4326ZipName);
        FileUtils.copyFileToDirectory(new File(url.toURI()), tempArea);
        noProjDefault4326Zip = new File(tempArea, noProjDefault4326ZipName);

        url = cl.getResource(sampleShapefileLocation + multiShpZipName);
        FileUtils.copyFileToDirectory(new File(url.toURI()), tempArea);
        multiShpZipNameZip = new File(tempArea, multiShpZipName);

        url = cl.getResource(sampleShapefileLocation + NJShpZipName);
        FileUtils.copyFileToDirectory(new File(url.toURI()), tempArea);
        NJShpZipNameZip = new File(tempArea, NJShpZipName);

    }

    @After
    public void afterTest() throws Exception {
        IOUtils.closeQuietly(fis);
        FileUtils.forceDelete(tempArea);
    }

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
        File shapefile = zipWithSubfolderZip;
        byte[] result = ProjectionUtils.getPRJByteArrayFromShapefileZip(shapefile);
        assertNotEquals(result.length, 0);
    }

    @Test
    public void testGetProjectionFromShapefile() throws Exception {
        System.out.println("getPRJByteArrayFromMultidirShapefileZip");
        File shapefile = validShapefileZip;
        try {
            ProjectionUtils.getProjectionFromShapefileZip(shapefile, false);
        } catch (Exception ex) {
            assertTrue(ex.getMessage().equals("Could not find EPSG code for prj definition. Please ensure proper projection and a valid PRJ file."));
        }

        String result;
        try {
            result = ProjectionUtils.getProjectionFromShapefileZip(shapefile, true);
            assertFalse(result.isEmpty());
            assertEquals("EPSG:4326", result);
        } catch (Exception ex) {
            fail("Test reached unexpected exception. " + ex.getMessage());
        }
    }

    @Test
    public void testGetProjectionFromMultiDirhapefile() throws Exception {
        System.out.println("getProjectionFromMultiDirhapefile");
        File shapefile = zipWithSubfolderZip;
        FileHelper.flattenZipFile(shapefile.getPath());
        try {
            ProjectionUtils.getProjectionFromShapefileZip(shapefile, false);
        } catch (Exception ex) {
            assertTrue(ex.getMessage().equals("Could not find EPSG code for prj definition. Please ensure proper projection and a valid PRJ file."));
        }

        String result;
        try {
            result = ProjectionUtils.getProjectionFromShapefileZip(shapefile, true);
            assertFalse(result.isEmpty());
            assertEquals("EPSG:4326", result);
        } catch (Exception ex) {
            fail("Test reached unexpected exception. " + ex.getMessage());
        }
    }

    @Test
    public void testGetProjectionFromEPSG26917Zip() throws Exception {
        System.out.println("getProjectionFromEPSG26917Zip");
        File shapefile = epsg26917Zip;
        FileHelper.flattenZipFile(shapefile.getPath());
        try {
            ProjectionUtils.getProjectionFromShapefileZip(shapefile, false);
        } catch (Exception ex) {
            assertTrue(ex.getMessage().equals("Could not find EPSG code for prj definition. Please ensure proper projection and a valid PRJ file."));
        }

        String result;
        try {
            result = ProjectionUtils.getProjectionFromShapefileZip(shapefile, true);
            assertEquals("EPSG:26917", result);
        } catch (Exception ex) {
            assertEquals(ex.getMessage(), "Could not find EPSG code for prj definition. Please ensure proper projection and a valid PRJ file.");
        }
    }

    @Test
    public void testGetProjectionFromEPSG4326Zip() throws Exception {
        System.out.println("getProjectionFromEPSG4326Zip");
        File shapefile = epsg4326Zip;
        FileHelper.flattenZipFile(shapefile.getPath());
        try {
            ProjectionUtils.getProjectionFromShapefileZip(shapefile, false);
        } catch (Exception ex) {
            assertTrue(ex.getMessage().equals("Could not find EPSG code for prj definition. Please ensure proper projection and a valid PRJ file."));
        }

        String result;
        try {
            result = ProjectionUtils.getProjectionFromShapefileZip(shapefile, true);
            assertFalse(result.isEmpty());
            assertEquals("EPSG:4326", result);
        } catch (Exception ex) {
            assertEquals(ex.getMessage(), "Could not find EPSG code for prj definition. Please ensure proper projection and a valid PRJ file.");
        }
    }

    @Test
    public void testGetProjectionFromEPSG5070Zip() throws Exception {
        System.out.println("getProjectionFromEPSG5070Zip");
        File shapefile = epsg5070Zip;
        FileHelper.flattenZipFile(shapefile.getPath());
        String projectionFromShapefileZip = ProjectionUtils.getProjectionFromShapefileZip(shapefile, false);
        assertEquals(projectionFromShapefileZip, "EPSG:5070");
    }

    @Test
    public void testGetProjectionFromnoProjDefault4326Zip() throws Exception {
        System.out.println("getProjectionFromnoProjDefault4326Zip");
        File shapefile = noProjDefault4326Zip;
        FileHelper.flattenZipFile(shapefile.getPath());
        try {
            String projectionFromShapefileZip = ProjectionUtils.getProjectionFromShapefileZip(shapefile, false);
            assertEquals(projectionFromShapefileZip, "CRS:84");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().equals("Could not find EPSG code for prj definition. Please ensure proper projection and a valid PRJ file."));
        }

        String result;
        try {
            result = ProjectionUtils.getProjectionFromShapefileZip(shapefile, true);
            assertFalse(result.isEmpty());
            assertEquals("EPSG:4326", result);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testGetProjectionFromMultiShpZipNameZip() throws Exception {
        System.out.println("getProjectionFromMultiShpZipNameZip");
        File shapefile = multiShpZipNameZip;
        FileHelper.flattenZipFile(shapefile.getPath());
        try {
            ProjectionUtils.getProjectionFromShapefileZip(shapefile, false);
        } catch (Exception ex) {
            assertTrue(ex.getMessage().equals("Error while getting EPSG information from PRJ file. Function halted."));
        }
    }

    @Test
    public void testGetProjectionFromNJZip() throws Exception {
        System.out.println("getProjectionFromNJZip");
        File shapefile = NJShpZipNameZip;
        FileHelper.flattenZipFile(shapefile.getPath());
        try {
        String result = ProjectionUtils.getProjectionFromShapefileZip(shapefile, true);
        assertFalse(result.isEmpty());
        } catch(Exception ex) {
            fail(ex.getMessage());
        }
        
    }
}