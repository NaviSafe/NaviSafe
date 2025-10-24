package com.naviSafe.naviSafe.domain.fcmDeviceTokenManagement.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class FcmDeviceTokenResponseDto {
    private Long id;
    private String deviceToken;
}
