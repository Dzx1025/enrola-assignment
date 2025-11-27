# 🖊️ AI Sales Agent Demo

A Spring Boot application implementing an intelligent AI sales agent capable of selling a pen through natural language conversation. This project demonstrates advanced AI patterns including agent orchestration, state management, and structured output using Spring AI and OpenAI.

## 🏗 Architecture

The project uses an **Orchestrator-Worker** pattern to manage sales conversations:

```
┌─────────────────────────────────────────────────────────────┐
│                      Orchestrator                           │
│  (Routes requests based on intent, stage, and interest)     │
└───────────────────────┬─────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┬───────────────┐
        ▼               ▼               ▼               ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│GeneralWorker│ │ PriceWorker │ │ObjectionWkr │ │ClosingWkr  │
│ (Discovery) │ │   (Budget)  │ │(Hesitation) │ │  (Close)   │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
```

### Components

- **Orchestrator**: Central router that analyzes user input, conversation history, and sentiment to select the appropriate worker.
- **Workers**: Specialized agents for different sales scenarios:
  - `GeneralWorker`: Handles discovery and general conversation
  - `PriceComparisonWorker`: Addresses budget concerns and value propositions
  - `ObjectionWorker`: Handles hesitation and reframes concerns
  - `ClosingWorker`: Finalizes sales when buying signals appear
- **ConversationState**: Persists session data including:
  - Conversation History
  - Customer Interest Score (0-10)
  - Current Sales Stage
  - Extracted Information (Slots)

## ✨ Key Features

- **Dynamic LLM Routing**: Uses OpenAI to intelligently route requests to specialized workers
- **Sentiment Tracking**: Real-time customer interest scoring to adjust sales strategy
- **Structured Output**: JSON schema responses for reliable decision-making
- **Tool Calling**: Product information retrieval via function tools
- **Rich Console UI**: Color-coded panels with interest bars and debug insights

## 🚀 Getting Started

### Prerequisites
- Java 21+
- OpenAI API Key

### Running the Interactive Console

```bash
export OPENAI_API_KEY=sk-your-key-here
./mvnw spring-boot:run -Dspring-boot.run.profiles=cli
```

The console displays:
- 🤖 Agent replies with worker attribution
- 📊 System status (worker, stage, interest level)
- 🎯 Extracted customer insights
- 📈 Interest trend tracking

Type `quit` or `exit` to end the session.

## 🧪 Running Tests

### All Tests
```bash
./mvnw test
```

### Individual Test Suites

| Test | Command | Description |
|------|---------|-------------|
| Basic | `./mvnw -Dtest=BasicTest test` | Worker and orchestrator unit tests |
| Structured Output | `./mvnw -Dtest=StructuredOutputTest test` | JSON schema output validation |
| Integration | `./mvnw -Dtest=MultiAgentIntegrationTest test` | Full sales journey scenarios |
| AI Evaluation | `./mvnw -Dtest=ConversationEvaluationTest test` | AI-powered quality scoring |

## 📊 Evaluation Framework

The project includes an AI-powered evaluation system that scores agent performance based on metrics:

| Metric | Weight | Description |
|--------|--------|-------------|
| Intent Recognition | 25% | Accuracy in identifying user intent |
| Business Outcome | 30% | Progress toward successful sale |
| Autonomy | 15% | Handling situations without human intervention |
| Hallucination Control | 20% | Avoiding false product information |
| Overall Quality | 10% | Natural conversation flow and professionalism |

Output includes weighted scores, letter grades (A+ to F), strengths, and improvement suggestions.

## 📂 Project Structure

```
src/
├── main/java/com/getenrola/aidemo/
│   ├── AiSalesAgentApplication.java    # Spring Boot entry point
│   ├── InteractiveConsoleRunner.java   # CLI interface (@Profile("cli"))
│   ├── agent/
│   │   ├── Orchestrator.java           # Main routing logic
│   │   ├── OpenAiClientWrapper.java    # Spring AI client wrapper
│   │   ├── ConversationState.java      # Session state model
│   │   ├── Worker.java                 # Worker interface
│   │   ├── worker/                     # Worker implementations
│   │   └── prompt/                     # Prompt templates
│   ├── config/                         # Spring configuration
│   └── model/                          # Data models
├── main/resources/
│   ├── application.properties          # App configuration
│   └── metric.csv                      # Evaluation metrics
└── test/java/com/getenrola/aidemo/agent/
    ├── BasicTest.java                  # Unit tests
    ├── StructuredOutputTest.java       # JSON output tests
    ├── MultiAgentIntegrationTest.java  # Integration tests
    └── ConversationEvaluationTest.java # AI evaluation tests
```

---
*Built with Spring Boot 3.5.7 and Spring AI 1.1.0*
