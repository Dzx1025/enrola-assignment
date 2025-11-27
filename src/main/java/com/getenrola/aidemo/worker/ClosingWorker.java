package com.getenrola.aidemo.worker;

import com.getenrola.aidemo.agent.ConversationState;
import com.getenrola.aidemo.agent.OpenAiClientWrapper;
import com.getenrola.aidemo.agent.PromptTemplates;
import com.getenrola.aidemo.agent.Worker;
import com.getenrola.aidemo.model.WorkerResponse;
import org.springframework.ai.converter.BeanOutputConverter;

public class ClosingWorker implements Worker {
    private final OpenAiClientWrapper client;

    public ClosingWorker(OpenAiClientWrapper client) {
        this.client = client;
    }

    @Override
    public WorkerResponse handle(String input, ConversationState state) throws Exception {
        String prompt = input + "\nLead:" + state.getSlots().toString();

        return client.callModel(PromptTemplates.CLOSING_PROMPT, prompt, "gpt-5-mini", WorkerResponse.class);
    }
}