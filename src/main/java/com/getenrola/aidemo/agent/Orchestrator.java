package com.getenrola.aidemo.agent;

import com.getenrola.aidemo.model.AgentResult;
import com.getenrola.aidemo.model.WorkerResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Orchestrator {
    private final Map<String, Worker> workers;
    private final OpenAiClientWrapper openAi;

    public Orchestrator(Map<String, Worker> workers, OpenAiClientWrapper openAi) {
        this.workers = workers;
        this.openAi = openAi;
    }

    public AgentResult route(String userInput, ConversationState state) throws Exception {
        // Save user input to conversation state
        state.addMessage("User: " + userInput);
        // Use LLM to select the appropriate worker
        String prompt = String.format("""
                        Given the user's message: "%s" and the user's current interest level: %d,
                        Select the most appropriate worker to handle this message from the following options: %s.
                        Respond with only the worker's name.
                        """,
                userInput,
                state.getInterestScore(),
                String.join(", ", workers.keySet())
        );
        String selected = openAi.callModel(
                "",
                prompt,
                "gpt-5-mini", String.class);

        if (!workers.containsKey(selected)) { // Fallback to general worker if selection is invalid
            selected = "general";
        }
        Worker selectedWorker = workers.get(selected);
        // Call the selected worker
        WorkerResponse workerResponse = selectedWorker.handle(userInput, state);
        // Update conversation state
        state.addMessage("Agent: " + workerResponse.message());
        workerResponse.slots().forEach(state::putSlot);
        // Adjust interest level
        int weight = 10;
        int delta = (int) ((double) workerResponse.lead_interest() * weight);
        state.adjustInterest(delta);
        // Return AgentResult
        AgentResult result = new AgentResult();
        result.setReplyAgent(selected);
        result.setInterest(state.getInterestScore());
        return result;
    }
}
