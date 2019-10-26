package de.mayring.geoarena;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.shape.random.RandomPointsBuilder;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import de.mayring.geoarena.entity.Country;
import de.mayring.geoarena.repo.CountryRepository;
import de.mayring.geoarena.service.HerePlacesReverseGeoCoder;
import de.mayring.geoarena.view.PlaceView;
import static de.mayring.geoarena.service.GeoJsonConverter.STD_GEOMETRY_FACTORY;

@Slf4j
@Controller
public class FooterController {

    private final HerePlacesReverseGeoCoder reverseGeoCoder;
    private final CountryRepository countryRepo;

    public FooterController(HerePlacesReverseGeoCoder reverseGeoCoder,
			    CountryRepository countryRepo) {
	this.reverseGeoCoder = reverseGeoCoder;
	this.countryRepo = countryRepo;
    }

    @GetMapping("/random/place")
    @ResponseBody
    public PlaceView getRandomPlaceAjax() {
	List<Country> allCountries = countryRepo.findAll();
	Country randomCountry = allCountries.get(RandomUtils.nextInt(0, allCountries.size()));
	return reverseGeoCoder.getPlaceForLocation(getRandomPointInCountry(randomCountry), randomCountry.getName());
    }

    private Coordinate getRandomPointInCountry(Country country) {
	RandomPointsBuilder builder = new RandomPointsBuilder(STD_GEOMETRY_FACTORY);
	builder.setExtent(country.getShape());
	builder.setNumPoints(1);
	MultiPoint mp = (MultiPoint) builder.getGeometry();
	return new Coordinate(mp.getCoordinate().x, mp.getCoordinate().y);
    }

}
