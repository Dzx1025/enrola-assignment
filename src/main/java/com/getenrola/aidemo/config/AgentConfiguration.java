package com.getenrola.aidemo.config;

import com.getenrola.aidemo.agent.OpenAiClientWrapper;
import com.getenrola.aidemo.agent.Orchestrator;
import com.getenrola.aidemo.agent.Worker;
import com.getenrola.aidemo.agent.worker.ClosingWorker;
import com.getenrola.aidemo.agent.worker.GeneralWorker;
import com.getenrola.aidemo.agent.worker.ObjectionWorker;
import com.getenrola.aidemo.agent.worker.PriceComparisonWorker;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;


@Configuration
public class AgentConfiguration {

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public Worker generalWorker(OpenAiClientWrapper client) {
        return new GeneralWorker(client);
    }

    @Bean
    public Worker priceComparisonWorker(OpenAiClientWrapper client) {
        return new PriceComparisonWorker(client);
    }

    @Bean
    public Worker objectionWorker(OpenAiClientWrapper client) {
        return new ObjectionWorker(client);
    }

    @Bean
    public Worker closingWorker(OpenAiClientWrapper client) {
        return new ClosingWorker(client);
    }

    @Bean
    public Orchestrator orchestrator(Map<String, Worker> workers, OpenAiClientWrapper client) {
        return new Orchestrator(workers, client);
    }
}