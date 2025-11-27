package com.getenrola.aidemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

// {"message": string, "slots": { ... }, "stage": string, "lead_interest": number}
public record WorkerResponse(
        @JsonProperty(required = true, value = "message")
        String message,

        @JsonProperty(required = true, value = "slots")
        Map<String, Object> slots,

        @JsonProperty(required = true, value = "stage")
        String stage,

        @JsonProperty(required = true, value = "lead_interest")
        int lead_interest
) {
}

