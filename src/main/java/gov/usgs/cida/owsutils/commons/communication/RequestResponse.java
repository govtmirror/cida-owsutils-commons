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
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author isuftin
 */
public class RequestResponse {
	public static final String ERROR_STRING = "error";
	public static final String SUCCESS_STRING = "success";

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
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RequestResponse.class);

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

	/**
	 *
	 * @param response
	 * @param responseMap
	 * @param responseType Null value allowed. Will default to JSON
	 */
	public static void sendErrorResponse(HttpServletResponse response, Map<String, String> responseMap, ResponseType responseType) {
		responseMap.put(RequestResponse.SUCCESS_STRING, "false");

		if (!responseMap.containsKey("serverCode")) {
			responseMap.put("serverCode", "500");
		}

		String error = responseMap.get("error");
		if (StringUtils.isNotBlank(error)) {
			LOGGER.warn(error);
		}
		
		if (responseType == null || responseType == ResponseType.JSON) {
			sendJSONResponse(response, responseMap, false);
		} else {
			sendXMLResponse(response, responseMap, false);
		}
	}

	/**
	 *
	 * @param response
	 * @param responseMap
	 * @param responseType Null value allowed. Will default to JSON
	 */
	public static void sendSuccessResponse(HttpServletResponse response, Map<String, String> responseMap, ResponseType responseType) {
		responseMap.put(RequestResponse.SUCCESS_STRING, "true");
		if (responseType == null || responseType == ResponseType.JSON) {
			sendJSONResponse(response, responseMap, true);
		} else {
			sendXMLResponse(response, responseMap, true);
		}
	}

	/**
	 *
	 * @param response
	 * @param responseMap
	 * @param isOk
	 */
	static void sendXMLResponse(HttpServletResponse response, Map<String, String> responseMap, boolean isOk) {
		String responseContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><success>" + responseMap.get("success") + "</success>";

		try {
			XMLBuilder root = XMLBuilder.create("Response");

			String[] keySetArray = responseMap.keySet().toArray(new String[0]);
			for (String key : keySetArray) {
				root.element(key).text(responseMap.get(key));
			}
			responseContent = root.asString();
		} catch (ParserConfigurationException | FactoryConfigurationError | TransformerException ex) {
			LOGGER.error("Could not send response XML.", ex);
		}

		if (!isOk) {
			try {
				response.sendError(Integer.parseInt(responseMap.get("serverCode")), responseContent);
			} catch (IOException ex) {
				LOGGER.warn("Possible error sending response data back to client", ex);
			}
		} else {
			sendResponse(response, ResponseType.XML.toString(), responseContent, null);
		}
	}

	static void sendJSONResponse(HttpServletResponse response, Map<String, String> responseMap, boolean isOk) {
		String responseContent = new Gson().toJson(responseMap);
		
		if (!isOk) {
			try {
				response.sendError(Integer.parseInt(responseMap.get("serverCode")), responseContent);
			} catch (IOException ex) {
				LOGGER.warn("Possible error sending response data back to client", ex);
			}
		} else {
			sendResponse(response, ResponseType.JSON.toString(), responseContent, null);
		}
		
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

		try (Writer writer = response.getWriter()) {
			writer.write(content);
		} catch (IOException ex) {
			LOGGER.warn("Possible error sending response data back to client", ex);
		}
	}
}
