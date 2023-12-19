package org.postgis.geojson;

/**
 * GeoJSON supported types.
 */
public interface GeometryTypes {
    String POINT               = "Point";
    String LINE_STRING         = "LineString";
    String POLYGON             = "Polygon";
    String MULTI_POINT         = "MultiPoint";
    String MULTI_LINE_STRING   = "MultiLineString";
    String MULTI_POLYGON       = "MultiPolygon";
    String GEOMETRY_COLLECTION = "GeometryCollection";
}
