# 🖊️ AI Demo Assignment — “Sell a Pen”

## Overview

This project is a short technical assignment designed to test your ability to **design, build, and evaluate an AI agent** using the OpenAI API and Java (Spring Boot).  
The goal is to implement a simple **AI Sales Agent** that can hold an SMS-style conversation with a potential customer and “sell a pen.”

The project includes a minimal starter setup with a working agent and console interface.  
You are free to modify, extend, or refactor as you like.

---

## 🎯 Your Task

Build and improve an **AI Agent** that sells a pen.

### Minimum requirements
- **Engineer and implement a prompt** that follows the sales process described below.
- **Implement a basic test or evaluation** to measure how well your agent performs with different conversation inputs.

### Extra-credit ideas (optional)
If you’re feeling ambitious, consider adding or writing a plan for:
- ✅ **Structured output** — e.g. text output + lead interest classification + sales stage.
- ✅ **A lightweight eval framework** — test different prompts, models, or temperatures, or use AI to score outputs.
- ✅ **Tool/function use** — call a local function for product details.
- ✅ **Spring AI integration** — refactor to use [Spring AI](https://spring.io/projects/spring-ai).
- ✅ **Conversation Memory** — is there a different way to manage conversation memory? What are the advantages/disadvantages of the different approaches?
- ✅ **Few-Shot** — can few-shot prompting be implemented and how does it improve performance?

You don’t have to do any of these, but they’ll help us see how you think about applied AI.

---

## 🧠 The Scenario

Your AI agent is a **sales representative** for a company that sells pens.  
It’s chatting with a **lead** (the potential buyer) via SMS-style messages — so keep messages short and natural.

The goal is to follow a realistic **five-step sales process:**

1. **Discovery** – Ask questions to understand the lead’s motivations.
2. **Presentation** – Link product features to what matters to the lead.
3. **Temperature Check** – Gauge the lead’s interest; invite questions.
4. **Commitment** – Move toward a purchase decision.
5. **Action** – Close the sale with a clear next step.

### Example conversation

```
User: Hi
Agent: Hey there! I hear you're looking for a new pen. What do you usually use a pen for?
User: I want something nice for signing contracts.
Agent: Got it. You need a pen that feels great and looks impressive. Our Signature Black Gel Pen fits that perfectly — smooth ink, classy design. How does that sound?
User: Sounds good.
Agent: Excellent. It sounds like you are pretty keen on our pen. Is there anything else you'd like to know about it?
User: No.
Agent: Awesome. Would you like me to send you a link to grab one?
User: Sure.
Agent: Great! Here’s your link: https://bit.ly/fakepen. It’s valid for 4 hours. Let me know if you have any questions.
```

Objection handling is an important part of the sales process — it’s how the agent helps the lead move past uncertainty without being pushy.

Objects are best handled with the following process:

* Acknowledge the concern – show understanding (“Totally fair,” “I get that,” “That’s a good question”).

* Reframe or clarify – address the reason behind the objection (“The price is higher because it’s refillable and lasts years.”).

* Reconfirm value – link back to what the user said matters most to them (“You mentioned you want something that feels professional — this one’s designed for exactly that.”).

* Check readiness – lightly test if the objection is resolved (“Does that sound more reasonable now?”).

* Transition smoothly – move back to the sales flow or closing step (“If it feels like the right fit, I can send you the link.”).

---

## 💻 Tech Setup

This project is built with **Java + Spring Boot** and uses the [OpenAI Java SDK](https://github.com/openai/openai-java).

### Prerequisites
- A **GitHub account**
- **Java IDE** (we recommend IntelliJ IDEA)
- **OpenAI API key** (you’ll be given one, budgeted at ~$20 USD for this project)

---

## ▶️ How to Run

```bash
export OPENAI_API_KEY=<<YOUR_OPENAI_API_KEY>>
./mvnw spring-boot:run
```

This starts the console app. You can then chat directly with your AI agent.

To run the included test:

```bash
export OPENAI_API_KEY=<<YOUR_OPENAI_API_KEY>>
./mvnw -Dtest=PenSalesOpenAiAgentTest test
```

If you are running using IntelliJ (not the command line), then you'll want to add that OPENAI_API_KEY as an environment variable in your run configuration.

---

## 🧩 Project Structure

| File                           | Description                                                                                     |
|--------------------------------|-------------------------------------------------------------------------------------------------|
| `PenSalesOpenAiAgent.java`     | Core logic for your AI agent. This is where you’ll spend most of your time.                     |
| `PenSalesOpenAiAgentTest.java` | A simple “eval” test. Extend this to test multiple prompts, models, or outcomes.                |
| `ConsoleChat.java`             | Lets you interact with the agent from the command line. You can leave this as-is or improve it. |

---

## 🚀 What to Submit

1. Push your completed project to **your own public GitHub repo**.
2. Re-write this README to describe:
    - What you built
    - Why you made your design choices
    - How to run and test your agent
    - Anything else you think we should know
3. Email us with a link to your repo.

---

## 💡 Tips

- Be creative — the task is small, but we’re looking for **clear thinking and applied AI ability**, not fancy frameworks.
- Use AI tools to help you write your code if you want (we do that too!).
- If you add structured output, evals, or tool use — keep it simple and explain your reasoning clearly.
