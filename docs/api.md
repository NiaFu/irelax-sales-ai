# API summary

## Customer CRM

- `GET /api/customers`
- `GET /api/customers/{id}`
- `POST /api/customers`
- `PUT /api/customers/{id}`
- `DELETE /api/customers/{id}`
- `POST /api/customers/{id}/interests`
- `POST /api/customers/{id}/notes`

## Messaging

- `GET /api/conversations`
- `GET /api/conversations/{id}`
- `POST /api/conversations/{id}/send`
- `POST /api/messages/{messageId}/ai-suggestion`

## Twilio webhooks

- `POST /api/webhooks/twilio/sms`
- `POST /api/webhooks/twilio/status`

Both endpoints validate `X-Twilio-Signature` when real Twilio mode is enabled.

## Follow-ups

- `GET /api/follow-ups`
- `POST /api/follow-ups`
- `POST /api/follow-ups/{id}/complete`

## Dashboard

- `GET /api/dashboard/today`

## Product knowledge

- `GET /api/products`
- `POST /api/products`
- `PUT /api/products/{id}`
- `GET /api/knowledge`
- `POST /api/knowledge`

## Sales profile

- `GET /api/profile`
- `PUT /api/profile`

## Structured assistant

- `POST /api/assistant/query`

V1 assistant queries are deterministic CRM queries. Rich AI drafting happens per inbound customer message.
