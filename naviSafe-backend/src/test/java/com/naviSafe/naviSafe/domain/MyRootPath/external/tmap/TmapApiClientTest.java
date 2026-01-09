package com.naviSafe.naviSafe.domain.MyRootPath.external.tmap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto.TmapRouteRequest;
import com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto.TmapRouteResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TmapApiClientTest{

    private TmapApiClient tmapApiClient;

    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public TmapApiClientTest(TmapApiClient tmapApiClient) {
        this.tmapApiClient = tmapApiClient;
    }

    @Test
    @DisplayName("경로 API 테스트")
    void getTmap() throws JsonProcessingException {
        TmapRouteRequest tmapRouteRequest = new TmapRouteRequest(
                127.10323758,
                37.36520202,
                128.87264091,
                35.17240084,
                null
        );

        TmapRouteResponse tmapRoute = tmapApiClient.getTmapRoute(tmapRouteRequest);
        System.out.println(
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(tmapRoute)
        );

        assertNotNull(tmapRoute);

    }
}