package com.naviSafe.naviSafe.domain.address.service;

import com.naviSafe.naviSafe.domain.address.dto.SearchPlaceDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

    @Value("${kakao.api-key}")
    private String restApiKey;

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

    public Object searchPlace(SearchPlaceDto searchPlaceDto) {
        String lat = searchPlaceDto.getLat();
        String lon = searchPlaceDto.getLon();
        String keyword = searchPlaceDto.getKeyword();
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?y="+lat+"&x="+lon+"&radius=20000";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + restApiKey);

        URI uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("query", keyword)
                .queryParam("size", 10)
                .encode()
                .build()
                .toUri();

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                Map.class
        );



        return response.getBody();
    }
}
