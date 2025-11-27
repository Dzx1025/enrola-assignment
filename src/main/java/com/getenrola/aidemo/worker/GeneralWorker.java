package com.getenrola.aidemo.worker;

import com.getenrola.aidemo.agent.ConversationState;
import com.getenrola.aidemo.agent.OpenAiClientWrapper;
import com.getenrola.aidemo.agent.PromptTemplates;
import com.getenrola.aidemo.agent.Worker;
import com.getenrola.aidemo.model.WorkerResponse;
import org.springframework.ai.converter.BeanOutputConverter;

public class GeneralWorker implements Worker {
    private final OpenAiClientWrapper client;

    public GeneralWorker(OpenAiClientWrapper client) {
        this.client = client;
    }

    @Override
    public WorkerResponse handle(String userInput, ConversationState state) throws Exception {
        String history = userInput + "\nHistory:" + String.join("\n", state.getHistory());

        return client.callModel(PromptTemplates.GENERAL_PROMPT, history, "gpt-5-mini", WorkerResponse.class);
    }
}