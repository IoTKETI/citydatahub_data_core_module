package org.postgis.geojson;

import org.postgis.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.postgis.geojson.deserializers.GeometryDeserializer;
import org.postgis.geojson.serializers.GeometrySerializer;

/**
 * Module for loading serializers/deserializers.
 * 
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 * @author Sebastien Deleuze
 */
public class PostGISModule extends SimpleModule {
    private static final long serialVersionUID = 1L;

    public PostGISModule() {
        super("PostGISModule");

        addSerializer(Geometry.class, new GeometrySerializer());
        addDeserializer(Geometry.class, new GeometryDeserializer());
        addDeserializer(Point.class, new GeometryDeserializer());
        addDeserializer(Polygon.class, new GeometryDeserializer());
        addDeserializer(LineString.class, new GeometryDeserializer());
        addDeserializer(MultiPolygon.class, new GeometryDeserializer());
        addDeserializer(MultiPoint.class, new GeometryDeserializer());
        addDeserializer(MultiLineString.class, new GeometryDeserializer());
//        addDeserializer(GeometryCollection.class, new GeometryDeserializer());
    }
}
