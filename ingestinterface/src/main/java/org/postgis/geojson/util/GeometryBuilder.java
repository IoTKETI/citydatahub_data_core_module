package org.postgis.geojson.util;

import java.util.ArrayList;
import java.util.List;

import org.postgis.LinearRing;
import org.postgis.Point;
import org.postgis.Polygon;

import kr.re.keti.sc.ingestinterface.common.code.Constants;

/**
 * Geometry Builder util class
 */
public class GeometryBuilder {

    public static Point[] createPoints(double[] points) {
        List<Point> result = new ArrayList<Point>();

        for (int i=0; i<points.length; i+=2) {
            result.add(new Point(points[i], points[i+1]));
        }

        return (Point[]) result.toArray();
    }

    public static Polygon createPolygon(Point[] points) {
        Polygon result = new Polygon(new LinearRing[] {new LinearRing(points)});
        result.setSrid(Constants.DEFAULT_SRID);
        return result;
    }

    public static Polygon createPolygon(Point[] points, int srid) {
        Polygon result = new Polygon(new LinearRing[] {new LinearRing(points)});
        result.setSrid(srid);
        return result;
    }

    public static Point createPoint(double x, double y) {
        Point point = new Point(x, y);
        point.setSrid(Constants.DEFAULT_SRID);
        return point;
    }

    public static Point createPoint(double x, double y, int srid) {
        Point point = new Point(x, y);
        point.setSrid(srid);
        return point;
    }

    public static Point createPoint3d(double x, double y, double z) {
        Point point = new Point(x, y, z);
        point.setSrid(Constants.DEFAULT_SRID);
        return point;
    }

    public static Point createPoint3d(double x, double y, double z, int srid) {
        Point point = new Point(x, y, z);
        point.setSrid(srid);
        return point;
    }
}
