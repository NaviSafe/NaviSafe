package com.naviSafe.naviSafe.domain.MyRootPath.external.tmap;

import com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto.TmapRouteRequest;
import com.naviSafe.naviSafe.domain.MyRootPath.external.tmap.dto.TmapRouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class TmapApiClient {
    private final WebClient tmapWebClient;

    public TmapRouteResponse getTmapRoute(TmapRouteRequest request) {
        return tmapWebClient.post()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/tmap/routes")
                                .queryParam("version", "1")
                                .build()
                )
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TmapRouteResponse.class)
                .block();
    }
}
