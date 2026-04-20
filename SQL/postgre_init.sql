CREATE Table edge_base AS
SELECT
    l.link_id::bigint AS id,
    l.f_node::bigint AS source,
    l.t_node::bigint AS target,
    l.length,
    l.max_spd,

    CASE
        WHEN n.turn_p = '1' AND ti.turn_type = '011' THEN -1
        ELSE l.length
        END AS cost,

    -1 AS reverse_cost,

    l.geom,

    ST_X(ST_StartPoint((ST_Dump(l.geom)).geom)) AS x1,
    ST_Y(ST_StartPoint((ST_Dump(l.geom)).geom)) AS y1,
    ST_X(ST_EndPoint((ST_Dump(l.geom)).geom)) AS x2,
    ST_Y(ST_EndPoint((ST_Dump(l.geom)).geom)) AS y2

FROM link l

         LEFT JOIN node n
                   ON l.f_node = n.node_id
         LEFT JOIN turninfo ti
                   ON n.node_id = ti.node_id;

CREATE INDEX edge_base_source_idx ON edge_base(source);
CREATE INDEX edge_base_target_idx ON edge_base(target);
CREATE INDEX edge_base_geom_idx ON edge_base USING GIST(geom);


CREATE USER root WITH PASSWORD '991120';
GRANT ALL PRIVILEGES ON DATABASE navigation TO root;


GRANT SELECT ON TABLE link TO root;
GRANT SELECT ON TABLE node TO root;
GRANT SELECT ON TABLE turninfo TO root;
GRANT SELECT ON TABLE edge_base TO root;