package com.getenrola.aidemo;

import com.getenrola.aidemo.agent.ConversationState;
import com.getenrola.aidemo.agent.Orchestrator;
import com.getenrola.aidemo.model.AgentResult;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class InteractiveConsoleRunner implements CommandLineRunner {

    private final Orchestrator orchestrator;

    public InteractiveConsoleRunner(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void run(String... args) throws Exception {
        ConversationState state = new ConversationState();

        try (Scanner scanner = new Scanner(System.in)) {
            printBanner();
            printWelcomeMessage();

            int turnCount = 0;
            while (true) {
                turnCount++;
                System.out.print("\n[Turn " + turnCount + "] You: ");
                String userInput = scanner.nextLine().trim();

                if (userInput.equalsIgnoreCase("quit") || userInput.equalsIgnoreCase("exit")) {
                    System.out.println("\nConversation ended.");
                    break;
                }

                if (userInput.isEmpty()) {
                    turnCount--;
                    continue;
                }

                try {
                    AgentResult result = orchestrator.route(userInput, state);
                    printDetailedResult(result, state, turnCount);

                    if (result.getSalesStage().equals("closing") && result.getInterest() >= 9) {
                        System.out.println("\n🎉 Congratulations! The customer shows strong purchase intent!");
                        if (shouldContinue(scanner)) {
                            break;
                        }
                    }

                    if (result.getInterest() <= 2) {
                        System.out.println("\n❌ Customer interest is too low, may need to adjust strategy");
                        if (shouldContinue(scanner)) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            printFinalSummary(state);
        }
    }

    private boolean shouldContinue(Scanner scanner) {
        System.out.print("\nContinue conversation? (y/n): ");
        String cont = scanner.nextLine().trim();
        return !cont.equalsIgnoreCase("y");
    }

    private void printBanner() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Interactive Sales Conversation Console");
        System.out.println("Type 'quit' or 'exit' to exit");
        System.out.println("=".repeat(80) + "\n");
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

    private void printDetailedResult(AgentResult result, ConversationState state, int turn) {
        System.out.println("\n" + "─".repeat(80));
        System.out.println("🤖 Agent reply:");
        System.out.println("   " + result.getMessage());

        System.out.println("\n📊 System status:");
        System.out.printf("   ├─ Worker: %s%n", result.getReplyAgent());
        System.out.printf("   ├─ Stage: %s%n", result.getSalesStage());
        System.out.printf("   ├─ Interest: %d/10 %s%n", result.getInterest(), getInterestBar(result.getInterest()));
        System.out.printf("   └─ Total Turns: %d%n", turn);

        if (!state.getSlots().isEmpty()) {
            System.out.println("\n🎯 Customer info:");
            state.getSlots().forEach((key, value) -> System.out.printf("   ├─ %s: %s%n", key, value));
        }

        if (turn > 1) {
            System.out.println("\n📈 Interest trend: " + getInterestTrend(state));
        }
    }

    private String getInterestTrend(ConversationState state) {
        int current = state.getInterestScore();
        return "📊 (Need to implement history tracking)";
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
            state.getSlots().forEach((key, value) -> System.out.printf("   ✓ %s: %s%n", key, value));
        }

        System.out.println("\n📊 Final Status:");
        System.out.printf("   Interest Score: %d/10 %s%n", state.getInterestScore(), getInterestBar(state.getInterestScore()));
        System.out.printf("   Stage: %s%n", state.getCurrentStage());
        System.out.printf("   Total Messages: %d%n", state.getHistory().size());

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
        String bar = "█".repeat(Math.max(0, interest)) + "░".repeat(Math.max(0, empty));

        if (interest >= 8) return "🟢 " + bar + " (High)";
        if (interest >= 6) return "🟡 " + bar + " (Medium)";
        if (interest >= 4) return "🟠 " + bar + " (Low-Medium)";
        return "🔴 " + bar + " (Low)";
    }
}
