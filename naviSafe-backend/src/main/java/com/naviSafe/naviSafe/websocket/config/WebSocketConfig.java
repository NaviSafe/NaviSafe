package com.naviSafe.naviSafe.websocket.config;

import com.naviSafe.naviSafe.websocket.GpsWebsocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final GpsWebsocketHandler gpsWebsocketHandler;

    @Autowired
    public WebSocketConfig(GpsWebsocketHandler gpsWebsocketHandler) {
        this.gpsWebsocketHandler = gpsWebsocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gpsWebsocketHandler, "/ws/gps")
                .setAllowedOrigins("*");
    }
}
