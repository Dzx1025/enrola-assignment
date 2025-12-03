package com.getenrola.aidemo.agent;

import com.getenrola.aidemo.model.ConversationState;
import com.getenrola.aidemo.model.WorkerResponse;
import com.getenrola.aidemo.agent.worker.GeneralWorker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class BasicTest {

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
