package com.getenrola.aidemo.agent;

import com.getenrola.aidemo.model.AgentResult;
import com.getenrola.aidemo.model.ConversationState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class MultiAgentIntegrationTest {
    @Autowired
    private Orchestrator orchestrator;
    @Autowired
    private OpenAiClientWrapper openAiService;

    // ========================================================================
    // 1. Preset Scenario Test - Full Sales Journey
    // ========================================================================
    @Test
    @DisplayName("Full Sales Journey - From Initial Contact to Closing")
    void testFullSalesJourney() throws Exception {
        ConversationState state = new ConversationState();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("Test Scenario: Full Sales Journey");
        System.out.println("=".repeat(80) + "\n");

        // Step 1: Initial Contact
        AgentResult r1 = executeAndLog(
                "Hi, I'm looking for a special pen",
                state,
                "Step 1 - Initial Contact"
        );
        assertNotNull(r1);
        assertEquals("generalWorker", r1.getReplyAgent());

        // Step 2: Needs Exploration
        AgentResult r2 = executeAndLog(
                "I need a pen for signing important business contracts",
                state,
                "Step 2 - Needs Exploration"
        );
        assertNotNull(r2);
        assertTrue(r2.getInterest() > r1.getInterest(),
                "Interest should increase after expressing a clear need");

        // Step 3: Price Inquiry
        AgentResult r3 = executeAndLog(
                "How much does it cost?",
                state,
                "Step 3 - Price Inquiry"
        );
        assertNotNull(r3);
        assertEquals("priceComparisonWorker", r3.getReplyAgent(),
                "Price-related questions should be routed to price worker");

        // Step 4: Price Objection
        AgentResult r4 = executeAndLog(
                "That's pretty expensive for a pen",
                state,
                "Step 4 - Price Objection"
        );
        assertNotNull(r4);
        assertTrue(r4.getReplyAgent().equals("priceComparisonWorker") ||
                r4.getReplyAgent().equals("objectionWorker"));

        // Step 5: Considering
        AgentResult r5 = executeAndLog(
                "I need to think about it",
                state,
                "Step 5 - Considering"
        );
        assertNotNull(r5);
        assertEquals("objectionWorker", r5.getReplyAgent());

        // Step 6: Buying Signal
        AgentResult r6 = executeAndLog(
                "Actually, you know what, I'm interested. How do I buy it?",
                state,
                "Step 6 - Buying Signal"
        );
        assertNotNull(r6);
        assertEquals("closingWorker", r6.getReplyAgent());
        assertTrue(r6.getInterest() >= 7, "Interest should be ≥7 when buying signal appears");

        // Step 7: Closing
        AgentResult r7 = executeAndLog(
                "Perfect, send me the purchase link",
                state,
                "Step 7 - Closing"
        );
        assertNotNull(r7);
        assertEquals("closingWorker", r7.getReplyAgent());
        assertEquals("closing", r7.getSalesStage());

        printFinalSummary(state);
    }

    // ========================================================================
    // 2. Price Sensitive Customer Test
    // ========================================================================
    @Test
    @DisplayName("Price Sensitive Customer - Multiple Price Objections")
    void testPriceSensitiveCustomer() throws Exception {
        ConversationState state = new ConversationState();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("Test Scenario: Price Sensitive Customer");
        System.out.println("=".repeat(80) + "\n");

        executeAndLog("Hi", state, "Initial Contact");
        executeAndLog("Tell me about this pen", state, "Product Inquiry");

        AgentResult r1 = executeAndLog(
                "How much?",
                state,
                "Price Inquiry"
        );
        assertEquals("priceComparisonWorker", r1.getReplyAgent());

        AgentResult r2 = executeAndLog(
                "$5000?? That's way too expensive!",
                state,
                "Price Shock"
        );
        assertTrue(r2.getInterest() < r1.getInterest(), "Interest should decrease after price objection");

        AgentResult r3 = executeAndLog(
                "Can you do $3000?",
                state,
                "Attempt to Bargain"
        );
        assertTrue(r3.getReplyAgent().equals("priceComparisonWorker") ||
                r3.getReplyAgent().equals("objectionWorker"));

        AgentResult r4 = executeAndLog(
                "I can't justify spending that much on a pen",
                state,
                "Objection Again"
        );
        assertTrue(state.getSlots().containsKey("objections"));

        printFinalSummary(state);
    }

    // ========================================================================
    // 3. Quick Closer Test
    // ========================================================================
    @Test
    @DisplayName("Quick Closer Customer - High Purchase Intent")
    void testQuickCloser() throws Exception {
        ConversationState state = new ConversationState();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("Test Scenario: Quick Closer Customer");
        System.out.println("=".repeat(80) + "\n");

        AgentResult r1 = executeAndLog(
                "I'm looking for a one-of-a-kind luxury pen for my collection",
                state,
                "Clear High Value Need"
        );
        assertTrue(r1.getInterest() >= 6);

        AgentResult r2 = executeAndLog(
                "Tell me the price and specs",
                state,
                "Direct Inquiry"
        );
        assertNotNull(r2);

        AgentResult r3 = executeAndLog(
                "$5000 is fine. I collect luxury items. How do I purchase?",
                state,
                "Accepts Price and Asks to Buy"
        );
        assertEquals("closingWorker", r3.getReplyAgent());
        assertTrue(r3.getInterest() >= 8);

        printFinalSummary(state);
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private AgentResult executeAndLog(
            String userInput,
            ConversationState state,
            String stepDescription
    ) throws Exception {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("📍 " + stepDescription);
        System.out.println("-".repeat(80));
        System.out.println("👤 User: " + userInput);

        AgentResult result = orchestrator.route(userInput, state);

        System.out.println("\n🤖 Agent reply:");
        System.out.println("   " + result.getMessage());
        System.out.println("\n📊 Status info:");
        System.out.println("   Worker: " + result.getReplyAgent());
        System.out.println("   Stage: " + result.getSalesStage());
        System.out.println("   Interest: " + result.getInterest() + "/10 " +
                getInterestBar(result.getInterest()));

        if (!state.getSlots().isEmpty()) {
            System.out.println("\n🎯 Extracted slots:");
            state.getSlots().forEach((key, value) ->
                    System.out.println("   - " + key + ": " + value)
            );
        }

        return result;
    }

    private void printFinalSummary(ConversationState state) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📋 Conversation Summary");
        System.out.println("=".repeat(80));

        System.out.println("\n💬 Full Conversation History:");
        state.getHistory().forEach(msg -> {
            if (msg.startsWith("User:")) {
                System.out.println("👤 " + msg.substring(5).trim());
            } else {
                System.out.println("🤖 " + msg.substring(6).trim());
            }
        });

        System.out.println("\n🎯 Collected Customer Info:");
        if (state.getSlots().isEmpty()) {
            System.out.println("   (No information collected)");
        } else {
            state.getSlots().forEach((key, value) ->
                    System.out.printf("   ✓ %s: %s%n", key, value)
            );
        }

        System.out.println("\n📊 Final Status:");
        System.out.printf(
                "   Interest Score: %d/10 %s%n",
                state.getInterestScore(),
                getInterestBar(state.getInterestScore())
        );
        System.out.printf(
                "   Stage: %s%n",
                state.getCurrentStage()
        );
        System.out.printf(
                "   Total Messages: %d%n",
                state.getHistory().size()
        );

        // Result determination
        System.out.println("\n🎯 Sales Result:");
        int finalInterest = state.getInterestScore();
        String stage = state.getCurrentStage();

        if (stage.equals("closing") && finalInterest >= 8) {
            System.out.println("   ✅ Success! Customer is ready to buy");
        } else if (finalInterest >= 6) {
            System.out.println("   ⚠️  Potential, needs further follow-up");
        } else if (finalInterest >= 4) {
            System.out.println("   ⏸️  Neutral, needs more effort");
        } else {
            System.out.println("   ❌ At risk of loss, suggest adjusting strategy");
        }

        System.out.println("\n" + "=".repeat(80) + "\n");
    }

    private String getInterestBar(int interest) {
        int empty = 10 - interest;
        String bar = "█".repeat(interest) + "░".repeat(empty);

        if (interest >= 8) return "🟢 " + bar + " (High)";
        if (interest >= 6) return "🟡 " + bar + " (Medium)";
        if (interest >= 4) return "🟠 " + bar + " (Low-Medium)";
        return "🔴 " + bar + " (Low)";
    }

    // ========================================================================
    // 5. Edge Case Tests
    // ========================================================================

    @Test
    @DisplayName("Edge Test - Interest Score Bounds")
    void testInterestScoreBounds() throws Exception {
        ConversationState state = new ConversationState();

        // Test upper bound
        state.setInterestScore(9);
        AgentResult r1 = executeAndLog(
                "I love it! Send me the link right now!",
                state,
                "Test Upper Bound"
        );
        assertTrue(r1.getInterest() <= 10, "Interest score should not exceed 10");

        // Test lower bound
        state.setInterestScore(1);
        AgentResult r2 = executeAndLog(
                "This is ridiculous, I'm not interested at all",
                state,
                "Test Lower Bound"
        );
        assertTrue(r2.getInterest() >= 0, "Interest score should not be below 0");
    }

    @Test
    @DisplayName("Worker Routing Accuracy Test")
    void testWorkerRouting() throws Exception {
        ConversationState state = new ConversationState();

        // Price related
        AgentResult r1 = orchestrator.route("How much does it cost?", state);
        assertEquals("priceComparisonWorker", r1.getReplyAgent(), "Price questions should be routed to priceComparisonWorker");

        // Objection related
        AgentResult r2 = orchestrator.route("I'm not sure about this", state);
        assertEquals("objectionWorker", r2.getReplyAgent(), "Hesitation should be routed to objectionWorker");

        // Closing related
        state.setInterestScore(8);
        AgentResult r3 = orchestrator.route("How do I buy this?", state);
        assertEquals("closingWorker", r3.getReplyAgent(), "Purchase inquiry should be routed to closing");
    }
}

