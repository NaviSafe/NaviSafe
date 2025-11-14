package com.naviSafe.naviSafe.domain.fcmDeviceTokenManagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "fcm_device_token")
public class FcmDeviceToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String deviceToken;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
