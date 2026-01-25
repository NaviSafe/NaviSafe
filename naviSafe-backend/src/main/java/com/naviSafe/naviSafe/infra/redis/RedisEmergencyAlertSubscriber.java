package com.naviSafe.naviSafe.infra.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naviSafe.naviSafe.infra.fcm.FcmPushNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Component
public class RedisEmergencyAlertSubscriber implements MessageListener {

    Logger log = LoggerFactory.getLogger(RedisEmergencyAlertSubscriber.class);
    private final ObjectMapper objectMapper;
    private final FcmPushNotificationService fcmPushNotificationService;

    public RedisEmergencyAlertSubscriber(FcmPushNotificationService fcmPushNotificationService) {
        this.objectMapper = new ObjectMapper();
        this.fcmPushNotificationService = fcmPushNotificationService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String data = new String(message.getBody());

            // 긴급 재난문자 체널만 구독
            if (!"EMERGENCY_ALERT_CHANNEL".equals(channel)) {
                return;
            }

            Map<String, Object> emergencyAlertData = objectMapper.readValue(data, Map.class);

            log.info("[Redis 구독] EMERGENCY_ALERT_CHANNEL - 데이터 수신: {}", emergencyAlertData);

            // 260110 기준: 서울 대상으로 서비스를 진행하므로 해당지역 메시지만 포함
            String crt = emergencyAlertData.get("created_at").toString().split("\\.")[0];
            if (
                    emergencyAlertData.get("region").toString().contains("서울")
                            && LocalDateTime.parse(crt,
                                    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                            ).toLocalTime().truncatedTo(ChronoUnit.MINUTES)
                            .equals(LocalTime.now().truncatedTo(ChronoUnit.MINUTES))
            ) {
                sendNotification(emergencyAlertData);
            }

        } catch (Exception e) {
            log.error("EmergencyAlertSubscriber 처리 중 오류 발생", e);
        }
    }

    /**
     * 재난알람 전송
     */
    private void sendNotification(Map<String, Object> emergencyAlertData) {
         try {
                String title = "긴급재난 알림";
                String body = String.valueOf(emergencyAlertData.get("message"));
                String type = String.valueOf(emergencyAlertData.get("type")); // 강수, 지진 등
                fcmPushNotificationService.sendPushNotification(title, body, "EMERGENCY_ALERT", type);

            } catch (Exception e) {
                log.error("FCM 전송 실패", e);
            }
    }
}
