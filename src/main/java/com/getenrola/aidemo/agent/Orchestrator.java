package com.getenrola.aidemo.agent;

import com.getenrola.aidemo.model.AgentResult;
import com.getenrola.aidemo.model.WorkerResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Orchestrator {
    private final Map<String, Worker> workers;
    private final OpenAiClientWrapper openAi;

    record RoutingDecision(String worker, String reasoning) {
    }

    public Orchestrator(Map<String, Worker> workers, OpenAiClientWrapper openAi) {
        this.workers = workers;
        this.openAi = openAi;
    }

    public AgentResult route(String userInput, ConversationState state) throws Exception {
        state.addMessage("User: " + userInput);

        // Build rich routing context
        String routingPrompt = buildRoutingPrompt(userInput, state);

        // Get LLM routing decision
        RoutingDecision decision = openAi.callModel(
                routingPrompt,
                "gpt-5-mini",
                RoutingDecision.class
        );

        // Validate and fallback
        String selectedWorker = decision.worker();
        if (!workers.containsKey(selectedWorker)) {
            selectedWorker = "generalWorker";
        }

        // Execute worker
        Worker worker = workers.get(selectedWorker);
        WorkerResponse response = worker.handle(userInput, state);

        // Handle worker's recommendation for next worker
        if (response.confidence() < 0.5 && response.next_recommended_worker() != null) {
            // Worker is not confident, try recommended worker
            Worker nextWorker = workers.get(response.next_recommended_worker());
            if (nextWorker != null) {
                response = nextWorker.handle(userInput, state);
                selectedWorker = response.next_recommended_worker();
            }
        }

        // Update state
        state.addMessage("Agent: " + response.message());
        if (response.slots() != null && !response.slots().isEmpty()) {
            response.slots().forEach(state::putSlot);
        }

        // Adjust interest with bounds checking
        int currentInterest = state.getInterestScore();
        int delta = (int) Math.round(response.lead_interest() * 10);  // Scale to -10 to +10
        int newInterest = Math.max(0, Math.min(10, currentInterest + delta));
        state.setInterestScore(newInterest);
        state.setCurrentStage(response.stage());

        // Build result
        AgentResult result = new AgentResult();
        result.setReplyAgent(selectedWorker);
        result.setSalesStage(response.stage());
        result.setInterest(newInterest);
        result.setMessage(response.message());

        return result;
    }

    private String buildRoutingPrompt(String userInput, ConversationState state) {
        return String.format("""
                        # Router Instructions
                        Select the BEST worker to handle this user message.
                        
                        ## Context
                        - User message: "%s"
                        - Current interest: %d/10
                        - Current stage: %s
                        - Extracted slots: %s
                        - Last 3 messages: %s
                        
                        ## Available Workers
                        - generalWorker: Initial contact, discovery, general questions
                        - priceComparisonWorker: Price concerns, budget questions, value comparisons
                        - objectionWorker: Objections, hesitations, concerns, "need to think"
                        - closingWorker: High interest, buying signals, ready to purchase
                        
                        ## Routing Logic
                        1. If user mentions price/cost/expensive → "priceComparisonWorker"
                        2. If user shows objection/concern/hesitation → "objectionWorker"
                        3. If interest ≥7 OR user asks "how to buy/next steps" → "closingWorker"
                        4. Otherwise → "generalWorker"
                        
                        ## Special Cases
                        - If user says "not interested" → "generalWorker" (for graceful exit)
                        - If conversation is stuck (same objection 2+ times) → "objectionWorker"
                        
                        Respond with JSON: {"worker": "name", "reasoning": "why"}
                        """,
                userInput,
                state.getInterestScore(),
                state.getCurrentStage(),
                state.getSlots(),
                state.getHistory(3)
        );
    }

    public Map<String, Worker> getWorkers() {
        return workers;
    }
}
