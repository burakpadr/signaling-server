package com.burakpadr.signalingserver.service;

import com.burakpadr.signalingserver.constant.EnumCallState;
import com.burakpadr.signalingserver.context.SessionCallStateContext;
import com.burakpadr.signalingserver.context.WebSocketSessionContext;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
public class SessionRegistryService {

    public void register(String peerId, WebSocketSession session) {
        WebSocketSessionContext.getInstance().add(peerId, session);
        SessionCallStateContext.getInstance().add(peerId, EnumCallState.IDLE);
    }
}
