package gov.usgs.cida.owsutils.commons.shapefile;

import gov.usgs.cida.owsutils.commons.io.FileHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import static org.apache.commons.io.FileUtils.getTempDirectory;
import org.apache.commons.lang.StringUtils;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.slf4j.LoggerFactory;

/**
 *
 * @author isuftin
 */
public class ProjectionUtils {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ProjectionUtils.class);

    public static String getProjectionFromShapefileZip(File shapefileZip, boolean useBaseCRSFailover) throws IOException, ProjectionException, FactoryException {
        File temporaryDirectory = null;
        try {
            temporaryDirectory = new File(getTempDirectory(), UUID.randomUUID().toString() + "-deleteme");
            FileHelper.forceMkdir(temporaryDirectory);
            FileHelper.unzipFile(temporaryDirectory.getPath(), shapefileZip);
            return getProjectionFromShapefile(temporaryDirectory, useBaseCRSFailover);
        } finally {
            FileHelper.forceDelete(temporaryDirectory);
        }
    }

    public static String getProjectionFromShapefile(File shapefile, boolean useBaseCRSFailover) throws ProjectionException, IOException, FactoryException {
        String declaredCRS;
        String prjString;
        try {
            prjString = new String(getPRJByteArrayFromShapefile(shapefile));
            if (prjString.isEmpty()) {
                throw new ProjectionException("Error while getting Prj/WKT information from PRJ file. Function halted.");
            }

            declaredCRS = getDeclaredEPSGFromWKT(prjString, false);
            if (declaredCRS == null || declaredCRS.isEmpty()) {
                if (useBaseCRSFailover) {
                    LOG.debug("Could not find EPSG code for prj definition. The geographic coordinate system '" + declaredCRS + "' will be used");
                    declaredCRS = getDeclaredEPSGFromWKT(prjString, true);
                    if (StringUtils.isBlank(declaredCRS)) {
                        throw new ProjectionException("Could not find EPSG code for prj definition. Please ensure proper projection and a valid PRJ file.");
                    }
                } else {
                    throw new ProjectionException("Could not find EPSG code for prj definition. Please ensure proper projection and a valid PRJ file.");
                }
            } else if (declaredCRS.startsWith("ESRI:")) {
                declaredCRS = declaredCRS.replaceFirst("ESRI:", "EPSG:");
            }
        } catch (IOException ex) {
            throw new IOException("Error while getting EPSG information from PRJ file. Function halted.", ex);
        }
        return declaredCRS;
    }

    public static byte[] getPRJByteArrayFromShapefileZip(final File shapefile) throws FileNotFoundException, IOException {
        File temporaryDirectory = null;
        try {
            temporaryDirectory = new File(getTempDirectory(), UUID.randomUUID().toString() + "-deleteme");
            FileHelper.forceMkdir(temporaryDirectory);
            FileHelper.unzipFile(temporaryDirectory.getPath(), shapefile);
            return getPRJByteArrayFromShapefile(temporaryDirectory);
        } finally {
            FileHelper.forceDelete(temporaryDirectory);
        }
    }

    public static byte[] getPRJByteArrayFromShapefile(final File shapefile) throws FileNotFoundException, IOException {
        List<File> fileList = (List<File>) FileHelper.listFiles(shapefile, new String[]{"prj"}, true);
        if (fileList.isEmpty()) {
            throw new FileNotFoundException("Could not find PRJ file within shapefile zip");
        }
        if (fileList.size() > 1) {
            throw new IOException("Found more than one PRJ file within shapefile zip");
        }
        return FileHelper.getByteArrayFromFile(fileList.get(0));
    }

    public static String getDeclaredEPSGFromPrj(final File prjFile) throws IOException, FactoryException {
        String result = null;
        if (prjFile == null || !prjFile.exists()) {
            return result;
        }
        LOG.debug(new StringBuilder("Attempting to get EPSG from file: ").append(prjFile.getPath()).toString());

        byte[] wktByteArray = FileHelper.getByteArrayFromFile(prjFile);
        result = getDeclaredEPSGFromWKT(new String(wktByteArray));
        return result;
    }

    /**
     *
     * @param wkt
     * @return
     * @throws FactoryException
     */
    public static String getDeclaredEPSGFromWKT(final String wkt) throws FactoryException {
        return getDeclaredEPSGFromWKT(wkt, true);
    }

    /**
     *
     * @param wkt
     * @param useBaseCRSFailover Use base CRS to do a lookup
     * @return
     * @throws FactoryException
     */
    public static String getDeclaredEPSGFromWKT(final String wkt, boolean useBaseCRSFailover) throws FactoryException {
        LOG.debug(new StringBuilder("Attempting to get EPSG from WKT: ").append(wkt).toString());
        String result = null;
        if (wkt == null || "".equals(wkt)) {
            return result;
        }

        CoordinateReferenceSystem crs = null;
        try {
            crs = CRS.parseWKT(wkt);
        } catch (FactoryException ex) {
            throw ex;
        }

        result = CRS.lookupIdentifier(crs, true);

        if (result == null && crs instanceof ProjectedCRS && useBaseCRSFailover) {
            result = CRS.lookupIdentifier(((ProjectedCRS) crs).getBaseCRS(), true);
        }
        LOG.debug("Found " + result);
        return result;
    }
}
