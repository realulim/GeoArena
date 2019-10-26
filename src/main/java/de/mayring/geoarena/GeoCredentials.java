package de.mayring.geoarena;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "here")
@PropertySource("classpath:here.properties")
public class GeoCredentials {
    private String appId;
    private String appCode;
}
