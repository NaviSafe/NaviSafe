DROP TABLE IF EXISTS edge_base;
-- 생성
CREATE Table edge_base AS
SELECT
    link_id::bigint AS id,
    f_node::bigint AS source,
    t_node::bigint AS target,
    length,
    max_spd,
    length / NULLIF(max_spd,0) AS cost,
    -1 AS reverse_cost,
    geom,
    ST_X(ST_StartPoint((ST_Dump(geom)).geom)) AS x1,
    ST_Y(ST_StartPoint((ST_Dump(geom)).geom)) AS y1,
    ST_X(ST_EndPoint((ST_Dump(geom)).geom)) AS x2,
    ST_Y(ST_EndPoint((ST_Dump(geom)).geom)) AS y2
FROM link;

-- 업데이트
UPDATE edge_base eb
SET cost = -1
WHERE (eb.source, eb.target) IN (
    SELECT
        l2.t_node::BIGINT AS start_point,
        l3.t_node::BIGINT AS end_point
    FROM link l1
             JOIN link l2
                  ON l1.t_node = l2.f_node
             JOIN link l3
                  ON l2.t_node = l3.f_node
             JOIN link l4
                  ON l3.t_node = l4.f_node
             JOIN turninfo ti
                  ON ti.ed_link = l4.link_id
                      AND ti.st_link = l1.link_id
    where turn_type IN ('003', '101', '102', '103'));