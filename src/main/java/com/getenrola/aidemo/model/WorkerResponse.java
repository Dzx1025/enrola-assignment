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
        double lead_interest,   // -1.0 to +1.0

        @JsonProperty(required = false, value = "next_recommended_worker")
        String next_recommended_worker,  // worker can suggest handoff

        @JsonProperty(required = false, value = "confidence")
        double confidence  // how confident this worker is
) {
}

