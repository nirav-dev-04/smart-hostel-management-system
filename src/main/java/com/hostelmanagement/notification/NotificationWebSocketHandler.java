package com.hostelmanagement.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    // Map to keep track of active sessions per user ID
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("WebSocket connection established for user ID: {}", userId);
        } else {
            log.warn("WebSocket connection established but no valid userId found in query params.");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket connection closed for user ID: {}", userId);
        }
    }

    public void sendNotification(Long userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.info("Successfully sent real-time WebSocket notification to user ID: {}", userId);
            } catch (IOException e) {
                log.error("Error sending WebSocket notification to user ID: {}", userId, e);
            }
        } else {
            log.debug("User ID: {} is not currently connected via WebSocket.", userId);
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            String query = uri.getQuery();
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyVal = param.split("=");
                if (keyVal.length == 2 && keyVal[0].equals("userId")) {
                    try {
                        return Long.parseLong(keyVal[1]);
                    } catch (NumberFormatException e) {
                        log.error("Invalid userId format in WebSocket query: {}", keyVal[1]);
                    }
                }
            }
        }
        return null;
    }
}
