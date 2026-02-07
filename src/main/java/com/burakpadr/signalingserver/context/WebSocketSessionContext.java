package com.burakpadr.signalingserver.context;

import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor
public class WebSocketSessionContext {

    private static WebSocketSessionContext instance;

    private final Map<String, WebSocketSession> webSocketSessions = new ConcurrentHashMap<>();

    public static WebSocketSessionContext getInstance() {
        if (Objects.nonNull(instance)) {
            return instance;
        }

        instance = new WebSocketSessionContext();

        return instance;
    }

    public void add(String sessionId, WebSocketSession session) {
        webSocketSessions.put(sessionId, session);
    }

    public Optional<WebSocketSession> get(String sessionId) {
        return Optional.ofNullable(webSocketSessions.get(sessionId));
    }

    public void remove(String sessionId) {
        try {
            Optional<WebSocketSession> webSocketSessionOptional = Optional.ofNullable(webSocketSessions.get(sessionId));

            if (webSocketSessionOptional.isPresent()) {
                webSocketSessions.get(sessionId).close();

                webSocketSessions.remove(sessionId);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
