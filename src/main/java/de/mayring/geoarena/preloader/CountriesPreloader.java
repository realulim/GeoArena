package de.mayring.geoarena.preloader;

import lombok.extern.slf4j.Slf4j;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import org.geojson.Feature;
import org.springframework.core.io.ResourceLoader;

import de.mayring.geoarena.entity.Country;
import de.mayring.geoarena.repo.CountryRepository;
import de.mayring.geoarena.service.GeoJsonConverter;
import static de.mayring.geoarena.service.GeoJsonConverter.STD_GEOMETRY_FACTORY;

@Slf4j
public class CountriesPreloader extends AbstractEntityPreloader {

    private final CountryRepository countryRepo;

    public CountriesPreloader(ResourceLoader resourceLoader, GeoJsonConverter geoJsonConverter, CountryRepository countryRepo) {
	super(resourceLoader, geoJsonConverter);
	this.countryRepo = countryRepo;
    }

    @Override
    protected Geometry validateShape(Geometry geometry) {
	if (geometry instanceof MultiPolygon) {
	    return geometry;
	}
	else if (geometry instanceof Polygon) {
	    return new MultiPolygon(new Polygon[]{(Polygon) geometry}, STD_GEOMETRY_FACTORY);
	}
	else return null;
    }

    @Override
    protected void saveEntity(Feature feature, Geometry shape) {
	String countryName = feature.getProperty("name");
	Country country = new Country(countryName, (MultiPolygon)shape, feature.getProperty("iso_a3"));
	countryRepo.save(country);
    }

}
