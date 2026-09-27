# iRelax Sales AI

Mobile-first AI sales assistant for iRelax massage-chair sales.

The V1 implementation is centered on SMS conversations: incoming customer messages become CRM activity, AI drafts a context-aware reply, the salesperson reviews it, and the system keeps the customer record and follow-up queue updated.

## Implemented V1 flow

```text
Customer sends SMS
        ↓
Twilio webhook
        ↓
Normalize phone + match/create customer
        ↓
Persist conversation and inbound message
        ↓
Asynchronous AI analysis
        ↓
Customer history + product data + sales knowledge + recent messages
        ↓
Draft reply + intent + product detection + follow-up suggestion
        ↓
Mobile app review/edit
        ↓
Salesperson presses Send
        ↓
Twilio sends SMS + delivery status is tracked
        ↓
CRM timeline and follow-up tasks stay updated
```

## What is included

### Mobile app

Expo / React Native application with four main areas:

- **Today** — due/overdue follow-ups, hot customers, new leads, recent conversations
- **Messages** — SMS conversation list, customer thread, AI draft, edit and send
- **Customers** — CRM profile, sales stage, visit feedback, product interests and notes
- **AI Assistant** — structured CRM queries such as follow-ups, HOT customers and stale leads

### Spring Boot backend

- Customer CRM CRUD
- Customer notes
- Customer-stated product interests
- AI-predicted product interests stored separately
- SMS conversation/message persistence
- Twilio inbound webhook
- Twilio outbound SMS
- Twilio delivery status callback
- Twilio signature validation
- OpenAI Responses API integration using Structured Outputs
- Conservative local AI fallback when no OpenAI key is configured
- Follow-up task engine
- Automatic post-sale tasks when a customer becomes `SOLD`
- Product knowledge CRUD
- Sales knowledge entries
- Configurable salesperson tone/rules
- Optional Supabase-compatible JWT resource-server authentication
- Flyway PostgreSQL schema

## Important AI rules

The system deliberately separates customer facts from AI inference.

```text
CUSTOMER source
"I like ROBO"

AI source
"Meister may also suit this customer"
```

AI-generated product interests are stored as `InterestSource.AI` and include confidence/reason metadata. They do not overwrite customer-stated facts.

The AI also does **not** autonomously send customer messages. V1 always requires the salesperson to review/edit and press **Send**.

Critical commercial facts should come from structured product/knowledge data. The AI is instructed not to invent:

- prices or discounts
- warranty terms
- stock availability
- delivery promises
- refunds
- medical claims

## Tech stack

### Mobile

- Expo SDK 57
- React Native 0.86
- React 19.2
- TypeScript

### Backend

- Java 21
- Spring Boot 4.1.1
- Spring MVC / RestClient
- Spring Data JPA
- Spring Security resource server
- Flyway
- PostgreSQL / Supabase PostgreSQL

### Integrations

- Twilio Programmable Messaging
- OpenAI Responses API

## Repository structure

```text
irelax-sales-ai/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/irelax/salesai/
│       │   ├── ai/
│       │   ├── api/
│       │   ├── auth/
│       │   ├── common/
│       │   ├── config/
│       │   ├── domain/
│       │   ├── integration/
│       │   ├── repository/
│       │   └── service/
│       └── main/resources/
│           └── db/migration/
├── mobile/
│   ├── App.tsx
│   └── src/
│       ├── screens/
│       ├── api.ts
│       ├── components.tsx
│       ├── theme.ts
│       └── types.ts
├── docs/
│   ├── api.md
│   ├── architecture.md
│   └── setup.md
├── docker-compose.yml
├── .env.example
└── README.md
```

## Customer data

The current CRM supports:

- first name / last name
- phone
- email
- preferred channel
- sales stage
- first visit date/location
- first meeting feedback
- budget range
- notes
- last contact
- next follow-up
- customer product interests
- AI product predictions
- customer notes
- full SMS conversation history

### Sales stages

```text
NEW
CONTACTED
VISITED
INTERESTED
HOT
SOLD
LOST
AFTER_SALES
```

