package com.burakpadr.signalingserver.model;

import com.burakpadr.signalingserver.constant.EnumMessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignalingMessageModel {

    private EnumMessageType type;
    private String from;
    private String to;
    private JsonNode payload;
}
