package com.naviSafe.naviSafe.domain.MyRootPath.v2.repository;

import com.naviSafe.naviSafe.domain.MyRootPath.v2.dto.Point;
import com.naviSafe.naviSafe.domain.MyRootPath.v2.dto.RouteEdge;
import com.naviSafe.naviSafe.domain.MyRootPath.v2.dto.RouteResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.geolatte.geom.LineString;
import org.geolatte.geom.MultiLineString;
import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Repository
public class RouteRepository {

    @PersistenceContext(unitName = "postgresEntityManager")
    private EntityManager em;

    Logger logger = LoggerFactory.getLogger(RouteRepository.class);

    @Transactional(transactionManager = "postgresTransactionManager")
    public RouteResult findRoute(
            double startLon,
            double startLat,
            double endLon,
            double endLat,
            List<Point> accidentPoints
    ) {

        logger.info("실시간 돌발상황 갯수:  {}", accidentPoints.size());

        // 임시 테이블 생성
        String createAccidentTable = """
            CREATE TEMP TABLE accident_points (
                geom geometry(POINT, 4326)
            ) ON COMMIT DROP
        """;
        em.createNativeQuery(createAccidentTable).executeUpdate();
        logger.info("임시 사고 테이블 생성 완료");

        // 배치 insert
        for (Point p : accidentPoints) {
            String insertSql = "INSERT INTO accident_points (geom) VALUES (ST_SetSRID(ST_MakePoint(:lon, :lat), 4326))";
            em.createNativeQuery(insertSql)
                    .setParameter("lon", p.lon())
                    .setParameter("lat", p.lat())
                    .executeUpdate();
        }
        logger.info("사고 좌표 insert 완료");

        String createIndex = "CREATE INDEX idx_accident_geom ON accident_points USING GIST(geom)";
        em.createNativeQuery(createIndex).executeUpdate();

        // edge 임시 테이블 생성
        String createTempEdge = """
            CREATE TEMP TABLE edge
            ON COMMIT DROP
            AS
            SELECT *
            FROM edge_base
        """;
        em.createNativeQuery(createTempEdge).executeUpdate();
        logger.info("Edge 임시 테이블 생성 완료");

        // edge 업데이트 (BBOX + 정확 거리)
        String updateAccidentCost = """
            UPDATE edge e
            SET cost = -1
            FROM accident_points ap
            WHERE e.geom && ST_Expand(ap.geom, 0.0005)  -- 약 50m bbox 필터
              AND ST_DWithin(e.geom, ap.geom, 0.0005)   -- 실제 거리 계산
        """;
        em.createNativeQuery(updateAccidentCost).executeUpdate();
        logger.info("BBOX 기반 돌발상황 발생 노드 cost 업데이트 완료");

        // A* 경로 조회
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
                ar.seq,
                ar.edge,
                e.geom,
                ar.cost,
                ar.agg_cost,
                ST_Length(e.geom::geography) AS length
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

        List<RouteEdge> routeEdges = results.stream()
                .map(r -> new RouteEdge(
                        ((Number) r[0]).intValue(),
                        ((Number) r[1]).longValue(),
                        (MultiLineString<Position>) r[2],
                        ((Number) r[3]).doubleValue(),
                        ((Number) r[4]).doubleValue()
                ))
                .toList();

        logger.info("A star 경로 조회 완료");

        // 3️⃣ 최종 반환
        List<Point> points = routeEdges.stream()
                .flatMap(edge -> {
                    MultiLineString<Position> multi = edge.geom();
                    return IntStream.range(0, multi.getNumGeometries())
                            .mapToObj(i -> (LineString<Position>) multi.getGeometryN(i));
                })
                .flatMap(line -> {
                    PositionSequence<Position> seq = line.getPositions();
                    return StreamSupport.stream(seq.spliterator(), false);
                })
                .map(p -> new Point(p.getCoordinate(1), p.getCoordinate(0)))
                .toList();

        double totalDistance = results.stream()
                .mapToDouble(r -> ((Number) r[5]).doubleValue()) // length
                .sum();

        return new RouteResult(points, totalDistance);
    }
}
