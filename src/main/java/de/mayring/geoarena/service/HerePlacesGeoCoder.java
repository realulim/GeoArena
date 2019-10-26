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
import org.thymeleaf.util.Validate;

import de.mayring.geoarena.GeoCredentials;
import de.mayring.geoarena.GeoProperties;
import static de.mayring.geoarena.service.GeoJsonConverter.STD_GEOMETRY_FACTORY;

@Slf4j
@Service
public class HerePlacesGeoCoder {

    private final String url;
    private final String fixedParams;
    private final Coordinate defaultSearchAt = new Coordinate(46.8, 11.9);

    public HerePlacesGeoCoder(GeoProperties geoProperties, GeoCredentials credentials) {
	this.url = geoProperties.getGeocodeUrl();
	Validate.notNull(credentials.getAppId(), "AppId is null - please provide Credentials");
	Validate.notNull(credentials.getAppId(), "AppCode is null - please provide Credentials");
	this.fixedParams = "&app_id=" + credentials.getAppId() + "&app_code=" + credentials.getAppCode() + "&tf=plain";
    }

    /**
     * @param query the search term
     * @param searchAt optional vicinity to search in, default is Munich
     * @return the location as GeoJson coordinate (Lat/Lon swapped)
     */
    public Point getLocationForFreeformQuery(String query, Coordinate searchAt) {
	if (searchAt == null) {
	    searchAt = defaultSearchAt;
	}
	try {
	    Resty resty = new Resty();
	    URI uri = URI.create(url + "discover/search?q=" + Resty.enc(query) + fixedParams + atParameter(searchAt));
	    JSONArray results = resty.json(uri).object().getJSONObject("results").getJSONArray("items");
	    Point point = null;
	    if (results.length() > 0) {
		JSONObject resultObject = results.getJSONObject(0);
		JSONArray coordinates = resultObject.getJSONArray("position");
		double latitude = coordinates.getDouble(0);
		double longitude = coordinates.getDouble(1);
		point = STD_GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude)); // swap coordinates
		point.setUserData(resultObject.optString("title"));
	    }
	    return point;
	}
	catch (IOException | JSONException ex) {
	    log.error(query, ex);
	    throw new RuntimeException("Geocoding-Server not available", ex);
	}
    }

    private String atParameter(Coordinate searchAt) {
	return "&at=" + searchAt.x + "," + searchAt.y;
    }

}
