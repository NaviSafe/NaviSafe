package com.naviSafe.naviSafe.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class GpsWebsocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(GpsWebsocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("New connection established: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("Connection closed: {}", session.getId());
    }

    public void broadcastGpsBatch(List<Map<String, Object>> gpsList) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "gps_batch");
            message.put("data", gpsList);

            String json = objectMapper.writeValueAsString(message);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
