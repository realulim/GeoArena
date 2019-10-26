package de.mayring.geoarena.repo;

import java.util.List;

import com.vividsolutions.jts.geom.Point;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.mayring.geoarena.entity.Country;

public interface CountryRepository extends JpaRepository<Country, Long> {

    /**
     * @param isoa3Code a country code
     * @return the country with this country code or null, if no country has this country code
     */
    Country findByIsoa3Code(String isoa3Code);

    /**
     * @param point a coordinate
     * @return all countries that this point is located in (usually just one)
     */
    @Query(value = "select c.* from Country c where ST_Within(:point, c.shape) order by ST_Area(c.shape) asc", nativeQuery = true)
    List<Country> findWithin(@Param("point") Point point);

}
