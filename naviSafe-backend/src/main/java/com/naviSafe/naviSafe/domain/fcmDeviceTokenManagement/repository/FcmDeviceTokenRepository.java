package com.naviSafe.naviSafe.domain.fcmDeviceTokenManagement.repository;

import com.naviSafe.naviSafe.domain.fcmDeviceTokenManagement.entity.FcmDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FcmDeviceTokenRepository extends JpaRepository<FcmDeviceToken, Long> {
    Optional<FcmDeviceToken> findByDeviceToken(String token);
}
