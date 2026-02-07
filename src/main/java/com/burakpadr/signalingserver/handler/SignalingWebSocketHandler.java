package com.burakpadr.signalingserver.handler;

import com.burakpadr.signalingserver.context.WebSocketSessionContext;
import com.burakpadr.signalingserver.service.SessionRegistryService;
import com.burakpadr.signalingserver.service.SignalingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class SignalingWebSocketHandler extends TextWebSocketHandler {

    private final SessionRegistryService sessionRegistry;
    private final SignalingService signalingService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Optional<String> peerIdOptional = extractPeerId(session);

        if (peerIdOptional.isEmpty()) {
            session.close(CloseStatus.BAD_DATA);

            return;
        }

        String peerId = peerIdOptional.get();

        session.getAttributes().put("peerId", peerId);

        sessionRegistry.register(peerId, session);

        log.info("New peer connected with id {}", peerId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String peerId = (String) session.getAttributes().get("peerId");

        if (Objects.isNull(peerId)) {
            session.close(CloseStatus.BAD_DATA);

            return;
        }

        signalingService.handle(peerId, message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String peerId = (String) session.getAttributes().get("peerId");

        if (Objects.nonNull(peerId)) {
            WebSocketSessionContext.getInstance().remove(peerId);

            log.info("Peer disconnected with id {}", session.getId());
        }
    }

    private Optional<String> extractPeerId(WebSocketSession session) {
        if (Objects.isNull(session) || Objects.isNull(session.getUri())) {
            return Optional.empty();
        }

        Map<String, String> params =
                UriComponentsBuilder.fromUri(session.getUri())
                        .build()
                        .getQueryParams()
                        .toSingleValueMap();

        return Optional.ofNullable(params.get("peerId"));
    }
}
