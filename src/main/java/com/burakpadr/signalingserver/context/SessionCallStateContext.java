package com.burakpadr.signalingserver.context;

import com.burakpadr.signalingserver.constant.EnumCallState;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor
public class SessionCallStateContext {

    private static SessionCallStateContext instance = new SessionCallStateContext();

    private final Map<String, EnumCallState> sessionCallStates = new ConcurrentHashMap<>();

    public static SessionCallStateContext getInstance() {
        if (Objects.nonNull(instance)) {
            return instance;
        }
        instance = new SessionCallStateContext();

        return instance;
    }

    public void add(String sessionId, EnumCallState callState) {
        sessionCallStates.put(sessionId, callState);
    }

    public Optional<EnumCallState> get(String sessionId) {
        return Optional.ofNullable(sessionCallStates.get(sessionId));
    }

    public void remove(String sessionId) {
        sessionCallStates.remove(sessionId);
    }

    public void update(String sessionId, EnumCallState callState) {
        sessionCallStates.put(sessionId, callState);
    }
}
