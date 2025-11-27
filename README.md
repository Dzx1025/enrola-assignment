# 🖊️ AI Sales Agent Demo

A Spring Boot application implementing an intelligent AI sales agent capable of selling a pen through a natural language conversation. This project demonstrates advanced AI patterns including agent orchestration, state management, and structured output using Spring AI and OpenAI.

## 🏗 Architecture

The project uses an **Orchestrator-Worker** pattern to manage the sales conversation effectively:

- **Orchestrator**: The central brain that analyzes user input, conversation history, and current sentiment to route the request to the most appropriate specialist worker.
- **Workers**: Specialized agents focused on specific domains:
  - `GeneralWorker`: Handles discovery and general conversation.
  - `PriceComparisonWorker`: Addresses budget concerns and value propositions.
  - `ObjectionWorker`: Skilled at handling hesitation and reframing concerns.
  - `ClosingWorker`: Focuses on finalizing the sale when buying signals are strong.
- **State Management**: A `ConversationState` object persists across the session, tracking:
  - Conversation History
  - Customer Interest Score (0-10)
  - Current Sales Stage
  - Extracted Information (Slots)

## ✨ Key Features

- **Dynamic Routing**: Uses an LLM to intelligently decide which worker should respond based on context.
- **Sentiment Analysis**: Real-time tracking of customer interest levels to adjust strategy.
- **Structured Output**: Leverages JSON schemas to ensure reliable decision-making data from the LLM.
- **Rich Console UI**: An interactive terminal interface with color-coded status panels, interest bars, and debug insights.

## 🚀 Getting Started

### Prerequisites
- Java 21+
- OpenAI API Key

### Installation & Run

1. **Clone the repository**
2. **Set your OpenAI API Key**:
   ```bash
   export OPENAI_API_KEY=sk-your-key-here
   ```
3. **Run the application**:
   ```bash
   ./mvnw spring-boot:run
   ```

### Running Tests
To run the evaluation suite:
```bash
./mvnw test
```

## 📂 Project Structure

- `src/main/java/.../agent/`
  - `Orchestrator.java`: Main routing logic.
  - `worker/`: Implementation of specialized workers.
  - `ConversationState.java`: Session state model.
- `InteractiveConsoleRunner.java`: The CLI entry point and UI renderer.

---
*Built with Spring Boot 3.5.7 and Spring AI 1.1.0*
