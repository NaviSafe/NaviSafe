package com.naviSafe.naviSafe.infra.fcm;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.naviSafe.naviSafe.domain.fcmDeviceTokenManagement.entity.FcmDeviceToken;
import com.naviSafe.naviSafe.domain.fcmDeviceTokenManagement.repository.FcmDeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FcmPushNotificationService {


    private static final Logger log = LoggerFactory.getLogger(FcmPushNotificationService.class);
    private final FcmDeviceTokenRepository fcmDeviceTokenRepository;

    @Autowired
    public FcmPushNotificationService(FcmDeviceTokenRepository fcmDeviceTokenRepository) {
        this.fcmDeviceTokenRepository = fcmDeviceTokenRepository;
    }

    public void sendPushNotification(String title, String body, String channel, String type) throws Exception {
        // 전체 디바이스 토큰 조회
        List<String> tokens = fcmDeviceTokenRepository.findAll()
                .stream()
                .map(FcmDeviceToken::getDeviceToken)
                .collect(Collectors.toList());

        if (tokens.isEmpty()) return;

        // 메시지 구성
        MulticastMessage message = MulticastMessage.builder()
                .putData("title", title)
                .putData("body", body)
                .putData("channel", channel)
                .putData("type", type)
                .addAllTokens(tokens)
                .build();

        // FCM 전송
        BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
        log.info("전송 완료: 성공={}, 실패={}", response.getSuccessCount(), response.getFailureCount());
    }


}
