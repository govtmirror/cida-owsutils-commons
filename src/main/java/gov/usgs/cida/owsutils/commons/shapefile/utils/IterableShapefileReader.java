package gov.usgs.cida.owsutils.commons.shapefile.utils;

import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapeHandler;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileReader.Record;
import org.slf4j.LoggerFactory;

/**
 * Read a shapefile, including the M values (which the usual ShpaefileDataStore
 * discards).
 *
 * @author rhayes
 *
 */
public class IterableShapefileReader implements Iterable<ShapeAndAttributes>, Iterator<ShapeAndAttributes>, AutoCloseable {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(IterableShapefileReader.class);
	private ShapefileReader rdr;
	private DbaseFileReader dbf;
	public boolean initialized = false;
	private File file;
	private ShapeHandler shapeHandler;
	private ShpFiles shapeFiles;

	public IterableShapefileReader(ShpFiles shapeFiles, ShapeHandler shapeHandler) {
		if (shapeHandler == null) {
			throw new IllegalArgumentException("A ShapeHandler is required");
		}
		this.shapeHandler = shapeHandler;
		this.shapeFiles = shapeFiles;
		init();
	}

	public IterableShapefileReader(ShpFiles shapeFiles) {
		this.shapeFiles = shapeFiles;
		init();
	}

	public DbaseFileHeader getDbfHeader() {
		return dbf.getHeader();
	}

	public ShpFiles getShpFiles() {
		return shapeFiles;
	}

	private void init() {
		initialized = false;

		try {
			CoordinateSequenceFactory coordSeqFactory = com.vividsolutions.jtsexample.geom.ExtendedCoordinateSequenceFactory.instance();
			GeometryFactory gf = new GeometryFactory(coordSeqFactory);
			
			rdr = new ShapefileReader(shapeFiles, false, true, gf);
			rdr.setHandler(this.shapeHandler);

			Charset charset = Charset.defaultCharset();
			dbf = new DbaseFileReader(shapeFiles, false, charset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized Iterator<ShapeAndAttributes> iterator() {
		if (initialized) {
			init();
		}
		return this;
	}

	@Override
	public synchronized boolean hasNext() {
		try {
			return rdr.hasNext();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized ShapeAndAttributes next() {
		initialized = true;
		try {
			Record rec = rdr.nextRecord();
			DbaseFileReader.Row row = dbf.readRow();
			return new ShapeAndAttributes(rec, row);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Nope, sorry");
	}

	@Override
	public void close() {
		try {
			rdr.close();
		} catch (IOException ex) {
			LOGGER.warn("Could not close ShapefileReader", ex);
		}
		try {
			dbf.close();
		} catch (IOException ex) {
			LOGGER.warn("Could not close DBaseFileReader", ex);
		}
		shapeFiles.dispose();
	}
}
