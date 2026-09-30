package com.naviSafe.naviSafe.domain.Navigation3D.repository;

import com.naviSafe.naviSafe.domain.Navigation3D.dto.BuildingGeometry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BuildingRepository {

    @PersistenceContext(unitName = "postgresEntityManager")
    private EntityManager postgresEntityManager;

    public List<BuildingGeometry> findBuildings(
            double longitude,
            double latitude,
            double radius
    ){

        String sql = """
            SELECT
                height,
                ST_AsGeoJSON(geom) AS geom
            FROM building
            WHERE ST_DWithin(
                geom::geography,
                ST_SetSRID(
                    ST_MakePoint(:longitude, :latitude),
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
                .map(row -> {
                        Double height = row[0] != null ? Double.valueOf(row[0].toString()) : null;

                        return new BuildingGeometry(
                                height,
                                row[1].toString()
                        );
                    })
                .toList();
    }
}
