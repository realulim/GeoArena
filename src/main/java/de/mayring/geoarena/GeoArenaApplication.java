package de.mayring.geoarena;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ GeoProperties.class })
public class GeoArenaApplication {

    public static void main(String[] args) {
	SpringApplication.run(GeoArenaApplication.class, args);
    }

}
