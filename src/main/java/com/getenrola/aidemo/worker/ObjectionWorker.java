package com.getenrola.aidemo.worker;


import com.getenrola.aidemo.agent.ConversationState;
import com.getenrola.aidemo.agent.OpenAiClientWrapper;
import com.getenrola.aidemo.agent.PromptTemplates;
import com.getenrola.aidemo.agent.Worker;
import com.getenrola.aidemo.model.WorkerResponse;

public class ObjectionWorker implements Worker {
    private final OpenAiClientWrapper client;

    public ObjectionWorker(OpenAiClientWrapper client) {
        this.client = client;
    }

    @Override
    public WorkerResponse handle(String input, ConversationState state) throws Exception {
        String prompt = input + "\nObjections:" + state.getSlots().getOrDefault("objections", "[]");

        return client.callModel(PromptTemplates.OBJECTION_PROMPT, prompt, "gpt-5-mini", WorkerResponse.class);
    }
}