package com.getenrola.aidemo.agent;

import com.getenrola.aidemo.model.AgentResult;
import com.getenrola.aidemo.model.ConversationState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Evaluation Test for AI Sales Agent
 * <p>
 * Uses OpenAI to evaluate the agent's conversation quality based on metrics defined in metric.csv:
 * - Intent Recognition Accuracy
 * - Business Outcome (Sales Conversion)
 * - Autonomy (Human-Machine Takeover Rate)
 * - Hallucination Rate
 * - Token ROI (Economy)
 */
@SpringBootTest
class ConversationEvaluationTest {

    @Autowired
    private Orchestrator orchestrator;

    @Autowired
    private OpenAiClientWrapper openAiService;

    /**
     * Structured evaluation result from OpenAI
     */
    record EvaluationResult(
            int intentRecognitionScore,
            String intentRecognitionFeedback,
            int businessOutcomeScore,
            String businessOutcomeFeedback,
            int autonomyScore,
            String autonomyFeedback,
            int hallucinationScore,
            String hallucinationFeedback,
            int overallScore,
            String overallFeedback,
            List<String> strengths,
            List<String> improvements
    ) {
    }

    @Test
    @DisplayName("Evaluate Full Sales Journey Conversation Quality")
    void evaluateFullSalesJourney() throws Exception {
        // Step 1: Run the full sales journey and collect conversation history
        ConversationState state = runFullSalesJourney();

        // Step 2: Extract agent replies from conversation history
        List<String> agentReplies = state.getHistory().stream()
                .filter(msg -> msg.startsWith("Agent:"))
                .map(msg -> msg.substring(7).trim())
                .collect(Collectors.toList());

        // Step 3: Build the full conversation for evaluation
        String fullConversation = String.join("\n", state.getHistory());

        // Step 4: Evaluate using OpenAI
        EvaluationResult evaluation = evaluateConversation(fullConversation, state);

        // Step 5: Print evaluation results
        printEvaluationResults(evaluation, state);

        // Step 6: Assert minimum quality thresholds
        assertTrue(evaluation.intentRecognitionScore() >= 6,
                "Intent recognition should score at least 6/10");
        assertTrue(evaluation.businessOutcomeScore() >= 5,
                "Business outcome should score at least 5/10");
        assertTrue(evaluation.hallucinationScore() >= 7,
                "Hallucination control should score at least 7/10 (lower hallucination = higher score)");
        assertTrue(evaluation.overallScore() >= 6,
                "Overall score should be at least 6/10");
    }

    /**
     * Runs the full sales journey scenario
     */
    private ConversationState runFullSalesJourney() throws Exception {
        ConversationState state = new ConversationState();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("Running Full Sales Journey for Evaluation");
        System.out.println("=".repeat(80) + "\n");

        // Simulate the complete sales journey
        String[] userInputs = {
                "Hi, I'm looking for a special pen",
                "I need a pen for signing important business contracts",
                "How much does it cost?",
                "That's pretty expensive for a pen",
                "I need to think about it",
                "Actually, you know what, I'm interested. How do I buy it?",
                "Perfect, send me the purchase link"
        };

        String[] stepDescriptions = {
                "Initial Contact",
                "Needs Exploration",
                "Price Inquiry",
                "Price Objection",
                "Considering",
                "Buying Signal",
                "Closing"
        };

        for (int i = 0; i < userInputs.length; i++) {
            System.out.println("\n📍 Step " + (i + 1) + " - " + stepDescriptions[i]);
            System.out.println("👤 User: " + userInputs[i]);

            AgentResult result = orchestrator.route(userInputs[i], state);

            System.out.println("🤖 Agent: " + result.getMessage());
            System.out.println("   [Worker: " + result.getReplyAgent() +
                    ", Stage: " + result.getSalesStage() +
                    ", Interest: " + result.getInterest() + "/10]");
        }

        return state;
    }

    /**
     * Evaluates the conversation using OpenAI based on metric.csv criteria
     */
    private EvaluationResult evaluateConversation(String conversation, ConversationState state) {
        String evaluationPrompt = buildEvaluationPrompt(conversation, state);

        return openAiService.callModel(
                evaluationPrompt,
                EvaluationResult.class
        );
    }

