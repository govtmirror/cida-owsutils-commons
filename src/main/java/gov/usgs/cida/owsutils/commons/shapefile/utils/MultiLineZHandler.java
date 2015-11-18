package gov.usgs.cida.owsutils.commons.shapefile.utils;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import org.geotools.data.shapefile.shp.JTSUtilities;
import org.geotools.data.shapefile.shp.ShapeHandler;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.data.shapefile.shp.ShapefileException;

/*
 * $Id$ 
 * @author aaime 
 * @author Ian Schneider
 */
/**
 * The default JTS handler for shapefile. Currently uses the default JTS
 * GeometryFactory, since it doesn't seem to matter.
 *
 *
 *
 * @source $URL$
 */
public class MultiLineZHandler implements ShapeHandler {

	final ShapeType shapeType;

	GeometryFactory geometryFactory;

	double[] xy;

	double[] z;

	/**
	 * Create a MultiLineHandler for ShapeType.ARC
	 * @param gf
	 */
	public MultiLineZHandler(GeometryFactory gf) {
		shapeType = ShapeType.ARC;
		this.geometryFactory = gf;
	}

	/**
	 * Create a MultiLineHandler for one of: <br>
	 * ShapeType.ARC,ShapeType.ARCM,ShapeType.ARCZ
	 *
	 * @param type The ShapeType to use.
	 * @param gf
	 * @throws ShapefileException If the ShapeType is not correct (see
	 * constructor).
	 */
	public MultiLineZHandler(ShapeType type, GeometryFactory gf) throws ShapefileException {
		if ((type != ShapeType.ARC) && (type != ShapeType.ARCM)
				&& (type != ShapeType.ARCZ)) {
			throw new ShapefileException(
					"MultiLineHandler constructor - expected type to be 3,13 or 23");
		}

		shapeType = type;
		this.geometryFactory = gf;
	}

	/**
	 * Get the type of shape stored
	 * (ShapeType.ARC,ShapeType.ARCM,ShapeType.ARCZ)
	 */
	public ShapeType getShapeType() {
		return shapeType;
	}

	/**
	 *
	 */
	public int getLength(Object geometry) {
		MultiLineString multi = (MultiLineString) geometry;

		int numlines;
		int numpoints;
		int length;

		numlines = multi.getNumGeometries();
		numpoints = multi.getNumPoints();

		if (shapeType == ShapeType.ARC) {
			length = 44 + (4 * numlines) + (numpoints * 16);
		} else if (shapeType == ShapeType.ARCM) {
			length = 44 + (4 * numlines) + (numpoints * 16) + 8 + 8
					+ (8 * numpoints);
		} else if (shapeType == ShapeType.ARCZ) {
			length = 44 + (4 * numlines) + (numpoints * 16) + 8 + 8
					+ (8 * numpoints) + 8 + 8 + (8 * numpoints);
		} else {
			throw new IllegalStateException("Expected ShapeType of Arc, got "
					+ shapeType);
		}

		return length;
	}

	private Object createNull() {
		return geometryFactory.createMultiLineString((LineString[]) null);
	}

	@Override
	public Object read(ByteBuffer buffer, ShapeType type, boolean flatGeometry) {
		if (type == ShapeType.NULL) {
			return createNull();
		}
		int dimensions = (shapeType == ShapeType.ARCZ && !flatGeometry) ? 3 : 2;
		// read bounding box (not needed)
		buffer.position(buffer.position() + 4 * 8);

		int numParts = buffer.getInt();
		int numPoints = buffer.getInt(); // total number of points

		int[] partOffsets = new int[numParts];

		// points = new Coordinate[numPoints];
		for (int i = 0; i < numParts; i++) {
			partOffsets[i] = buffer.getInt();
		}
		// read the first two coordinates and start building the coordinate
		// sequences
		CoordinateSequence[] lines = new CoordinateSequence[numParts];
		int finish, start = 0;
		int length = 0;
		boolean clonePoint;
		final DoubleBuffer doubleBuffer = buffer.asDoubleBuffer();
		for (int part = 0; part < numParts; part++) {
			start = partOffsets[part];

			if (part == (numParts - 1)) {
				finish = numPoints;
			} else {
				finish = partOffsets[part + 1];
			}

			length = finish - start;
			int xyLength = length;
			if (length == 1) {
				length = 2;
				clonePoint = true;
			} else {
				clonePoint = false;
			}

			CoordinateSequence cs = geometryFactory.getCoordinateSequenceFactory().create(length, dimensions);
			double[] xy = new double[xyLength * 2];
			doubleBuffer.get(xy);
			for (int i = 0; i < xyLength; i++) {
				cs.setOrdinate(i, 0, xy[i * 2]);
				cs.setOrdinate(i, 1, xy[i * 2 + 1]);
			}

			if (clonePoint) {
				cs.setOrdinate(1, 0, cs.getOrdinate(0, 0));
				cs.setOrdinate(1, 1, cs.getOrdinate(0, 1));
			}

			lines[part] = cs;
		}

		// if we have Z dimension, add to all the points
		if (shapeType == ShapeType.ARCZ) {
			// z min, max
			readExtraDimension(numParts, numPoints, partOffsets, lines,
					doubleBuffer, 2);
		}

		// ArcM and ArcZ both have measure dimension (trailing)
		if (shapeType == ShapeType.ARCZ || shapeType == ShapeType.ARCM) {
			readExtraDimension(numParts, numPoints, partOffsets, lines,
					doubleBuffer, 3);
		}

		// Prepare line strings and return the multilinestring
		LineString[] lineStrings = new LineString[numParts];
		for (int part = 0; part < numParts; part++) {
			lineStrings[part] = geometryFactory.createLineString(lines[part]);
		}

		return geometryFactory.createMultiLineString(lineStrings);
	}

