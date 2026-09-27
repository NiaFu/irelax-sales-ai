# iRelax Sales AI

A mobile-first AI sales assistant for iRelax massage-chair sales.

## V1 Goal

The first version focuses on the sales workflow around customer SMS conversations:

```text
Customer sends SMS
→ system identifies or creates the customer
→ conversation is saved
→ AI loads customer history + product knowledge + sales style
→ AI generates a suggested reply
→ salesperson reviews/edits
→ reply is sent
→ customer state is updated
→ follow-up task is created when needed
```

## Core Features

### 1. Messaging
- Receive inbound customer SMS
- Match phone numbers to customer profiles
- Store complete conversation history
- Draft AI replies
- Review/edit before sending
- Send outbound SMS
- Track message delivery status

### 2. Customer CRM
Each customer can store:
- First name / last name
- Phone number
- Preferred contact channel
- First visit date and location
- Visit feedback
- Budget
- Interested products
- AI-predicted alternative products
- Sales stage
- Notes
- Last contact
- Next follow-up

### 3. AI Sales Assistant
AI context can include:
- Customer profile
- Recent conversation history
- Visit notes
- Interested products
- Approved iRelax product data
- Sales scripts and communication style

V1 uses **AI drafts only**. Messages are not automatically sent without approval.

### 4. Follow-up Automation
The system can create tasks such as:
- General follow-up
- Quote follow-up
- Visit follow-up
- No-response follow-up
- Delivery check
- 7-day post-sale check
- 30-day post-sale check

### 5. Product Knowledge
Product information will be stored in a controlled database and later supplemented by:
- iRelax website information
- Product manuals
- Sales notes
- Product comparisons

Structured data is preferred for important facts such as price, warranty, dimensions and features.

## Mobile App

Planned main tabs:

### Today
- Follow-ups due today
- Overdue follow-ups
- New leads
- Hot customers
- Recent incoming messages
- Post-sale tasks

### Messages
- Customer conversation list
- Full SMS history
- AI reply suggestion
- Edit / regenerate / send

### Customers
- Search and filter customers
- Customer profile
- Product interests
- Sales stage
- Timeline
- Notes and follow-ups

### AI Assistant
Examples:
- “Who should I follow up today?”
- “Show customers interested in ROBO.”
- “Write a softer follow-up for Peter.”
- “Which customers have not replied for 7 days?”

## Planned Tech Stack

### Mobile
- React Native
- TypeScript

### Backend
- Java 21
- Spring Boot
- REST API

### Database
- PostgreSQL
- Supabase

### Messaging
- Twilio SMS

### AI
- OpenAI API

## High-Level Architecture

```text
React Native Mobile App
        |
        | REST API
        v
Spring Boot Backend
        |
        +-- Customer Service
        +-- Messaging Service
        +-- AI Service
        +-- Follow-up Service
        +-- Product Service
        +-- Knowledge Service
        |
        v
Supabase PostgreSQL

External Services:
Twilio SMS
OpenAI API
```

## Initial Data Model

Main entities:

- Customer
- Conversation
- Message
- CustomerProductInterest
- FollowUpTask
- Product
- CustomerNote
- AISuggestion
- SalesAssistantProfile

### Sales Stages

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

Customer-stated product interest and AI predictions must be stored separately.

## Messaging Flow

### Incoming SMS

```text
Twilio webhook
→ normalize phone number
→ find customer
→ create new lead if unknown
→ save message
→ analyse message
→ generate AI draft
→ notify mobile app
```

### Outgoing SMS

```text
User approves draft
→ backend sends via Twilio
→ save external message ID
→ update status
→ delivery webhook updates SENT / DELIVERED / FAILED
```

## AI Safety / Reliability Rules

The AI must not invent:
- Prices
- Discounts
- Warranty terms
- Stock availability
- Delivery promises
- Product specifications

These values should come from approved structured data or trusted knowledge sources.

## Planned Repository Structure

```text
irelax-sales-ai/
├── mobile/
│   └── src/
│       ├── screens/
│       ├── components/
│       ├── services/
│       ├── hooks/
│       ├── navigation/
│       ├── store/
│       └── types/
├── backend/
│   └── src/main/
│       ├── java/
│       │   ├── customer/
│       │   ├── messaging/
│       │   ├── ai/
│       │   ├── followup/
│       │   ├── product/
│       │   ├── knowledge/
│       │   ├── auth/
│       │   └── common/
│       └── resources/
├── docs/
├── .env.example
└── README.md
```

## Implementation Order

1. Project foundation
2. Customer CRM
3. SMS messaging
4. AI reply drafts
5. Customer/message analysis
6. Follow-up automation
7. Product knowledge
8. Post-sale automation
9. Authentication and security
10. Testing and mobile polish

## V1 Definition of Done

V1 is successful when this workflow works reliably from the mobile app:

```text
Customer sends SMS
→ salesperson receives it in the app
→ opens the customer profile
→ sees previous conversation and visit context
→ gets an AI reply suggestion
→ edits or approves it
→ sends the SMS
→ conversation is saved
→ system reminds salesperson when to follow up
```

## Not Included in Initial V1

- Email integration
- WhatsApp integration
- WeChat integration
- Fully automatic customer messaging
- Voice calls
- Complex machine-learning recommendation models
- Multi-company support
- Advanced analytics

## Secrets and Configuration

Credentials must never be committed to GitHub.

Expected environment variables will include:

```text
OPENAI_API_KEY
TWILIO_ACCOUNT_SID
TWILIO_AUTH_TOKEN
TWILIO_PHONE_NUMBER
SUPABASE_URL
SUPABASE_DB_URL
SUPABASE_KEY
```

A safe `.env.example` will be added during implementation.
