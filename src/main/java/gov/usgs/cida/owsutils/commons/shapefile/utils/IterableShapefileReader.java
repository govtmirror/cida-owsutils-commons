package gov.usgs.cida.owsutils.commons.shapefile.utils;

import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
	public boolean used = false;
	private File file;
	private ShpFiles shpFile;
	private ShapeHandler shapeHandler;

	public IterableShapefileReader(String fn, ShapeHandler shapeHandler) {
		if (shapeHandler == null) {
			throw new IllegalArgumentException("A ShapeHandler is required");
		}
		this.shapeHandler = shapeHandler;
		init(new File(fn + ".shp"));
	}

	public IterableShapefileReader(File f) {
		init(f);
	}

	public DbaseFileHeader getDbfHeader() {
		return dbf.getHeader();
	}

	public ShpFiles getShpFiles() {
		return shpFile;
	}

	private void init(File f) {
		file = f;

		used = false;

		try {
			shpFile = new ShpFiles(file);
			CoordinateSequenceFactory x = com.vividsolutions.jtsexample.geom.ExtendedCoordinateSequenceFactory.instance();
			GeometryFactory gf = new GeometryFactory(x);

			rdr = new ShapefileReader(shpFile, false, false, gf);
			rdr.setHandler(this.shapeHandler);

			Charset charset = Charset.defaultCharset();
			dbf = new DbaseFileReader(shpFile, false, charset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized Iterator<ShapeAndAttributes> iterator() {
		if (used) {
			init(file);
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
		used = true;
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
			// Skip
		}
		try {
			dbf.close();
		} catch (IOException ex) {
			// Skip
		}
		shpFile.dispose();
	}
}
