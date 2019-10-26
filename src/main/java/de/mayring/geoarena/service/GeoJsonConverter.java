package de.mayring.geoarena.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.geojson.GeoJsonReader;
import com.vividsolutions.jts.io.geojson.GeoJsonWriter;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GeoJsonConverter {

    private static final PrecisionModel PRECISION_MODEL_8 = new PrecisionModel(100000000);
    public static final GeometryFactory STD_GEOMETRY_FACTORY = new GeometryFactory(PRECISION_MODEL_8, 4326);

    public GeoJsonConverter() {
    }

    /**
     * @param geoJson a GeoJsonObject
     * @return the corresponding JTS Geometry object
     */
    public Geometry convert(GeoJsonObject geoJson) {
	ObjectMapper mapper = new ObjectMapper();
	String geoJsonStr;
	try {
	    geoJsonStr = mapper.writeValueAsString(geoJson);
	}
	catch (JsonProcessingException ex) {
	    log.error(geoJson.toString(), ex);
	    throw new RuntimeException();
	}
	return convert(geoJsonStr);
    }

    /**
     * @param geometry a JTS Geometry object
     * @return the corresponding GeoJsonObject
     */
    public GeoJsonObject convert(Geometry geometry) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return mapper.readValue(convertAndSerialize(geometry), GeoJsonObject.class);
        }
        catch (IOException ex) {
            // should never happen as we are reading from a string
	    log.error(geometry.toText(), ex);
	    throw new RuntimeException(geometry.toText(), ex);
        }
    }
    
    /**
     * Converts textual GeoJson to a JTS Geometry object and reduces precision to 8 decimal places.
     *
     * @param geoJson textual GeoJson
     * @return the corresponding JTS Geometry object with precision reduced to 8 decimal places
     */
    public Geometry convert(String geoJson) {
	GeoJsonReader reader = new GeoJsonReader();
	try {
	    Geometry geometry = reader.read(geoJson);
	    GeometryPrecisionReducer precisionReducer = new GeometryPrecisionReducer(PRECISION_MODEL_8);
	    return precisionReducer.reduce(geometry);
	}
	catch (ParseException ex) {
	    log.error(geoJson, ex);
	    throw new RuntimeException(ex);
	}
    }

    /**
     * @param featureCollectionAsGeoJson a textual representation of a GeoJson FeatureCollection
     * @return the deserialized FeatureCollection
     */
    public FeatureCollection deserializeFeatureCollection(String featureCollectionAsGeoJson) {
	try {
	    InputStream in = new ByteArrayInputStream(featureCollectionAsGeoJson.getBytes("utf-8"));
	    ObjectMapper objectMapper = new ObjectMapper();
	    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	    return objectMapper.readValue(in, FeatureCollection.class);
	}
	catch (IOException ex) {
	    // should never happen as we are reading from a string
	    log.error(featureCollectionAsGeoJson, ex);
	    throw new RuntimeException(ex);
	}
    }

    /**
     * @param geometry a JTS Geometry object
     * @return the textual GeoJson corresponding to the geometry object
     */
    public String convertAndSerialize(Geometry geometry) {
        GeoJsonWriter writer = new GeoJsonWriter();
        return writer.write(geometry);
    }

}
