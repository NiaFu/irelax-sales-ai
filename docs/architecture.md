# Architecture

## Runtime flow

```text
Twilio SMS
   |
   | POST /api/webhooks/twilio/sms
   v
Spring Boot API
   |-- Customer matching by normalized phone number
   |-- Conversation/message persistence
   |-- asynchronous AI processing
   |-- follow-up automation
   v
PostgreSQL / Supabase

Mobile app <---- REST ----> Spring Boot API
                      |
                      +---- Twilio Messages API
                      +---- OpenAI Responses API
```

## Design rules

1. **Messages are the source of truth for conversation history.** Incoming and outgoing messages are persisted before or immediately after external actions.
2. **Customer facts and AI predictions are separate.** Customer-stated interests use `InterestSource.CUSTOMER`; inferred product interests use `InterestSource.AI` with confidence/reason metadata.
3. **AI never sends directly.** OpenAI produces a draft suggestion. The salesperson explicitly edits/approves and presses Send.
4. **Critical commercial facts are structured.** Price, warranty and core product facts come from the product table/knowledge entries rather than model memory.
5. **Inbound webhooks are fast.** Twilio SMS is persisted synchronously and AI work runs asynchronously.
6. **External integrations are replaceable.** Twilio and OpenAI are isolated behind service boundaries. Twilio also has a safe mock mode for local development.

## Authentication

The backend supports two modes:

- `AUTH_ENABLED=false`: local single-user development. Data is scoped to `DEV_OWNER_ID`.
- `AUTH_ENABLED=true`: Spring Security validates JWTs using `SUPABASE_ISSUER_URI`, and the JWT subject becomes the owner ID.

Twilio webhooks are public endpoints but are protected using `X-Twilio-Signature`. A configured Twilio number maps to `TWILIO_OWNER_ID` in V1.

## AI processing

The OpenAI integration uses the Responses API with Structured Outputs. The request contains:

- salesperson style/profile
- customer CRM context
- recent messages
- approved active product data
- active knowledge entries
- the newest inbound message

The structured response contains a draft, intent, sentiment, detected products, follow-up suggestion, stage suggestion and summary. If no OpenAI key is configured, the app falls back to a conservative local rules engine so development can continue without external API usage.

## Future extensions

The conversation/channel model already supports additional channels such as WhatsApp, email and manually imported messages. Those are intentionally outside V1.
