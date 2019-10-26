package de.mayring.geoarena.preloader;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import de.mayring.geoarena.service.GeoJsonConverter;
import de.mayring.geoarena.repo.CountryRepository;
import de.mayring.geoarena.repo.MountainRepository;

@Component
@Slf4j
public class DataPreloader {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private GeoJsonConverter geoJsonConverter;

    @Autowired
    private CountryRepository countryRepo;

    @Autowired
    private MountainRepository mountainRepo;

    @EventListener(ContextRefreshedEvent.class)
    public void doWheneverContextIsRefreshedOrInitialized() throws IOException {
	long start = System.currentTimeMillis();
	CountriesPreloader countriesPreloader = new CountriesPreloader(resourceLoader, geoJsonConverter, countryRepo);
	int count = countriesPreloader.preloadGeoJsonEntities("countries.geo.json");
	log.info("Preloaded " + count + " Countries in " + (System.currentTimeMillis() - start) + "ms");

	start = System.currentTimeMillis();
	MountainsPreloader mountainsPreloader = new MountainsPreloader(resourceLoader, geoJsonConverter, mountainRepo);
	count = mountainsPreloader.preloadGeoJsonEntities("mountains.geo.json");
	log.info("Preloaded " + count + " Mountains in " + (System.currentTimeMillis() - start) + "ms");
    }

}
