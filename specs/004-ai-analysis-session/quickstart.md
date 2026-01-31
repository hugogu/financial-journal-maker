# Quickstart: AI Analysis Session

**Feature**: 004-ai-analysis-session
**Date**: 2026-01-31

## Prerequisites

- Backend running on `http://localhost:8080`
- Frontend running on `http://localhost:3000`
- PostgreSQL database with migrations applied
- At least one AI provider configured

## Analyst Workflow

### 1. Create a New Session

```bash
curl -X POST http://localhost:8080/api/v1/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Consumer Loan Product Design"
  }'
```

Response:
```json
{
  "id": 1,
  "title": "Consumer Loan Product Design",
  "status": "ACTIVE",
  "currentPhase": "PRODUCT",
  "createdAt": "2026-01-31T10:00:00Z",
  "updatedAt": "2026-01-31T10:00:00Z"
}
```

### 2. Send Message to AI (Streaming)

```bash
curl -X POST http://localhost:8080/api/v1/sessions/1/messages/stream \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "content": "I want to design the accounting for a consumer loan disbursement process. The loan is funded from our treasury account and disbursed to the customer bank account."
  }'
```

SSE Response (streamed):
```
data: {"chunk": "Based on your description, I'll help you design the accounting structure for a consumer loan disbursement..."}

data: {"chunk": "\n\n## Suggested Product Structure\n\n**Product Name**: Consumer Loan\n**Code**: CONSUMER_LOAN\n..."}

data: {"done": true, "messageId": 2}
```

### 3. Confirm Design Decision

```bash
curl -X POST http://localhost:8080/api/v1/sessions/1/decisions \
  -H "Content-Type: application/json" \
  -d '{
    "decisionType": "PRODUCT",
    "entityType": "Product",
    "content": {
      "code": "CONSUMER_LOAN",
      "name": "Consumer Loan",
      "description": "Consumer loan disbursement and repayment"
    },
    "isConfirmed": true
  }'
```

### 4. Progress to Next Phase

After confirming Product, send another message to proceed to Scenario design:

```bash
curl -X POST http://localhost:8080/api/v1/sessions/1/messages/stream \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "content": "Now let us design the scenarios for this product. The main scenario is loan disbursement."
  }'
```

### 5. View Session Details

```bash
curl http://localhost:8080/api/v1/sessions/1
```

Response:
```json
{
  "id": 1,
  "title": "Consumer Loan Product Design",
  "status": "ACTIVE",
  "currentPhase": "SCENARIO",
  "messageCount": 4,
  "confirmedDecisions": [
    {
      "id": 1,
      "decisionType": "PRODUCT",
      "entityType": "Product",
      "content": {
        "code": "CONSUMER_LOAN",
        "name": "Consumer Loan"
      },
      "isConfirmed": true
    }
  ],
  "createdAt": "2026-01-31T10:00:00Z",
  "updatedAt": "2026-01-31T10:15:00Z"
}
```

### 6. Pause and Resume Session

```bash
# Pause
curl -X POST http://localhost:8080/api/v1/sessions/1/pause

# Resume later
curl -X POST http://localhost:8080/api/v1/sessions/1/resume
```

### 7. Complete and Export

```bash
# Complete session (after all phases done)
curl -X POST http://localhost:8080/api/v1/sessions/1/complete

# Export as COA entries
curl -X POST http://localhost:8080/api/v1/sessions/1/export/coa

# Export as accounting rules
curl -X POST http://localhost:8080/api/v1/sessions/1/export/rules

# Export as Numscript
curl -X POST http://localhost:8080/api/v1/sessions/1/export/numscript
```

Export Response (Numscript example):
```json
{
  "artifactType": "numscript",
  "content": "// Consumer Loan Disbursement\nvars {\n  account $treasury\n  account $customer\n  monetary $amount\n}\n\nsend $amount (\n  source = $treasury\n  destination = $customer\n)",
  "entityCount": 1,
  "warnings": []
}
```

### 8. Handle Export Conflicts

If export conflicts with existing data:
```bash
# Check conflicts first (returns 409 if conflicts exist)
curl -X POST http://localhost:8080/api/v1/sessions/1/export/rules

# Force overwrite
curl -X POST "http://localhost:8080/api/v1/sessions/1/export/rules?forceOverwrite=true"
```

## Admin Workflow

### 1. Configure AI Provider

```bash
curl -X POST http://localhost:8080/api/v1/ai-config \
  -H "Content-Type: application/json" \
  -d '{
    "providerName": "openai",
    "displayName": "OpenAI GPT-4",
    "endpoint": "https://api.openai.com/v1",
    "modelName": "gpt-4-turbo-preview",
    "apiKey": "sk-..."
  }'
```

### 2. Test Configuration

```bash
curl -X POST http://localhost:8080/api/v1/ai-config/test \
  -H "Content-Type: application/json" \
  -d '{
    "providerName": "openai",
    "endpoint": "https://api.openai.com/v1",
    "modelName": "gpt-4-turbo-preview",
    "apiKey": "sk-..."
  }'
```

Response:
```json
{
  "valid": true,
  "message": "Connection successful",
  "latencyMs": 450
}
```

### 3. Activate Provider

```bash
curl -X POST http://localhost:8080/api/v1/ai-config/1/activate
```

### 4. Manage Prompt Templates

```bash
# Create custom prompt for Product phase
curl -X POST http://localhost:8080/api/v1/prompts \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Product Analysis",
    "designPhase": "PRODUCT",
    "content": "You are an accounting design assistant. The user is designing a financial product.\n\nExisting products in the system:\n{{existingProducts}}\n\nUser message: {{userMessage}}\n\nProvide structured suggestions for the product design including code, name, and description."
  }'

# Update prompt (creates new version)
curl -X PUT http://localhost:8080/api/v1/prompts/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Product Analysis",
    "designPhase": "PRODUCT",
    "content": "..."
  }'

# View version history
curl http://localhost:8080/api/v1/prompts/1/versions

# Activate specific version
curl -X POST http://localhost:8080/api/v1/prompts/1/activate
```

## Frontend Pages

| Page | URL | Description |
|------|-----|-------------|
| Session List | `/analysis` | View and manage all sessions |
| Session View | `/analysis/:id` | Conversation interface with AI |
| AI Config | `/admin/ai-config` | Configure LLM providers |
| Prompts | `/admin/prompts` | Manage prompt templates |

## Design Phase Flow

```
1. PRODUCT Phase
   - Describe business product
   - AI suggests product structure
   - Confirm or modify product details
   
2. SCENARIO Phase
   - Describe scenarios within product
   - AI suggests scenario breakdown
   - Link to existing products or create new
   
3. TRANSACTION_TYPE Phase
   - Define transaction types per scenario
   - AI suggests transaction flows
   - Configure accounts involved
   
4. ACCOUNTING Phase
   - Design detailed accounting entries
   - AI validates consistency
   - Generate rules and Numscript
```

## Error Handling

| Status | Meaning |
|--------|---------|
| 400 | Invalid request or state transition |
| 404 | Session/resource not found |
| 409 | Conflict (max sessions, export conflicts) |
| 503 | AI service unavailable |

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `AI_ENCRYPTION_KEY` | Key for API key encryption | Required |
| `SESSION_AUTO_SAVE_INTERVAL` | Auto-save interval in seconds | 30 |
| `MAX_CONCURRENT_SESSIONS` | Max active sessions per analyst | 5 |
