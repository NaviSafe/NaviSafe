package com.naviSafe.naviSafe.domain.MyRootPath.v2.repository;

import com.naviSafe.naviSafe.domain.MyRootPath.v1.service.Point;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class RouteRepository {

    @PersistenceContext(unitName = "postgresEntityManager")
    private EntityManager em;

    @Transactional(transactionManager = "postgresTransactionManager")
    public List<Point> findRoute(
            double startLon,
            double startLat,
            double endLon,
            double endLat,
            List<Point> accidentPoints
    ) {

        System.out.println("=== findRoute 시작 ===");
        System.out.println("사고 좌표 개수: " + accidentPoints.size());

        // 1️⃣ 임시 테이블 생성
        String createAccidentTable = """
            CREATE TEMP TABLE accident_points (
                geom geometry(POINT, 4326)
            ) ON COMMIT DROP
        """;
        em.createNativeQuery(createAccidentTable).executeUpdate();
        System.out.println("임시 사고 테이블 생성 완료");

        // 2️⃣ 배치 insert
        for (Point p : accidentPoints) {
            String insertSql = "INSERT INTO accident_points (geom) VALUES (ST_SetSRID(ST_MakePoint(:lon, :lat), 4326))";
            em.createNativeQuery(insertSql)
                    .setParameter("lon", p.lon())
                    .setParameter("lat", p.lat())
                    .executeUpdate();
        }
        System.out.println("사고 좌표 insert 완료");

        String createIndex = "CREATE INDEX idx_accident_geom ON accident_points USING GIST(geom)";
        em.createNativeQuery(createIndex).executeUpdate();

        // 3️⃣ edge 임시 테이블 생성
        String createTempEdge = """
            CREATE TEMP TABLE edge
            ON COMMIT DROP
            AS
            SELECT *
            FROM edge_base
        """;
        em.createNativeQuery(createTempEdge).executeUpdate();
        System.out.println("Edge 임시 테이블 생성 완료");

        // edge 업데이트 (BBOX + 정확 거리)
        String updateAccidentCost = """
            UPDATE edge e
            SET cost = -1
            FROM accident_points ap
            WHERE e.geom && ST_Expand(ap.geom, 0.0005)  -- 약 50m bbox 필터
              AND ST_DWithin(e.geom, ap.geom, 0.0005)   -- 실제 거리 계산
        """;
        em.createNativeQuery(updateAccidentCost).executeUpdate();
        System.out.println("사고 비용 업데이트 완료");

        // 5️⃣ 회전 제한 업데이트
        String updateTurnRestriction = """
            UPDATE edge e
            SET cost = -1
            FROM node n
            JOIN turninfo ti
              ON n.node_id = ti.node_id
            WHERE e.source = n.node_id::bigint
              AND n.turn_p = '1'
              AND ti.turn_type = '011'
        """;
        em.createNativeQuery(updateTurnRestriction).executeUpdate();
        System.out.println("회전 제한 업데이트 완료");

        // 6️⃣ A* 경로 조회
        String routeSql = """
            WITH start_node AS (
                SELECT node_id AS id
                FROM node
                ORDER BY ST_DistanceSphere(
                    geom,
                    ST_SetSRID(ST_MakePoint(:startLon, :startLat),4326)
                )
                LIMIT 1
            ),
            end_node AS (
                SELECT node_id AS id
                FROM node
                ORDER BY ST_DistanceSphere(
                    geom,
                    ST_SetSRID(ST_MakePoint(:endLon, :endLat),4326)
                )
                LIMIT 1
            ),
            astar_result AS (
                SELECT *
                FROM pgr_astar(
                    'SELECT id, source, target, cost, reverse_cost, x1, y1, x2, y2 FROM edge',
                    (SELECT id FROM start_node)::BIGINT,
                    (SELECT id FROM end_node)::BIGINT,
                    directed := true
                )
            )
            SELECT
                ST_X(ST_StartPoint(e.geom)) AS lon,
                ST_Y(ST_StartPoint(e.geom)) AS lat
            FROM astar_result ar
            JOIN edge e ON e.id = ar.edge
            WHERE ar.edge <> -1
            ORDER BY ar.seq
        """;
        Query query = em.createNativeQuery(routeSql);
        query.setParameter("startLon", startLon);
        query.setParameter("startLat", startLat);
        query.setParameter("endLon", endLon);
        query.setParameter("endLat", endLat);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(r -> new Point(
                        ((Number) r[1]).doubleValue(), // lat
                        ((Number) r[0]).doubleValue()  // lon
                ))
                .toList();
    }
}
