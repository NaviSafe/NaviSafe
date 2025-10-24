package com.naviSafe.naviSafe.domain.fcmDeviceTokenManagement.service;

import com.naviSafe.naviSafe.domain.fcmDeviceTokenManagement.entity.FcmDeviceToken;
import com.naviSafe.naviSafe.domain.fcmDeviceTokenManagement.repository.FcmDeviceTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FcmDeviceTokenService {
    private final FcmDeviceTokenRepository fcmDeviceTokenRepository;

    @Autowired
    public FcmDeviceTokenService(FcmDeviceTokenRepository fcmDeviceTokenRepository) {
        this.fcmDeviceTokenRepository = fcmDeviceTokenRepository;
    }

    public FcmDeviceToken createFcmDeviceToken(FcmDeviceToken fcmDeviceToken) {
        return fcmDeviceTokenRepository.save(fcmDeviceToken);
    }

    public Optional<FcmDeviceToken> findByDeviceToken(String token) {
        return fcmDeviceTokenRepository.findByDeviceToken(token);
    }
}
