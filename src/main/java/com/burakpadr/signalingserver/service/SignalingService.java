package com.burakpadr.signalingserver.service;

import com.burakpadr.signalingserver.constant.EnumCallState;
import com.burakpadr.signalingserver.constant.EnumMessageType;
import com.burakpadr.signalingserver.constant.ExceptionMessage;
import com.burakpadr.signalingserver.model.SignalingMessageModel;
import com.burakpadr.signalingserver.context.SessionCallStateContext;
import com.burakpadr.signalingserver.context.WebSocketSessionContext;
import com.burakpadr.signalingserver.model.SignalingSystemMessageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SignalingService {

    private final ObjectMapper objectMapper;

    public void handle(String fromPeerId, String payload) {
        SignalingMessageModel message;

        try {
            message = objectMapper.readValue(payload, SignalingMessageModel.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(ExceptionMessage.INVALID_SIGNALING_MESSAGE_FORMAT, e);
        }

        WebSocketSession fromSession = WebSocketSessionContext.getInstance()
                .get(message.getFrom())
                .orElseThrow(() -> new IllegalStateException(ExceptionMessage.NO_FOUND_WEBSOCKET_CONN));

        if (!message.getFrom().equals(fromPeerId)) {
            sendSystem(fromSession, EnumMessageType.FAILED, ExceptionMessage.PEER_ID_MISMATCH);

            return;
        }
        else if (message.getFrom().equals(message.getTo())) {
            sendSystem(fromSession, EnumMessageType.FAILED, ExceptionMessage.TARGET_PEER_CANNOT_BE_THEMSELVES);

            return;
        }

        switch (message.getType()) {

            case CALL_REQUEST -> handleCallRequest(fromSession, message);
            case CALL_ACCEPT -> handleCallAccept(message);
            case CALL_REJECT -> handleCallReject(message);
            case SDP_OFFER, SDP_ANSWER -> handleSdp(fromSession, message);
            case ICE_CANDIDATE -> handleIceCandidate(message);
            default -> sendSystem(fromSession, EnumMessageType.FAILED, ExceptionMessage.UNKNOWN_SIGNALING_MESSAGE_TYPE);
        }
    }

    private void handleCallRequest(WebSocketSession fromSession, SignalingMessageModel message) {
        Optional<WebSocketSession> targetSessionOptional = WebSocketSessionContext.getInstance().get(message.getTo());

        if (targetSessionOptional.isEmpty()) {
            sendSystem(fromSession, EnumMessageType.FAILED, "PEER_OFFLINE");

            return;
        }

        EnumCallState targetState = SessionCallStateContext.getInstance()
                        .get(message.getTo())
                        .orElseThrow(() -> new IllegalStateException(ExceptionMessage.NO_SESSION_CALL_STATE_FOUND));

        if (targetState != EnumCallState.IDLE) {
            sendSystem(fromSession, EnumMessageType.FAILED, "PEER_BUSY");

            return;
        }

        SessionCallStateContext.getInstance().update(message.getFrom(), EnumCallState.RINGING);
        SessionCallStateContext.getInstance().update(message.getTo(), EnumCallState.RINGING);

        send(targetSessionOptional.get(), message);
    }

    private void handleCallAccept(SignalingMessageModel message) {
        SessionCallStateContext.getInstance().update(message.getFrom(), EnumCallState.IN_CALL);
        SessionCallStateContext.getInstance().update(message.getTo(), EnumCallState.IN_CALL);

        WebSocketSession callerSession = WebSocketSessionContext.getInstance()
                        .get(message.getTo())
                        .orElseThrow();

        send(callerSession, message);
    }

    private void handleCallReject(SignalingMessageModel message) {
        SessionCallStateContext.getInstance().update(message.getFrom(), EnumCallState.IDLE);
        SessionCallStateContext.getInstance().update(message.getTo(), EnumCallState.IDLE);

        WebSocketSession callerSession = WebSocketSessionContext.getInstance()
                        .get(message.getTo())
                        .orElseThrow();

        send(callerSession, message);
    }

    private void handleSdp(WebSocketSession fromSession, SignalingMessageModel message) {
        EnumCallState fromState = SessionCallStateContext.getInstance()
                .get(message.getFrom())
                .orElseThrow(() -> new IllegalStateException(ExceptionMessage.NO_SESSION_CALL_STATE_FOUND));

        EnumCallState toState = SessionCallStateContext.getInstance()
                .get(message.getTo())
                .orElseThrow(() -> new IllegalStateException(ExceptionMessage.NO_SESSION_CALL_STATE_FOUND));

        if (fromState != EnumCallState.IN_CALL || toState != EnumCallState.IN_CALL) {
            sendSystem(fromSession, EnumMessageType.FAILED, ExceptionMessage.SDP_OFFER_NOT_ALLOWED_IN_CURRENT_STATE);

            return;
        }

        WebSocketSession targetSession = WebSocketSessionContext.getInstance()
                        .get(message.getTo())
                        .orElseThrow(() -> new IllegalStateException(ExceptionMessage.TARGET_PEER_OFFLINE));

        send(targetSession, message);
    }

    private void handleIceCandidate(SignalingMessageModel message) {
        EnumCallState fromState = SessionCallStateContext.getInstance()
                .get(message.getFrom())
                .orElseThrow(() -> new IllegalStateException(ExceptionMessage.NO_SESSION_CALL_STATE_FOUND));

        if (fromState == EnumCallState.RINGING || fromState == EnumCallState.IN_CALL) {
            Optional<WebSocketSession> targetSessionOptional =
                    WebSocketSessionContext.getInstance().get(message.getTo());

            if (targetSessionOptional.isEmpty()) {
                return;
            }

            send(targetSessionOptional.get(), message);
        }
    }

    private void sendSystem(WebSocketSession session, EnumMessageType type, String reason) {
        SignalingSystemMessageModel systemMessage = SignalingSystemMessageModel.builder()
                        .type(type.name())
                        .reason(reason)
                        .build();

        send(session, systemMessage);
    }

    private void send(WebSocketSession session, Object payload) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
