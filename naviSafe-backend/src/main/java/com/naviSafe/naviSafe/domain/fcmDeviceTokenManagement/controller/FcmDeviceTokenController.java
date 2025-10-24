package com.naviSafe.naviSafe.domain.fcmDeviceTokenManagement.controller;


import com.naviSafe.naviSafe.domain.fcmDeviceTokenManagement.dto.FcmDeviceTokenRequestDto;
import com.naviSafe.naviSafe.domain.fcmDeviceTokenManagement.dto.FcmDeviceTokenResponseDto;
import com.naviSafe.naviSafe.domain.fcmDeviceTokenManagement.entity.FcmDeviceToken;
import com.naviSafe.naviSafe.domain.fcmDeviceTokenManagement.service.FcmDeviceTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
public class FcmDeviceTokenController {

    private final FcmDeviceTokenService fcmDeviceTokenService;

    @Autowired
    public FcmDeviceTokenController(FcmDeviceTokenService fcmDeviceTokenService) {
        this.fcmDeviceTokenService = fcmDeviceTokenService;
    }

    @PostMapping("/api/fcm/register")
    public ResponseEntity<FcmDeviceTokenResponseDto> registerDeviceToken(@RequestBody FcmDeviceTokenRequestDto fcmDeviceTokenRequestDto) {
        return fcmDeviceTokenService.findByDeviceToken(fcmDeviceTokenRequestDto.getDeviceToken())
                .map(token -> ResponseEntity.ok(
                        new FcmDeviceTokenResponseDto(token.getId(), token.getDeviceToken())
                ))
                .orElseGet(() -> {
                    FcmDeviceToken savedToken = fcmDeviceTokenService.createFcmDeviceToken(
                            FcmDeviceToken.builder()
                                    .deviceToken(fcmDeviceTokenRequestDto.getDeviceToken())
                                    .createdAt(LocalDateTime.now())
                                    .build()
                    );
                    return ResponseEntity.ok(
                            new FcmDeviceTokenResponseDto(savedToken.getId(), savedToken.getDeviceToken())
                    );
                });
    }
}