	protected void readExtraDimension(int numParts, int numPoints,
			int[] partOffsets, CoordinateSequence[] lines,
			final DoubleBuffer doubleBuffer, int ordIdx) {
		int finish;
		int start;
		int length;
		// skip the range
		doubleBuffer.position(doubleBuffer.position() + 2);

		for (int part = 0; part < numParts; part++) {
			start = partOffsets[part];

			if (part == (numParts - 1)) {
				finish = numPoints;
			} else {
				finish = partOffsets[part + 1];
			}

			length = finish - start;
			if (length == 1) {
				length = 2;
			}

			double[] z = new double[length];
			doubleBuffer.get(z);
			for (int i = 0; i < length; i++) {
				double value = z[i];
				// Any floating point number smaller than –10e38 is considered by a shapefile reader to represent a "no data" value.
				if (value < -1.0e38) {
					value = Double.NaN;
				}
				lines[part].setOrdinate(i, ordIdx, value);
			}
		}
	}

	@Override
	public void write(ByteBuffer buffer, Object geometry) {
		MultiLineString multi = (MultiLineString) geometry;

		Envelope box = multi.getEnvelopeInternal();
		buffer.putDouble(box.getMinX());
		buffer.putDouble(box.getMinY());
		buffer.putDouble(box.getMaxX());
		buffer.putDouble(box.getMaxY());

		final int numParts = multi.getNumGeometries();
		final CoordinateSequence[] lines = new CoordinateSequence[numParts];
		final double[] zExtreame = {Double.NaN, Double.NaN};
		final int npoints = multi.getNumPoints();

		buffer.putInt(numParts);
		buffer.putInt(npoints);

		{
			int idx = 0;
			for (int i = 0; i < numParts; i++) {
				lines[i] = ((LineString) multi.getGeometryN(i)).getCoordinateSequence();
				buffer.putInt(idx);
				idx += lines[i].size();
			}
		}

		for (CoordinateSequence coords : lines) {
			if (shapeType == ShapeType.ARCZ) {
				JTSUtilities.zMinMax(coords, zExtreame);
			}
			final int ncoords = coords.size();

			for (int t = 0; t < ncoords; t++) {
				buffer.putDouble(coords.getX(t));
				buffer.putDouble(coords.getY(t));
			}
		}

		if (shapeType == ShapeType.ARCZ) {
			if (Double.isNaN(zExtreame[0])) {
				buffer.putDouble(0.0);
				buffer.putDouble(0.0);
			} else {
				buffer.putDouble(zExtreame[0]);
				buffer.putDouble(zExtreame[1]);
			}

			for (CoordinateSequence coords : lines) {
				final int ncoords = coords.size();
				double z;
				for (int t = 0; t < ncoords; t++) {
					z = coords.getOrdinate(t, 2);
					if (Double.isNaN(z)) {
						buffer.putDouble(0.0);
					} else {
						buffer.putDouble(z);
					}
				}
			}

			buffer.putDouble(-10E40);
			buffer.putDouble(-10E40);

			for (int t = 0; t < npoints; t++) {
				buffer.putDouble(-10E40);
			}
		}
	}

}
