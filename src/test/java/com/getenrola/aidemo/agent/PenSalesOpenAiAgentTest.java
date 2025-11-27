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
    private Orchestrator orchestrator;
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
        orchestrator.getWorkers().forEach((k, v) -> System.out.println(k + " -> " + v.getClass().getSimpleName()));
    }
}
