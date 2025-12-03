package com.getenrola.aidemo.agent.worker;

import com.getenrola.aidemo.model.ConversationState;
import com.getenrola.aidemo.agent.OpenAiClientWrapper;
import com.getenrola.aidemo.agent.prompt.PromptTemplates;
import com.getenrola.aidemo.agent.Worker;
import com.getenrola.aidemo.model.WorkerResponse;

public class PriceComparisonWorker implements Worker {
    private final OpenAiClientWrapper client;

    public PriceComparisonWorker(OpenAiClientWrapper client) {
        this.client = client;
    }

    @Override
    public WorkerResponse handle(String input, ConversationState state) throws Exception {
        String prompt = input + "\nSlots:" + state.getSlots().toString();

        return client.callModel(PromptTemplates.PRICE_PROMPT, prompt, "gpt-5-mini", WorkerResponse.class);
    }
}
