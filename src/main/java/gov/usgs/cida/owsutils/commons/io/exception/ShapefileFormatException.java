package gov.usgs.cida.owsutils.commons.io.exception;

/**
 * Exception gets thrown when a file does not meet expected format standard Shapefile formats
 * 
 * @author thongsav
 *
 */
public class ShapefileFormatException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ShapefileFormatException(String message) {
		super(message);
	}
}
