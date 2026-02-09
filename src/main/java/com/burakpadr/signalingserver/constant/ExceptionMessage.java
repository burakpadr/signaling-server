package com.burakpadr.signalingserver.constant;

public class ExceptionMessage {

    public static final String INVALID_SIGNALING_MESSAGE_FORMAT = "Invalid signaling message format.";

    public static final String PEER_ID_MISMATCH = "peerId mismatch.";

    public static final String TARGET_PEER_CANNOT_BE_THEMSELVES = "The target peer cannot be themselves.";

    public static final String UNKNOWN_SIGNALING_MESSAGE_TYPE = "Unknown signaling message type.";

    public static final String NO_FOUND_WEBSOCKET_CONN = "No websocket connection found.";

    public static final String NO_SESSION_CALL_STATE_FOUND = "No session call state found.";

    public static final String SDP_OFFER_NOT_ALLOWED_IN_CURRENT_STATE = "SDP_OFFER is not allowed in current state.";

    public static final String TARGET_PEER_OFFLINE = "Target peer offline.";
}
