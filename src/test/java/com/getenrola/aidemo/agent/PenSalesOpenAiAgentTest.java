package com.getenrola.aidemo.agent;

import com.getenrola.aidemo.model.AgentResult;
import com.getenrola.aidemo.model.WorkerResponse;
import com.getenrola.aidemo.worker.ClosingWorker;
import com.getenrola.aidemo.worker.GeneralWorker;
import com.getenrola.aidemo.worker.ObjectionWorker;
import com.getenrola.aidemo.worker.PriceComparisonWorker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;


@SpringBootTest
public class PenSalesOpenAiAgentTest {

    @Autowired
    private OpenAiClientWrapper openAiClientWrapper;

    @Test
    void generalWorkerTest() throws Exception {
        GeneralWorker worker = new GeneralWorker(openAiClientWrapper);
        ConversationState state = new ConversationState();
        WorkerResponse result = worker.handle("Hello, I am interested in buying a pen.", state);
        Assertions.assertNotNull(result);
    }

    @Test
    void orchestratorTest() throws Exception {
        Orchestrator orchestrator = new Orchestrator(
                Map.of(
                        "general", new GeneralWorker(openAiClientWrapper),
                        "price_comparison", new PriceComparisonWorker(openAiClientWrapper),
                        "objection", new ObjectionWorker(openAiClientWrapper),
                        "closing", new ClosingWorker(openAiClientWrapper)
                ),
                openAiClientWrapper
        );

        ConversationState state = new ConversationState();
        AgentResult r1 = orchestrator.route("Hi", state);
        Assertions.assertNotNull(r1);
        AgentResult r2 = orchestrator.route("I need a pen for signing contracts", state);
        Assertions.assertNotNull(r2);
        AgentResult r3 = orchestrator.route("How much does it cost?", state);
        Assertions.assertNotNull(r3);
        AgentResult r4 = orchestrator.route("Sounds good, send link", state);
        Assertions.assertNotNull(r4);
        System.out.println(state.getHistory());
    }
}
