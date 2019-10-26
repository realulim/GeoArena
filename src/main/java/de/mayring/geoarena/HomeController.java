package de.mayring.geoarena;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriUtils;
import org.thymeleaf.util.StringUtils;

import de.mayring.geoarena.entity.Country;
import de.mayring.geoarena.entity.Route;
import de.mayring.geoarena.repo.CountryRepository;
import de.mayring.geoarena.repo.MountainRepository;
import de.mayring.geoarena.repo.RouteRepository;
import de.mayring.geoarena.service.GeoJsonConverter;
import de.mayring.geoarena.service.HerePlacesGeoCoder;
import de.mayring.geoarena.view.MountainView;
import de.mayring.geoarena.view.PlaceView;
import de.mayring.geoarena.view.RouteView;

@Slf4j
@Controller
public class HomeController {

    private final HerePlacesGeoCoder geoCoder;
    private final GeoJsonConverter geoJsonConverter;
    private final CountryRepository countryRepo;
    private final MountainRepository mountainRepo;
    private final RouteRepository routeRepo;

    private boolean showMountains = true;
    private static final int MOUNTAINS_ALONG_ROUTE_BUFFER = 20000;
    private static final int ROUTES_NEAR_LOCATION_BUFFER = 10000;

    public HomeController(HerePlacesGeoCoder geoCoder, GeoJsonConverter geoJsonConverter,
			  CountryRepository countryRepo, MountainRepository mountainRepo, RouteRepository routeRepo) {
	this.geoCoder = geoCoder;
	this.geoJsonConverter = geoJsonConverter;
	this.countryRepo = countryRepo;
	this.mountainRepo = mountainRepo;
	this.routeRepo = routeRepo;
    }

    @GetMapping({"/", "/search"})
    public String homePage() {
	return "index";
    }

    @PostMapping("/search/places")
    public String searchPlaces(@RequestParam(name = "p", required = false) String query) {
	if (StringUtils.isEmptyOrWhitespace(query)) {
	    return homePage();
	}
	else {
	    return "redirect:/search/places/" + UriUtils.encodeQueryParam(query, "utf-8");
	}
    }

    @PostMapping("/search/routes")
    public String searchRoutes(@RequestParam(name = "r", required = false) String query) {
	if (StringUtils.isEmptyOrWhitespace(query)) {
	    return homePage();
	}
	else {
	    return "redirect:/search/routes/" + UriUtils.encodeQueryParam(query, "utf-8");
	}
    }

    @GetMapping("/search/{type}/{query}")
    public String search(@PathVariable String type, @PathVariable String query, Model model) {
	Point location = geoCoder.getLocationForFreeformQuery(query, null);
	if (location != null) {
	    if (type.equals("places")) {
		searchForPlaces(location, query, model);
	    }
	    else if (type.equals("routes")) {
		searchForRoutes(location, query, model);
	    }

	    String shape = geoJsonConverter.convertAndSerialize(location);
	    model.addAttribute("foundLocation", new PlaceView(shape, location.getUserData().toString(), ""));
	    model.addAttribute("showMountains", showMountains);
	}
	else {
	    log.warn("Nothing found: " + query);
	}
	return "index";
    }

    private void searchForPlaces(Point location, String query, Model model) {
	List<Country> countries = countryRepo.findWithin(location);
	if (countries.size() > 0) {
	    // add found country
	    MultiPolygon countryShape = countries.get(0).getShape();
	    model.addAttribute("country", geoJsonConverter.convert(countryShape));
	    model.addAttribute("mountains", addMountainsInCountry(countryShape));
	    model.addAttribute("placesQuery", query);
	}
	else {
	    log.error("Coordinate outside any country: " + location.toString());
	}
    }

    // add all routes that pass within a certain distance of the found location
    private void searchForRoutes(Point location, String query, Model model) {
	List<Route> routes = routeRepo.findRoutesNearLocation(location, ROUTES_NEAR_LOCATION_BUFFER);
	if (routes.size() > 0) {
	    model.addAttribute("routes", routes.stream().map(route -> {
		return new RouteView(geoJsonConverter.convertAndSerialize(route.getShape()), route.getName());
	    }).collect(Collectors.toList()));
	    model.addAttribute("mountains", addMountainsAlongRoutes(routes));
	}
	model.addAttribute("routesQuery", query);
    }

    @PostMapping("/config/showmountains/{value}")
    @ResponseBody
    public void configureMountainLayer_Ajax(@PathVariable String value) {
	if (! StringUtils.isEmptyOrWhitespace(value)) {
	    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on")) this.showMountains = true;
	    else this.showMountains = false;
	}
    }

    // add all mountains located in found country
    private List<MountainView> addMountainsInCountry(MultiPolygon countryShape) {
	return mountainRepo.findMountainsWithinCountry(countryShape).stream().map(mountain -> {
	    String shape = geoJsonConverter.convertAndSerialize(mountain.getShape());
	    return new MountainView(shape, mountain.getName(), mountain.getHeight());
	}).collect(Collectors.toList());
    }

    // add all mountains within a certain distance from a route
    private List<MountainView> addMountainsAlongRoutes(List<Route> routes) {
	List<MountainView> mountains = new ArrayList<>();
	for (Route route : routes) {
	    mountains.addAll(mountainRepo.findMountainsAlongRoute(route.getId(), MOUNTAINS_ALONG_ROUTE_BUFFER).stream().map(mountain -> {
		String shape = geoJsonConverter.convertAndSerialize(mountain.getShape());
		return new MountainView(shape, mountain.getName(), mountain.getHeight());
	    }).collect(Collectors.toList()));
	}
	return mountains;
    }

    @PostMapping("/route/save")
    @ResponseBody
    public boolean saveRoute(@RequestParam String shape, @RequestParam String name) {
	Route route = new Route();
	route.setName(name);
	route.setShape((LineString)geoJsonConverter.convert(shape));
	Route savedRoute = routeRepo.save(route);
	return savedRoute != null;
    }

}
