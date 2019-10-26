package de.mayring.geoarena.preloader;

import lombok.extern.slf4j.Slf4j;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.geojson.Feature;
import org.springframework.core.io.ResourceLoader;

import de.mayring.geoarena.entity.Mountain;
import de.mayring.geoarena.repo.MountainRepository;
import de.mayring.geoarena.service.GeoJsonConverter;

@Slf4j
public class MountainsPreloader extends AbstractEntityPreloader {

    private final MountainRepository mountainRepo;

    public MountainsPreloader(ResourceLoader resourceLoader, GeoJsonConverter geoJsonConverter, MountainRepository mountainRepo) {
	super(resourceLoader, geoJsonConverter);
	this.mountainRepo = mountainRepo;
    }

    @Override
    protected Geometry validateShape(Geometry geometry) {
	if (geometry instanceof Point) return geometry;
	else return null;
    }

    @Override
    protected void saveEntity(Feature feature, Geometry shape) {
	String mountainName = feature.getProperty("name");
	int mountainHeight = feature.getProperty("elevation");
	Mountain mountain = new Mountain(mountainName, mountainHeight, (Point)shape);
	mountainRepo.save(mountain);
    }

}
