package com.naviSafe.naviSafe.infra.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naviSafe.naviSafe.websocket.GpsWebsocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class RedisGpsSubscriber implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(RedisGpsSubscriber.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GpsWebsocketHandler gpsWebSocketHandler;
    private final List<Map<String, Object>> gpsBuffer = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> flushTask;

    @Autowired
    public RedisGpsSubscriber(GpsWebsocketHandler gpsWebSocketHandler) {
        this.gpsWebSocketHandler = gpsWebSocketHandler;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String data = new String(message.getBody());
            Map<String, Object> gpsData = objectMapper.readValue(data, Map.class);

            if (!"MAP_GPS".equals(gpsData.get("type"))) {
                return;
            }

            log.info("Redis 로부터 새로운 gps 메시지 구독: {}", gpsData);
            gpsBuffer.add(gpsData);
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
        flushTask = scheduler.schedule(this::flushGpsBatch, 5, TimeUnit.SECONDS);
    }

    /**
     * 버퍼에 쌓인 데이터 전송 후 비우기
     */
    private synchronized void flushGpsBatch() {
        if (!gpsBuffer.isEmpty()) {
            log.info("5초간 입력 없음, GPS 배치 전송 ({})", gpsBuffer.size());
            gpsWebSocketHandler.broadcastGpsBatch(new ArrayList<>(gpsBuffer));
            gpsBuffer.clear();
        }
    }
}
