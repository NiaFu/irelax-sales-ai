# Local setup

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 22+
- Docker (recommended for local PostgreSQL)
- Expo tooling (`npx expo` is sufficient)

## 1. Database

From the repository root:

```bash
docker compose up -d postgres
```

Flyway runs the initial migration automatically when the backend starts.

## 2. Environment

Copy the example file and fill values as needed:

```bash
cp .env.example .env
```

For local development you can keep:

```text
AUTH_ENABLED=false
TWILIO_MOCK_ENABLED=true
OPENAI_API_KEY=
```

That configuration runs the CRM and message UI without sending real SMS and uses the local conservative AI fallback.

## 3. Backend

Export the environment variables (or configure them in your IDE), then:

```bash
cd backend
mvn spring-boot:run
```

Health endpoint:

```text
GET http://localhost:8080/actuator/health
```

## 4. Mobile app

```bash
cd mobile
npm install
npm run start
```

Set `EXPO_PUBLIC_API_URL` to an address reachable by the phone/simulator. `localhost` only works when the app and backend share the same network namespace. For a physical phone, use the computer's LAN IP or a secure tunnel.

## 5. Twilio production setup

Set:

```text
TWILIO_MOCK_ENABLED=false
TWILIO_ACCOUNT_SID=...
TWILIO_AUTH_TOKEN=...
TWILIO_PHONE_NUMBER=...
TWILIO_WEBHOOK_BASE_URL=https://your-public-backend.example
TWILIO_STATUS_CALLBACK_URL=https://your-public-backend.example/api/webhooks/twilio/status
```

Configure the Twilio number's incoming-message webhook to:

```text
POST https://your-public-backend.example/api/webhooks/twilio/sms
```

`TWILIO_WEBHOOK_BASE_URL` matters when the backend is behind a reverse proxy because Twilio signatures include the exact public request URL.

## 6. OpenAI

Set:

```text
OPENAI_API_KEY=...
OPENAI_MODEL=gpt-6-luna
```

The default is a cost-sensitive model suitable for frequent sales drafting. The model can be changed through environment configuration without code changes.

## 7. Supabase JWT protection

When ready to protect the API:

```text
AUTH_ENABLED=true
SUPABASE_ISSUER_URI=https://YOUR_PROJECT.supabase.co/auth/v1
```

The mobile client currently accepts a development bearer token through `EXPO_PUBLIC_API_BEARER_TOKEN`. A dedicated Supabase sign-in screen can be added once the production Supabase project and authentication flow are selected.
