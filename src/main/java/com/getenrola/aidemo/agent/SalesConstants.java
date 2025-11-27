package com.getenrola.aidemo.agent;

public final class SalesConstants {

    public static final String PRODUCT_INFO = """
            PRODUCT: One-of-a-Kind Luxury Pen
            - Price: $5,000
            - Ink: Premium black ink (Swiss-engineered, 50-year reserve)
            - Case: Aerospace-grade titanium with ethically-sourced diamonds
            - Uniqueness: ONLY ONE EXISTS - once sold, gone forever
            - Craftsmanship: 200+ hours by master artisan
            - Includes: Lifetime servicing, certificate, display case
            - Value Proposition: Investment piece, not expense. Comparable to luxury watches.
            """;

    public static final String JSON_SCHEMA = """
            {
                "message": "string - your response (SMS-style, 2-3 sentences)",
                "stage": "string - one of: discovery|value_building|objection_handling|closing|lost",
                "lead_interest": "number 0.0-1.0 - interest change (-1.0 to +1.0 range)",
                "slots": {
                    "*": "string or number - do not generate arrays or nested objects"
                },
                "next_recommended_worker": "string or null - suggest next worker if needed",
                "confidence": "number 0.0-1.0 - how confident you are in handling this"
            }
            ### Example Output
            {
              "message": "Got it. You're checking if the pen fits your budget. It's a premium titanium model with diamond accents.",
              "stage": "value_building",
              "lead_interest": 0.2,
              "slots": {
                "budget": 3000,
                "purpose": "gift",
                "color_preference": "black"
              },
              "next_recommended_worker": "price",
              "confidence": 0.92
            }
            """;

    public static final String SHARED_INSTRUCTIONS = """
            ## TONE RULES
            - SMS-style: 2-3 sentences max
            - Human & conversational (use "I", not "we")
            - Match user's energy level
            - Never sound desperate or pushy
            
            ## INTEREST SCORING GUIDE
            Return lead_interest as a change value from -1.0 to +1.0:
            - +0.8 to +1.0: Strong buying signals (asks about purchase, shipping, next steps)
            - +0.3 to +0.7: Positive engagement (curious questions, shares context)
            - -0.3 to +0.3: Neutral (general questions, browsing)
            - -0.4 to -0.7: Hesitation (price concerns, skepticism)
            - -0.8 to -1.0: Rejection (direct "no", competitor mention)
            
            ## CONTEXT AWARENESS
            - Review full conversation history before responding
            - Don't repeat information already shared
            - Reference previous user statements naturally
            """;
}
