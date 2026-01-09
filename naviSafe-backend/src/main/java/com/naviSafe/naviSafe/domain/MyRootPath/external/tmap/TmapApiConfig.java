package com.naviSafe.naviSafe.domain.MyRootPath.external.tmap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TmapApiConfig {

    @Value("${tmap.base-url}")
    private String baseUrl;

    @Value("${tmap.app-key}")
    private String appKey;

    @Bean
    public WebClient tmapWebClient() {
        return WebClient.builder().baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("appKey", appKey)
                .build();
    }



}
