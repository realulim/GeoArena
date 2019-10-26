package de.mayring.geoarena.repo;

import java.util.List;

import com.vividsolutions.jts.geom.MultiPolygon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.mayring.geoarena.entity.Mountain;

public interface MountainRepository extends JpaRepository<Mountain, Long> {

    @Query(value = "select m.* from Mountain m where ST_Within(m.shape, :countryShape)", nativeQuery = true)
    List<Mountain> findMountainsWithinCountry(@Param("countryShape") MultiPolygon countryShape);

    @Query(value = "select m.* from Mountain m, Route r where r.id = :routeId " +
			"and ST_DWithin(" +
				"cast(ST_SetSRID(m.shape,4326) as geography), " +
				"cast(ST_SetSRID(r.shape,4326) as geography), :buffer)", nativeQuery = true)
    List<Mountain> findMountainsAlongRoute(@Param("routeId") long routeId, @Param("buffer") int buffer);

}
