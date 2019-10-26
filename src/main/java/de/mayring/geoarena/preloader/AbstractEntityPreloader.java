package de.mayring.geoarena.preloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import com.vividsolutions.jts.geom.Geometry;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import de.mayring.geoarena.service.GeoJsonConverter;

@Slf4j
public abstract class AbstractEntityPreloader {

    private final ResourceLoader resourceLoader;
    private final GeoJsonConverter geoJsonConverter;

    public AbstractEntityPreloader(ResourceLoader resourceLoader, GeoJsonConverter geoJsonConverter) {
	this.resourceLoader = resourceLoader;
	this.geoJsonConverter = geoJsonConverter;
    }

    public int preloadGeoJsonEntities(String fileName) throws IOException {
	FeatureCollection coll;
	int count = 0;
	Resource worldCountries = resourceLoader.getResource("classpath:database/" + fileName);
	try (BufferedReader buffer = new BufferedReader(new InputStreamReader(worldCountries.getInputStream()))) {
	    String geoJson = buffer.lines().collect(Collectors.joining("\n"));
	    coll = geoJsonConverter.deserializeFeatureCollection(geoJson);
	}
	for (Feature feature : coll.getFeatures()) {
	    Geometry geometry = geoJsonConverter.convert(feature.getGeometry());
	    Geometry finalShape = validateShape(geometry);
	    if (finalShape != null) {
		saveEntity(feature, finalShape);
		count++;
	    }
	    else {
		log.warn("Unparseable Shape for entity " + feature.toString());
	    }
	}
	return count;
    }

    protected abstract Geometry validateShape(Geometry geometry);

    protected abstract void saveEntity(Feature feature, Geometry shape);

}
