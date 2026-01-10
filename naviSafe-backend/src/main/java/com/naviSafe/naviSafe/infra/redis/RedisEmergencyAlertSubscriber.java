package com.naviSafe.naviSafe.infra.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naviSafe.naviSafe.infra.fcm.FcmPushNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class RedisEmergencyAlertSubscriber implements MessageListener {

    Logger log = LoggerFactory.getLogger(RedisEmergencyAlertSubscriber.class);
    private final ObjectMapper objectMapper;
    private final List<Map<String, Object>> alertBuffer = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> flushTask;

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
            if(emergencyAlertData.containsKey("RCPTN_RGN_NM") && emergencyAlertData.get("RCPTN_RGN_NM").toString().contains("서울")) {
                alertBuffer.add(emergencyAlertData);
            }
            resetFlushTimer();


        } catch (Exception e) {
            log.error("EmergencyAlertSubscriber 처리 중 오류 발생", e);
        }
    }

    /**
     * 5초 동안 새로운 데이터가 없으면 자동으로 전송
     */
    private synchronized void resetFlushTimer() {
        // 기존 타이머가 있으면 취소
        if (flushTask != null && !flushTask.isDone()) {
            flushTask.cancel(false);
        }

        // 새 타이머 설정 (5초 후 실행)
        flushTask = scheduler.schedule(this::flushAlertBatch, 5, TimeUnit.SECONDS);
    }

    /**
     * 버퍼에 쌓인 데이터 전송 후 비우기
     */
    private synchronized void flushAlertBatch() {
        if (!alertBuffer.isEmpty()) {
            log.info("5초간 입력 없음, Emergency Alert 배치 전송 ({})", alertBuffer.size());

            try {
                // alertBuffer 내용을 하나로 합쳐서 title/body 구성 가능
                String title = "긴급재난 알림";
                StringBuilder bodyBuilder = new StringBuilder();

                for (Map<String, Object> alert : alertBuffer) {
                    bodyBuilder
                            .append(alert.get("MSG_CN"))
                            .append("\n");
                }

                String body = bodyBuilder.toString();

                fcmPushNotificationService.sendPushNotification(title, body);

            } catch (Exception e) {
                log.error("FCM 전송 실패", e);
            } finally {
                alertBuffer.clear();
            }
        }
    }
}
