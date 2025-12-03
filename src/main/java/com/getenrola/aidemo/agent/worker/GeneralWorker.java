package com.getenrola.aidemo.agent.worker;

import com.getenrola.aidemo.model.ConversationState;
import com.getenrola.aidemo.agent.OpenAiClientWrapper;
import com.getenrola.aidemo.agent.prompt.PromptTemplates;
import com.getenrola.aidemo.agent.Worker;
import com.getenrola.aidemo.model.WorkerResponse;

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