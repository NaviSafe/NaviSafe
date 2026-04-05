package com.naviSafe.naviSafe.domain.address.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AddressService {

    @Value("${juso.api-key}")
    private String confmKey;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RestTemplate restTemplate = new RestTemplate();

    public Object searchAddress(String keyword) {

        String url = "https://business.juso.go.kr/addrlink/addrLinkApi.do";
        logger.info("검색주소: {}", keyword);

        URI uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("currentPage", 1)
                .queryParam("countPerPage", 10)
                .queryParam("keyword", keyword)
                .queryParam("resultType", "json")
                .queryParam("confmKey", confmKey)
                .encode()
                .build()
                .toUri();

        ResponseEntity<Map> response = restTemplate.getForEntity(uri, Map.class);
        return response.getBody();
    }
}
