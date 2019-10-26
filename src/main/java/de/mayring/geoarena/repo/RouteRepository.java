package de.mayring.geoarena.repo;

import java.util.List;

import com.vividsolutions.jts.geom.Point;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.mayring.geoarena.entity.Route;

public interface RouteRepository extends JpaRepository<Route, Long> {

    @Query(value = "select distinct r.* from Route r where ST_DWithin(" +
			"cast(ST_SetSRID(:location,4326) as geography), " +
			"cast(ST_SetSRID(r.shape,4326) as geography), :buffer)", nativeQuery = true)
    List<Route> findRoutesNearLocation(@Param("location") Point location, @Param("buffer") int buffer);

}
