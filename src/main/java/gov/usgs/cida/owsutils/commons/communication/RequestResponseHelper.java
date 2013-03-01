package gov.usgs.cida.owsutils.commons.communication;

import com.google.gson.Gson;
import com.jamesmurty.utils.XMLBuilder;
import gov.usgs.cida.owsutils.commons.io.FileHelper;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author isuftin
 */
public class RequestResponseHelper {

    public static enum ResponseType {
        XML, JSON;

        @Override
        public String toString() {
            if (this == XML) {
                return "application/xml";
            } else {
                return "application/json";
            }
        }
    }
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RequestResponseHelper.class);

    /**
     * Takes a HttpServletRequest, parses it for a specific parameter that
     * represents a file and saves the file as denoted by a File object
     *
     * @param request
     * @param destinationFile
     * @param fileParam
     * @throws FileUploadException
     * @throws IOException
     */
    public static void saveFileFromRequest(HttpServletRequest request, File destinationFile, String fileParam) throws FileUploadException, IOException {
        // Handle form-based upload (from IE)
        if (ServletFileUpload.isMultipartContent(request)) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);

            // Parse the request
            FileItemIterator iter;
            iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                if (fileParam.equalsIgnoreCase(name)) {
                    FileHelper.copyInputStreamToFile(item.openStream(), destinationFile);
                    break;
                }
            }
        } else {
            FileHelper.copyInputStreamToFile(request.getInputStream(), destinationFile);
        }
    }

    public static void sendErrorResponse(HttpServletResponse response, Map<String, String> responseMap, ResponseType responseType) {
        responseMap.put("success", "false");
        if (responseType == null) {
            responseType = ResponseType.JSON;
        }

        switch (responseType) {
            case XML: {
                sendXMLResponse(response, responseMap);
                break;
            }
            case JSON: {
                sendJSONResponse(response, responseMap);
            }
        }
    }

    public static void sendSuccessResponse(HttpServletResponse response, Map<String, String> responseMap, ResponseType responseType) {
        responseMap.put("success", "true");
        if (responseType == null) {
            responseType = ResponseType.JSON;
        }

        switch (responseType) {
            case XML: {
                sendXMLResponse(response, responseMap);
                break;
            }
            case JSON: {
                sendJSONResponse(response, responseMap);
            }
        }
    }

    static void sendXMLResponse(HttpServletResponse response, Map<String, String> responseMap) {
        String responseContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><success>" + responseMap.get("success") + "</success>";

        try {
            XMLBuilder root = XMLBuilder.create("Response");
            root.element("success").text(responseMap.get("success"));

            String[] keySetArray = responseMap.keySet().toArray(new String[0]);
            for (String key : keySetArray) {
                root.element("success").text(responseMap.get(key));
            }
            responseContent = root.asString();
        } catch (ParserConfigurationException ex) {
            LOG.error("Could not send response XML.", ex);
        } catch (FactoryConfigurationError ex) {
            LOG.error("Could not send response XML.", ex);
        } catch (TransformerException ex) {
            LOG.error("Could not send response XML.", ex);
        }

        sendResponse(response, ResponseType.XML.toString(), responseContent, null);
    }

    static void sendJSONResponse(HttpServletResponse response, Map<String, String> responseMap) {
        String responseContent = new Gson().toJson(responseMap);
        sendResponse(response, ResponseType.JSON.toString(), responseContent, null);
    }
    
    /**
     *
     * @param response
     * @param contentType
     * @param content
     * @param characterEncoding Must be within
     * http://www.iana.org/assignments/character-sets
     */
    static void sendResponse(HttpServletResponse response, String contentType, String content, String characterEncoding) {
        response.setContentType(contentType);
        response.setCharacterEncoding(StringUtils.isBlank(characterEncoding) ? "utf-8" : characterEncoding);
        response.setHeader("Content-Length", Integer.toString(content.length()));
        Writer writer = null;
        try {
            writer = response.getWriter();
            writer.write(content);
        } catch (IOException ex) {
            LOG.warn("Possible errror sending response data back to client", ex);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }
}
