package com.getenrola.aidemo.agent;

import com.getenrola.aidemo.model.AgentResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


import java.util.Scanner;

@SpringBootTest
class OrchestratorTest {
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
        assertTrue(r1.getInterest() >= 3 && r1.getInterest() <= 7,
                "Initial interest should be between 3 and 7");

        // Step 2: Needs Exploration
        AgentResult r2 = executeAndLog(
                "I need a pen for signing important business contracts",
                state,
                "Step 2 - Needs Exploration"
        );
        assertNotNull(r2);
        assertTrue(state.getSlots().containsKey("purpose"));
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
    // 4. Interactive Console Test - Manual Conversation
    // ========================================================================
    @Test
    @DisplayName("Interactive Test - Manual Conversation")
    void interactiveConsoleTest() throws Exception {
        ConversationState state = new ConversationState();
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n" + "=".repeat(80));
        System.out.println("Interactive Sales Conversation Test");
        System.out.println("Type 'quit' or 'exit' to exit");
        System.out.println("=".repeat(80) + "\n");

        printWelcomeMessage();

        int turnCount = 0;
        while (true) {
            turnCount++;
            System.out.print("\n[Turn " + turnCount + "] You: ");
            String userInput = scanner.nextLine().trim();

            if (userInput.equalsIgnoreCase("quit") ||
                    userInput.equalsIgnoreCase("exit")) {
                System.out.println("\nConversation ended.");
                break;
            }

            if (userInput.isEmpty()) {
                continue;
            }

            try {
                AgentResult result = orchestrator.route(userInput, state);
                printDetailedResult(result, state, turnCount);

                // Check if reached closing
                if (result.getSalesStage().equals("closing") &&
                        result.getInterest() >= 9) {
                    System.out.println("\n🎉 Congratulations! The customer shows strong purchase intent!");
                    System.out.print("\nContinue conversation? (y/n): ");
                    String cont = scanner.nextLine().trim();
                    if (!cont.equalsIgnoreCase("y")) {
                        break;
                    }
                }

                if (result.getInterest() <= 2) {
                    System.out.println("\n❌ Customer interest is too low, may need to adjust strategy");
                    System.out.print("\nContinue conversation? (y/n): ");
                    String cont = scanner.nextLine().trim();
                    if (!cont.equalsIgnoreCase("y")) {
                        break;
                    }
                }

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        printFinalSummary(state);
        scanner.close();
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

    private void printDetailedResult(
            AgentResult result,
            ConversationState state,
            int turn
    ) {
        System.out.println("\n" + "─".repeat(80));
        System.out.println("🤖 Agent reply:");
        System.out.println("   " + result.getMessage());

        System.out.println("\n📊 System status:");
        System.out.println(String.format(
                "   ├─ Worker: %s",
                result.getReplyAgent()
        ));
        System.out.println(String.format(
                "   ├─ Stage: %s",
                result.getSalesStage()
        ));
        System.out.println(String.format(
                "   ├─ Interest: %d/10 %s",
                result.getInterest(),
                getInterestBar(result.getInterest())
        ));
        System.out.println(String.format(
                "   └─ Total Turns: %d",
                turn
        ));

        if (!state.getSlots().isEmpty()) {
            System.out.println("\n🎯 Customer info:");
            state.getSlots().forEach((key, value) ->
                    System.out.println(String.format("   ├─ %s: %s", key, value))
            );
        }

        // Interest trend
        if (turn > 1) {
            System.out.println("\n📈 Interest trend: " + getInterestTrend(state));
        }
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
                    System.out.println(String.format("   ✓ %s: %s", key, value))
            );
        }

        System.out.println("\n📊 Final Status:");
        System.out.println(String.format(
                "   Interest Score: %d/10 %s",
                state.getInterestScore(),
                getInterestBar(state.getInterestScore())
        ));
        System.out.println(String.format(
                "   Stage: %s",
                state.getCurrentStage()
        ));
        System.out.println(String.format(
                "   Total Messages: %d",
                state.getHistory().size()
        ));

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

    private void printWelcomeMessage() {
        System.out.println("You will play the role of a potential customer and converse with the AI sales agent.");
        System.out.println("Try different replies and observe how the AI responds:");
        System.out.println("  • Ask about price");
        System.out.println("  • Express objections");
        System.out.println("  • Show purchase intent");
        System.out.println("  • Bargain");
        System.out.println("\nThe system will display in real time:");
        System.out.println("  • Worker selection (generalWorker/priceComparisonWorker/objectionWorker/closingWorker)");
        System.out.println("  • Interest score changes");
        System.out.println("  • Sales stage");
        System.out.println("  • Extracted customer info");
    }

    private String getInterestBar(int interest) {
        int filled = interest;
        int empty = 10 - interest;
        String bar = "█".repeat(filled) + "░".repeat(empty);

        if (interest >= 8) return "🟢 " + bar + " (High)";
        if (interest >= 6) return "🟡 " + bar + " (Medium)";
        if (interest >= 4) return "🟠 " + bar + " (Low-Medium)";
        return "🔴 " + bar + " (Low)";
    }

    private String getInterestTrend(ConversationState state) {
        // Simplified: Compare the most recent two interest scores
        // In actual implementation, you need to track historical scores in ConversationState
        int current = state.getInterestScore();
        // Assume there is a getPreviousInterest() method
        // int previous = state.getPreviousInterest();

        // Placeholder implementation
        return "📊 (Need to implement history tracking)";
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

