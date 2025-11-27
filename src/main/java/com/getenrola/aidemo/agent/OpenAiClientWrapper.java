package com.getenrola.aidemo.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class OpenAiClientWrapper {

    private final ChatClient chatClient;

    @Value("${spring.ai.openai.chat.options.model:gpt-5-mini}")
    private String model;

    public OpenAiClientWrapper(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public <T> T callModel(String userPrompt,
                           Class<T> responseType) {
        return chatClient.prompt()
                .options(OpenAiChatOptions.builder()
                        .model(this.model)
                        .temperature(1.0)  // disable temperature for gpt5 series
                        .build())
                .user(userPrompt)
                .call().entity(responseType);
    }

    public <T> T callModel(String systemPrompt,
                           String userPrompt,
                           Class<T> responseType) {
        return chatClient.prompt()
                .options(OpenAiChatOptions.builder()
                        .model(this.model)
                        .temperature(1.0)  // disable temperature for gpt5 series
                        .build())
                .system(systemPrompt)
                .user(userPrompt)
                .call().entity(responseType);
    }

    public <T> T callModel(String systemPrompt,
                           String userPrompt,
                           String model,
                           Class<T> responseType) {
        return chatClient.prompt()
                .options(OpenAiChatOptions.builder()
                        .model(model)
                        .temperature(1.0)
                        .build())
                .system(systemPrompt)
                .user(userPrompt)
                .call().entity(responseType);
    }

}