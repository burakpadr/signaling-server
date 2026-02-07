package com.burakpadr.signalingserver.constant;

public enum EnumMessageType {

    CALL_REQUEST,
    CALL_ACCEPT,
    CALL_REJECT,
    CALL_FAILED,

    SDP_OFFER,
    SDP_ANSWER,

    ICE_CANDIDATE
}
