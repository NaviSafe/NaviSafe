package com.naviSafe.naviSafe.domain.Navigation3D.repository;

import com.naviSafe.naviSafe.domain.Navigation3D.dto.RoadGeometry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoadRepository {
    @PersistenceContext(unitName = "postgresEntityManager")
    private EntityManager postgresEntityManager;

    public List<RoadGeometry> findRoads(
            double longitude,
            double latitude,
            double radius
    ) {
        String sql = """
            SELECT
                 id, 
                 ST_AsGeoJSON(geom) AS geom
             FROM edge_base
             WHERE
                 geom && ST_Expand(
                     ST_SetSRID(
                         ST_MakePoint(
                             :longitude,
                             :latitude
                         ),
                         4326
                     ),
                     0.0012,
                     0.001
                 )
                 AND ST_DWithin(
                     geom::geography,
                     ST_SetSRID(
                         ST_MakePoint(
                             :longitude,
                             :latitude
                         ),
                         4326
                     )::geography,
                     :radius
                 )
            """;

        List<Object[]> results = postgresEntityManager
                .createNativeQuery(sql)
                .setParameter("longitude", longitude)
                .setParameter("latitude", latitude)
                .setParameter("radius", radius)
                .getResultList();

        return results.stream()
                .map(row -> new RoadGeometry(
                        Long.parseLong(String.valueOf(row[0])),
                        (String) row[1]
                ))
                .toList();
    }
}


