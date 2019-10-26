package de.mayring.geoarena;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "geo")
public class GeoProperties {
    private String geocodeUrl;
    private String reverseGeocodeUrl;
    private String routingUrl;
}
