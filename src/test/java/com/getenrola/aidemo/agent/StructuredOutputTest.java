package com.getenrola.aidemo.agent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class StructuredOutputTest {
    @Autowired
    private OpenAiClientWrapper openAiClientWrapper;

    public record TestResponse(
            String message,
            String modelName,
            String reason
    ) {
    }

    @Test
    void testCallModel() {
        String systemPrompt = "You are a helpful AI. Output JSON with a single field message.";
        String userPrompt = "Introduce yourself";

        TestResponse response = openAiClientWrapper.callModel(
                systemPrompt,
                userPrompt,
                TestResponse.class
        );

        assertNotNull(response);
        assertNotNull(response.message());
        System.out.println("Model returned: " + response);
    }
}
