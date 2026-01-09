package com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto.geometry;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class TmapGeometryDeserializer extends JsonDeserializer<TmapGeometry> {


    @Override
    public TmapGeometry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);
        String type = node.get("type").asText();

        ObjectMapper mapper = (ObjectMapper) p.getCodec();

        if ("Point".equals(type)) {
            return mapper.treeToValue(node, TmapPointGeometry.class);
        }

        if ("LineString".equals(type)) {
            return mapper.treeToValue(node, TmapLineStringGeometry.class);
        }

        throw new IllegalArgumentException("Unsupported geometry type: " + type);
    }
}