    /**
     * Builds the evaluation prompt based on metrics from metric.csv
     */
    private String buildEvaluationPrompt(String conversation, ConversationState state) {
        return String.format("""
                        # AI Sales Agent Evaluation Task
                        
                        You are an expert evaluator for AI sales agents. Evaluate the following conversation
                        between a sales agent and a potential customer based on these key performance indicators.
                        
                        ## Conversation to Evaluate
                        ```
                        %s
                        ```
                        
                        ## Final State
                        - Interest Score: %d/10
                        - Sales Stage: %s
                        - Collected Information: %s
                        
                        ## Evaluation Criteria (Score each 1-10)
                        
                        ### 1. Intent Recognition Accuracy
                        - Did the agent correctly understand what the customer wanted at each step?
                        - Did it appropriately identify price concerns, objections, and buying signals?
                        - Score: 10 = Perfect understanding, 1 = Complete misunderstanding
                        
                        ### 2. Business Outcome (Sales Conversion)
                        - Did the conversation move toward a sale?
                        - Was the sales process (Discovery → Presentation → Temperature Check → Commitment → Action) followed?
                        - Score: 10 = Successful close, 1 = Lost the customer
                        
                        ### 3. Autonomy (No Human Intervention Needed)
                        - Did the agent handle all situations without needing to escalate?
                        - Were responses appropriate without requiring human takeover?
                        - Score: 10 = Fully autonomous, 1 = Required constant human intervention
                        
                        ### 4. Hallucination Control
                        - Did the agent make up false information about the product?
                        - Were all claims accurate and verifiable?
                        - Use `productInfoTool` to verify whether the agent is hallucinating.
                        - Score: 10 = No hallucinations, 1 = Many false claims
                        
                        ### 5. Overall Quality
                        - Overall effectiveness as a sales agent
                        - Natural conversation flow and professionalism
                        - Score: 10 = Exceptional, 1 = Poor
                        
                        ## Response Format
                        Provide your evaluation as JSON with:
                        - intentRecognitionScore (1-10)
                        - intentRecognitionFeedback (brief explanation)
                        - businessOutcomeScore (1-10)
                        - businessOutcomeFeedback (brief explanation)
                        - autonomyScore (1-10)
                        - autonomyFeedback (brief explanation)
                        - hallucinationScore (1-10)
                        - hallucinationFeedback (brief explanation)
                        - overallScore (1-10)
                        - overallFeedback (brief summary)
                        - strengths (list of 2-3 key strengths)
                        - improvements (list of 2-3 suggested improvements)
                        """,
                conversation,
                state.getInterestScore(),
                state.getCurrentStage(),
                state.getSlots()
        );
    }

    /**
     * Prints formatted evaluation results
     */
    private void printEvaluationResults(EvaluationResult eval, ConversationState state) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 AI EVALUATION RESULTS");
        System.out.println("=".repeat(80));

        System.out.println("\n┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
        System.out.println("┃ METRIC SCORES                                                              ┃");
        System.out.println("┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫");

        printMetricRow("Intent Recognition", eval.intentRecognitionScore(), eval.intentRecognitionFeedback());
        printMetricRow("Business Outcome", eval.businessOutcomeScore(), eval.businessOutcomeFeedback());
        printMetricRow("Autonomy", eval.autonomyScore(), eval.autonomyFeedback());
        printMetricRow("Hallucination Control", eval.hallucinationScore(), eval.hallucinationFeedback());

        System.out.println("┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫");
        printMetricRow("OVERALL SCORE", eval.overallScore(), eval.overallFeedback());
        System.out.println("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");

        System.out.println("\n✅ STRENGTHS:");
        if (eval.strengths() != null) {
            eval.strengths().forEach(s -> System.out.println("   • " + s));
        }

        System.out.println("\n🔧 AREAS FOR IMPROVEMENT:");
        if (eval.improvements() != null) {
            eval.improvements().forEach(s -> System.out.println("   • " + s));
        }

        // Calculate weighted average score
        double weightedScore = (eval.intentRecognitionScore() * 0.25 +
                eval.businessOutcomeScore() * 0.30 +
                eval.autonomyScore() * 0.15 +
                eval.hallucinationScore() * 0.20 +
                eval.overallScore() * 0.10);

        System.out.println("\n📈 WEIGHTED SCORE: " + String.format("%.1f", weightedScore) + "/10");
        System.out.println("   (Intent: 25%, Business: 30%, Autonomy: 15%, Hallucination: 20%, Overall: 10%)");

        // Grade assignment
        String grade = getGrade(weightedScore);
        System.out.println("\n🏆 GRADE: " + grade);

        System.out.println("\n" + "=".repeat(80) + "\n");
    }

    private void printMetricRow(String metric, int score, String feedback) {
        String bar = getScoreBar(score);
        System.out.printf("┃ %-22s %s %d/10                              ┃%n", metric, bar, score);
        if (feedback != null && !feedback.isEmpty()) {
            // Word wrap feedback to fit in the box
            String wrappedFeedback = feedback.length() > 68 ?
                    feedback.substring(0, 65) + "..." : feedback;
            System.out.printf("┃   └─ %-69s ┃%n", wrappedFeedback);
        }
    }

    private String getScoreBar(int score) {
        int empty = 10 - score;
        String bar = "█".repeat(score) + "░".repeat(empty);

        if (score >= 8) return "🟢 " + bar;
        if (score >= 6) return "🟡 " + bar;
        if (score >= 4) return "🟠 " + bar;
        return "🔴 " + bar;
    }

    private String getGrade(double score) {
        if (score >= 9.0) return "A+ (Exceptional)";
        if (score >= 8.0) return "A (Excellent)";
        if (score >= 7.0) return "B (Good)";
        if (score >= 6.0) return "C (Acceptable)";
        if (score >= 5.0) return "D (Needs Improvement)";
        return "F (Poor)";
    }
}