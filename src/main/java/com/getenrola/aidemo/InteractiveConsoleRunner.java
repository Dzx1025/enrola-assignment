package com.getenrola.aidemo;

import com.getenrola.aidemo.model.ConversationState;
import com.getenrola.aidemo.agent.Orchestrator;
import com.getenrola.aidemo.model.AgentResult;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

@Component
@Profile("cli")
public class InteractiveConsoleRunner implements CommandLineRunner {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_DIM = "\u001B[2m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_MAGENTA = "\u001B[35m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BLUE = "\u001B[34m";

    private static final Pattern ANSI_ESCAPE = Pattern.compile("\u001B\\[[;\\d]*m");
    private static final int PANEL_WIDTH = 74;
    private static final int CONTENT_WIDTH = PANEL_WIDTH - 2;
    private static final int KEY_WIDTH = 18;

    private final Orchestrator orchestrator;

    public InteractiveConsoleRunner(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void run(String... args) {
        ConversationState state = new ConversationState();

        try (Scanner scanner = new Scanner(System.in)) {
            printBanner();

            int turnCount = 0;
            while (true) {
                turnCount++;
                System.out.print(ANSI_BLUE + "\n[Turn " + turnCount + "] You: " + ANSI_RESET);
                String userInput = scanner.nextLine().trim();

                if (userInput.equalsIgnoreCase("quit") || userInput.equalsIgnoreCase("exit")) {
                    System.out.println(ANSI_DIM + "\nConversation ended." + ANSI_RESET);
                    break;
                }

                if (userInput.isEmpty()) {
                    turnCount--;
                    continue;
                }

                try {
                    AgentResult result = orchestrator.route(userInput, state);
                    printDetailedResult(result, state, turnCount);

                    if (result.getSalesStage().equalsIgnoreCase("closing") && result.getInterest() >= 9) {
                        System.out.println(ANSI_GREEN + "\n🎉 Congratulations! The customer shows strong purchase intent!" + ANSI_RESET);
                        if (!userWantsToContinue(scanner)) {
                            break;
                        }
                    }

                    if (result.getInterest() <= 2) {
                        System.out.println(ANSI_RED + "\n❌ Customer interest is too low, may need to adjust strategy" + ANSI_RESET);
                        if (!userWantsToContinue(scanner)) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.err.println(ANSI_RED + "Error: " + e.getMessage() + ANSI_RESET);
                    e.printStackTrace();
                }
            }

            printFinalSummary(state);
        }
    }

    private boolean userWantsToContinue(Scanner scanner) {
        System.out.print(ANSI_YELLOW + "\nContinue conversation? (y/n): " + ANSI_RESET);
        String cont = scanner.nextLine().trim();
        return cont.equalsIgnoreCase("y");
    }

    private void printBanner() {
        String border = "═".repeat(70);
        System.out.println("\n" + ANSI_MAGENTA + "╔" + border + "╗" + ANSI_RESET);
        System.out.println(ANSI_MAGENTA + "║" + ANSI_RESET + ANSI_BOLD + centerText("AI SALES AGENT CONSOLE", 70) + ANSI_RESET + ANSI_MAGENTA + "║" + ANSI_RESET);
        System.out.println(ANSI_MAGENTA + "║" + ANSI_RESET + centerText("Type 'quit' or 'exit' to leave the simulation", 70) + ANSI_MAGENTA + "║" + ANSI_RESET);
        System.out.println(ANSI_MAGENTA + "╚" + border + "╝" + ANSI_RESET + "\n");
    }

    private void printDetailedResult(AgentResult result, ConversationState state, int turn) {
        printPanelHeader("🤖 Agent Reply", ANSI_CYAN);
        wrapLines(result.getMessage(), CONTENT_WIDTH).forEach(line -> printPanelContentLine(ANSI_CYAN, line));
        printPanelFooter(ANSI_CYAN);

        printPanelHeader("📊 System Status", ANSI_BLUE);
        printKeyValueLine(ANSI_BLUE, "Worker:", accent(result.getReplyAgent()));
        printKeyValueLine(ANSI_BLUE, "Stage:", colorizeStage(result.getSalesStage()));
        printKeyValueLine(ANSI_BLUE, "Interest:", getInterestBar(result.getInterest()));
        printKeyValueLine(ANSI_BLUE, "Total Turns:", String.valueOf(turn));
        printPanelFooter(ANSI_BLUE);

        printPanelHeader("🎯 Customer Insights", ANSI_MAGENTA);
        if (state.getSlots().isEmpty()) {
            printPanelContentLine(ANSI_MAGENTA, "No structured customer info captured yet.");
        } else {
            state.getSlots().forEach((key, value) -> printWrappedKeyValueLine(ANSI_MAGENTA, key + ":", String.valueOf(value)));
        }
        printPanelFooter(ANSI_MAGENTA);

        if (turn > 1) {
            printPanelHeader("📈 Interest Trend", ANSI_YELLOW);
            printPanelContentLine(ANSI_YELLOW, getInterestTrend(state));
            printPanelFooter(ANSI_YELLOW);
        }
    }

    private String getInterestTrend(ConversationState state) {
        return "Trend tracking placeholder (latest score: " + state.getInterestScore() + ")";
    }

    private void printFinalSummary(ConversationState state) {
        System.out.println(ANSI_MAGENTA + "\n" + "═".repeat(80) + ANSI_RESET);
        System.out.println(ANSI_BOLD + "📋 Conversation Summary" + ANSI_RESET);
        System.out.println(ANSI_MAGENTA + "═".repeat(80) + ANSI_RESET);

        printPanelHeader("💬 Full Conversation", ANSI_CYAN);
        if (state.getHistory().isEmpty()) {
            printPanelContentLine(ANSI_CYAN, "No exchanges recorded.");
        } else {
            state.getHistory().forEach(msg -> {
                boolean isUser = msg.startsWith("User:");
                String body = isUser ? msg.substring(5).trim() : msg.substring(6).trim();
                String prefix = isUser ? "👤 " : "🤖 ";
                List<String> chunks = wrapLines(body, CONTENT_WIDTH - prefix.length());
                for (int i = 0; i < chunks.size(); i++) {
                    String appliedPrefix = i == 0 ? prefix : "   ";
                    printPanelContentLine(ANSI_CYAN, appliedPrefix + chunks.get(i));
                }
            });
        }
        printPanelFooter(ANSI_CYAN);

        printPanelHeader("🎯 Collected Info", ANSI_GREEN);
        if (state.getSlots().isEmpty()) {
            printPanelContentLine(ANSI_GREEN, "(No information collected)");
        } else {
            state.getSlots().forEach((key, value) -> printWrappedKeyValueLine(ANSI_GREEN, key + ":", String.valueOf(value)));
        }
        printPanelFooter(ANSI_GREEN);

        printPanelHeader("📊 Final Status", ANSI_BLUE);
        printKeyValueLine(ANSI_BLUE, "Interest Score:", getInterestBar(state.getInterestScore()));
        printKeyValueLine(ANSI_BLUE, "Stage:", colorizeStage(state.getCurrentStage()));
        printKeyValueLine(ANSI_BLUE, "Total Messages:", String.valueOf(state.getHistory().size()));
        printPanelFooter(ANSI_BLUE);

        printPanelHeader("🎯 Sales Result", ANSI_MAGENTA);
        int finalInterest = state.getInterestScore();
        String stage = state.getCurrentStage();
        if (stage.equalsIgnoreCase("closing") && finalInterest >= 8) {
            printPanelContentLine(ANSI_MAGENTA, "✅ Success! Customer is ready to buy");
        } else if (finalInterest >= 6) {
            printPanelContentLine(ANSI_MAGENTA, "⚠️  Potential, needs further follow-up");
        } else if (finalInterest >= 4) {
            printPanelContentLine(ANSI_MAGENTA, "⏸️  Neutral, needs more effort");
        } else {
            printPanelContentLine(ANSI_MAGENTA, "❌ At risk of loss, suggest adjusting strategy");
        }
        printPanelFooter(ANSI_MAGENTA);

        System.out.println(ANSI_MAGENTA + "=".repeat(80) + ANSI_RESET + "\n");
    }

    private void printPanelHeader(String title, String color) {
        System.out.println("\n" + color + "┏" + "━".repeat(PANEL_WIDTH) + "┓" + ANSI_RESET);
        printPanelContentLine(color, ANSI_BOLD + title + ANSI_RESET);
        System.out.println(color + "┣" + "━".repeat(PANEL_WIDTH) + "┫" + ANSI_RESET);
    }

    private void printPanelFooter(String color) {
        System.out.println(color + "┗" + "━".repeat(PANEL_WIDTH) + "┛" + ANSI_RESET);
    }

    private void printPanelContentLine(String color, String content) {
        String safeContent = content == null ? "" : content;
        System.out.println(color + "┃ " + ANSI_RESET + padRight(safeContent, CONTENT_WIDTH) + color + " ┃" + ANSI_RESET);
    }

    private void printKeyValueLine(String color, String key, String value) {
        printPanelContentLine(color, formatKeyValue(key, value));
    }

    private void printWrappedKeyValueLine(String color, String key, String value) {
        List<String> segments = wrapLines(value == null ? "" : value, CONTENT_WIDTH - KEY_WIDTH - 1);
        if (segments.isEmpty()) {
            segments.add("");
        }
        for (int i = 0; i < segments.size(); i++) {
            String label = i == 0 ? key : "";
            printPanelContentLine(color, formatKeyValue(label, segments.get(i)));
        }
    }

    private String formatKeyValue(String key, String value) {
        String safeValue = value == null ? "" : value;
        String label;
        if (key == null || key.isEmpty()) {
            label = " ".repeat(KEY_WIDTH);
        } else {
            label = String.format("%-" + KEY_WIDTH + "s", key);
        }
        return label + " " + safeValue;
    }

    private List<String> wrapLines(String text, int width) {
        int adjustedWidth = Math.max(10, width);
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        for (String rawLine : text.split("\\R")) {
            String remaining = rawLine.trim();
            if (remaining.isEmpty()) {
                lines.add("");
                continue;
            }
            while (remaining.length() > adjustedWidth) {
                int breakIndex = remaining.lastIndexOf(' ', adjustedWidth);
                if (breakIndex <= 0) {
                    lines.add(remaining.substring(0, adjustedWidth));
                    remaining = remaining.substring(adjustedWidth).trim();
                } else {
                    lines.add(remaining.substring(0, breakIndex));
                    remaining = remaining.substring(breakIndex + 1);
                }
            }
            lines.add(remaining);
        }

        if (lines.isEmpty()) {
            lines.add("");
        }
        return lines;
    }

    private String padRight(String text, int width) {
        String safe = text == null ? "" : text;
        String stripped = stripAnsi(safe);
        if (stripped.length() >= width) {
            return safe;
        }
        return safe + " ".repeat(width - stripped.length());
    }

    private String stripAnsi(String value) {
        return ANSI_ESCAPE.matcher(value == null ? "" : value).replaceAll("");
    }

    private void printBullet(String text) {
        System.out.println("  " + ANSI_GREEN + "• " + ANSI_RESET + text);
    }

    private String accent(String text) {
        return ANSI_BOLD + text + ANSI_RESET;
    }

    private String colorizeStage(String stage) {
        if (stage == null) {
            return "unknown";
        }
        return switch (stage.toLowerCase()) {
            case "closing" -> ANSI_GREEN + stage + ANSI_RESET;
            case "objection" -> ANSI_YELLOW + stage + ANSI_RESET;
            case "price" -> ANSI_BLUE + stage + ANSI_RESET;
            default -> ANSI_CYAN + stage + ANSI_RESET;
        };
    }

    private String getInterestBar(int interest) {
        int clamped = Math.max(0, Math.min(10, interest));
        int empty = 10 - clamped;
        String bar = "█".repeat(clamped) + "░".repeat(empty);

        if (clamped >= 8) return ANSI_GREEN + bar + " (High)" + ANSI_RESET;
        if (clamped >= 6) return ANSI_YELLOW + bar + " (Medium)" + ANSI_RESET;
        if (clamped >= 4) return ANSI_BLUE + bar + " (Low-Medium)" + ANSI_RESET;
        return ANSI_RED + bar + " (Low)" + ANSI_RESET;
    }

    private String centerText(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - padding - text.length());
    }
}
