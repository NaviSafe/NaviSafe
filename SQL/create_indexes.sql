CREATE INDEX edge_base_source_idx ON edge_base(source);
CREATE INDEX edge_base_target_idx ON edge_base(target);
CREATE INDEX edge_base_geom_idx ON edge_base USING GIST(geom);