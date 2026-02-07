package com.burakpadr.signalingserver.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignalingSystemMessageModel {

    private String type;
    private String reason;
}
