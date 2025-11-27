package com.getenrola.aidemo.worker;

import com.getenrola.aidemo.agent.ConversationState;
import com.getenrola.aidemo.agent.OpenAiClientWrapper;
import com.getenrola.aidemo.agent.PromptTemplates;
import com.getenrola.aidemo.agent.Worker;
import com.getenrola.aidemo.model.WorkerResponse;
import org.springframework.ai.converter.BeanOutputConverter;

public class PriceComparisonWorker implements Worker {
    private final OpenAiClientWrapper client;
    private final BeanOutputConverter<WorkerResponse> beanOutputConverter;

    public PriceComparisonWorker(OpenAiClientWrapper client) {
        this.client = client;
        this.beanOutputConverter = new BeanOutputConverter<>(WorkerResponse.class);

    }

    @Override
    public WorkerResponse handle(String input, ConversationState state) throws Exception {
        String jsonFormat = this.beanOutputConverter.getFormat();

        String systemPrompt = String.format(PromptTemplates.PRICE_PROMPT, jsonFormat);
        String prompt = input + "\nSlots:" + state.getSlots().toString();

        return client.callModel(systemPrompt, prompt, "gpt-5-mini", WorkerResponse.class);
    }
}