## Follow-up automation

Supported task types include:

```text
GENERAL_FOLLOW_UP
QUOTE_FOLLOW_UP
VISIT_FOLLOW_UP
NO_RESPONSE
DELIVERY_CHECK
POST_SALE_7_DAY
POST_SALE_30_DAY
CUSTOM
```

Changing a customer to `SOLD` creates delivery, 7-day and 30-day care tasks if they do not already exist.

## Local development

### 1. Start PostgreSQL

```bash
docker compose up -d postgres
```

### 2. Configure environment

```bash
cp .env.example .env
```

For a no-cost local workflow, keep:

```text
AUTH_ENABLED=false
TWILIO_MOCK_ENABLED=true
OPENAI_API_KEY=
```

In that mode:

- outbound SMS is mocked rather than sent
- OpenAI is replaced with a conservative local rules engine
- the app still exercises the CRM/message/follow-up workflow

### 3. Run backend

```bash
cd backend
mvn spring-boot:run
```

Health:

```text
GET http://localhost:8080/actuator/health
```

### 4. Run mobile app

```bash
cd mobile
npm install
npm run start
```

For a physical phone, set `EXPO_PUBLIC_API_URL` to a backend address reachable from that device rather than `localhost`.

See [docs/setup.md](docs/setup.md) for full setup instructions.

## Twilio configuration

For real SMS:

```text
TWILIO_MOCK_ENABLED=false
TWILIO_ACCOUNT_SID=...
TWILIO_AUTH_TOKEN=...
TWILIO_PHONE_NUMBER=...
TWILIO_WEBHOOK_BASE_URL=https://your-api.example
TWILIO_STATUS_CALLBACK_URL=https://your-api.example/api/webhooks/twilio/status
```

Configure the Twilio incoming-message webhook as:

```text
POST https://your-api.example/api/webhooks/twilio/sms
```

Inbound webhooks are signature validated. AI processing is asynchronous so the Twilio webhook can return immediately.

## OpenAI configuration

```text
OPENAI_API_KEY=...
OPENAI_MODEL=gpt-6-luna
```

`gpt-6-luna` is the default for frequent sales drafting. The model is configurable without code changes.

The AI context can include:

- customer profile
- visit feedback and notes
- recent conversation history
- active product data
- sales knowledge entries
- salesperson tone/rules
- newest inbound message

## Authentication

Development defaults to single-user mode:

```text
AUTH_ENABLED=false
DEV_OWNER_ID=nia
```

The backend can validate Supabase JWTs when enabled:

```text
AUTH_ENABLED=true
SUPABASE_ISSUER_URI=https://YOUR_PROJECT.supabase.co/auth/v1
```

The authenticated JWT subject becomes the data owner ID.

The current mobile client can send a development bearer token through `EXPO_PUBLIC_API_BEARER_TOKEN`. A production Supabase sign-in UI is intentionally left until the production Supabase project/authentication method is selected.

## Tests

Backend tests cover the application context, Australian phone normalization, fallback AI intent/product detection and Twilio request-signature logic.

```bash
cd backend
mvn test
```

Mobile static check:

```bash
cd mobile
npm run typecheck
```

## API

See [docs/api.md](docs/api.md).

Primary endpoints:

```text
GET    /api/dashboard/today
GET    /api/customers
POST   /api/customers
GET    /api/conversations
GET    /api/conversations/{id}
POST   /api/conversations/{id}/send
POST   /api/messages/{messageId}/ai-suggestion
GET    /api/follow-ups
POST   /api/follow-ups/{id}/complete
GET    /api/products
POST   /api/products
GET    /api/knowledge
POST   /api/knowledge
POST   /api/assistant/query
```

## Not included yet

The following were intentionally kept out of V1:

- email integration
- WhatsApp integration
- WeChat integration
- direct reading of iPhone Messages history
- automatic customer message sending without approval
- voice calls
- full live website scraping
- complex ML recommendation models
- multi-company administration
- advanced analytics
- production Supabase sign-in UI

The data model is designed so additional conversation channels can be added later without replacing the CRM/message core.
