package de.mayring.geoarena.service;

import java.io.IOException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import org.springframework.stereotype.Service;

import de.mayring.geoarena.GeoCredentials;
import de.mayring.geoarena.GeoProperties;
import de.mayring.geoarena.view.PlaceView;
import static de.mayring.geoarena.service.GeoJsonConverter.STD_GEOMETRY_FACTORY;

@Slf4j
@Service
public class HerePlacesReverseGeoCoder {

    private final String url;
    private final GeoJsonConverter geoJsonConverter;
    private final String fixedParams;

    public HerePlacesReverseGeoCoder(GeoProperties geoProperties, GeoCredentials credentials, GeoJsonConverter geoJsonConverter) {
	this.url = geoProperties.getGeocodeUrl();
	this.geoJsonConverter = geoJsonConverter;
	this.fixedParams = "&app_id=" + credentials.getAppId() + "&app_code=" + credentials.getAppCode() + "&cat=eat-drink,accommodation,going-out,sights-museums,leisure-outdoor,natural-geographical&cs=places";
    }

    /**
     * @param location the coordinate at which to search
     * @param description an optional textual description of the location
     * @return the Place nearest the given coordinate or null, if nothing was found
     */
    public PlaceView getPlaceForLocation(Coordinate location, String description) {
	try {
	    Resty resty = new Resty();
	    URI uri = URI.create(url + "discover/explore?" + atParameter(location)  + fixedParams);
	    JSONObject response = resty.json(uri).object();
	    JSONArray results = response.getJSONObject("results").getJSONArray("items");
	    if (results.length() > 0) {
		JSONObject resultObject = results.getJSONObject(0);

		JSONArray coordinates = resultObject.getJSONArray("position");
		double latitude = coordinates.getDouble(0);
		double longitude = coordinates.getDouble(1);
		Point point = STD_GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude)); // swap coordinates

		JSONObject firstCategory = resultObject.getJSONArray("categories").getJSONObject(0);
		String title = firstCategory.optString("title") + " " + resultObject.optString("title");
		String shape = geoJsonConverter.convertAndSerialize(point);
		return new PlaceView(shape, title, resultObject.optString("vicinity"));
	    }
	    else {
		Point point = STD_GEOMETRY_FACTORY.createPoint(location);
		String shape = geoJsonConverter.convertAndSerialize(point);
		return new PlaceView(shape, null, description);
	    }
	}
	catch (IOException | JSONException ex) {
	    log.error(location.toString(), ex);
	    throw new RuntimeException("ReverseGeocoding-Server not available", ex);
	}
    }

    private String atParameter(Coordinate searchAt) {
	return "at=" + searchAt.y + "," + searchAt.x + ";r=10500"; // swap coordinates (GeoJSON)
    }

}
