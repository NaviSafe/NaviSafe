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
public class RedisAlertSubscriber implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(RedisAlertSubscriber.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Map<String, Object>> alertBuffer = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> flushTask;

    private final FcmPushNotificationService fcmPushNotificationService;

    public RedisAlertSubscriber(FcmPushNotificationService fcmPushNotificationService) {
        this.fcmPushNotificationService = fcmPushNotificationService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String data = new String(message.getBody());
            Map<String, Object> alertData = objectMapper.readValue(data, Map.class);

            if (!"ACC_ALERTS".equals(channel)) {
                return;
            }

            log.info("Redis 로부터 새로운 돌발상황 알림 메시지 구독: {}", alertData);
            alertBuffer.add(alertData);
            resetFlushTimer();

        } catch (Exception e) {
            log.error("레디스 subscribe 내 처리 중 오류 발생", e);
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
            log.info("5초간 입력 없음, Alert 배치 전송 ({})", alertBuffer.size());

            try {
                // alertBuffer 내용을 하나로 합쳐서 title/body 구성 가능
                String title = "돌발 상황 알림";
                StringBuilder bodyBuilder = new StringBuilder();

                for (Map<String, Object> alert : alertBuffer) {
                    bodyBuilder
                            .append(alert.get("acc_info"))
                            .append("\n");
                }

                String body = bodyBuilder.toString();

                fcmPushNotificationService.sendPushNotification(title, body, "OUTBREAK_OCCUR_ALERT", "");

            } catch (Exception e) {
                log.error("FCM 전송 실패", e);
            } finally {
                alertBuffer.clear();
            }
        }
    }
}
