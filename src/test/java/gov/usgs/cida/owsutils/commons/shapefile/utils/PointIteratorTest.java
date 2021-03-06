package gov.usgs.cida.owsutils.commons.shapefile.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollectionIterator;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.geotools.geometry.jts.JTSFactoryFinder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class PointIteratorTest {

    private GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

	@Test(expected=ClassCastException.class)
	public void testSinglePoint() {
		Point p = geometryFactory.createPoint(new Coordinate(4001.1, -9999.99));
		
		PointIterator pi = new PointIterator(p);
		
		Collection<Point> xpected = Collections.singleton(p);
		
		check(pi, xpected);
	}
	
	@Test
	public void testGeometryIterator(){
		Coordinate[] coordinates = new Coordinate[100];
		for (int i = 0; i < coordinates.length; i++) {
			coordinates[i] = new Coordinate(99.9+i, i/100.0);
		}
		
		LineString ls = geometryFactory.createLineString(coordinates);
		GeometryCollectionIterator gci;
		int ct;
		
		gci = new GeometryCollectionIterator(ls);
		
		ct = 0;
		while (gci.hasNext()) {
			Object thing = gci.next();
			ct++;
		}
		// This is what you get for applying a GeometryCollectionIterator to a non-collection geometry;
		// you get the initial object twice.
		// This is probably a bug in geotools -- should either barf at init, or return base type once.
		assertEquals(2, ct);	
		
		Point p = geometryFactory.createPoint(coordinates[0]);
		gci = new GeometryCollectionIterator(ls);
		
		ct = 0;
		while (gci.hasNext()) {
			Object thing = gci.next();
			ct++;
		}
		// as above
		assertEquals(2, ct);	
		
	}
	
	@Test
	public void testLineString() {
		Coordinate[] coordinates = new Coordinate[6];
		for (int i = 0; i < coordinates.length; i++) {
			coordinates[i] = new Coordinate(99.9+i, i/100.0);
		}
		
		Collection<Point> xpected = new ArrayList<>(coordinates.length);
		for (Coordinate coordinate : coordinates) {
			xpected.add(geometryFactory.createPoint(coordinate));
		}
		
		LineString ls = geometryFactory.createLineString(coordinates);
		
		PointIterator pi = new PointIterator(ls);

		check(pi, xpected);
	}

	@Test
	public void testMultiLineString() {
		Coordinate[] coordinates = new Coordinate[6];
		for (int i = 0; i < coordinates.length; i++) {
			coordinates[i] = new Coordinate(99.9+i, i/100.0);
		}
		
		Collection<Point> xpected = new ArrayList<>(coordinates.length);
		for (Coordinate coordinate : coordinates) {
			xpected.add(geometryFactory.createPoint(coordinate));
		}
		
		Coordinate[] cc1 = Arrays.copyOfRange(coordinates, 0, 4);
		LineString ls1 = geometryFactory.createLineString(cc1);
		Coordinate[] cc2 = Arrays.copyOfRange(coordinates, 4, coordinates.length);
		LineString ls2 = geometryFactory.createLineString(cc2);
		
		LineString[] lls = {ls1,ls2};
		
		MultiLineString mls = geometryFactory.createMultiLineString(lls);
		PointIterator pi = new PointIterator(mls);

		check(pi, xpected);
	}

	private void check(PointIterator pi, Collection<Point> xpected) {
		
		int ct = 0;
		while (pi.hasNext()) {
			Point x = pi.next();
			ct++;
			assertThat(xpected,hasItem(x));
		}
		
		assertThat(ct,is(xpected.size()));
	}

	
}
