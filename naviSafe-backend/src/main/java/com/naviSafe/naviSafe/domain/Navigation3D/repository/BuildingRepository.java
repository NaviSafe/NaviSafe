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
                  grnd_flr,
                  ST_AsGeoJSON(geom) AS geom
              FROM building
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
                .map(row -> {
                    double height = row[0] == null
                            ? 0.0
                            : Double.parseDouble(String.valueOf(row[0]));

                    double groundFloor = row[1] == null
                            ? 0.0
                            : Double.parseDouble(String.valueOf(row[1]));

                    if (height == 0.0) {
                        height = groundFloor * 3.5;
                    }

                    return new BuildingGeometry(
                            height,
                            (String) row[2]
                    );
                })
                .toList();
    }
}
