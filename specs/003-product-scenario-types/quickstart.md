# Quickstart: Product/Scenario/TransactionType Module

**Feature**: 003-product-scenario-types  
**Prerequisite**: Backend service running with PostgreSQL

## Quick Setup

```bash
# Ensure database is running
docker-compose up -d postgres

# Start backend (from backend directory)
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# API available at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui/index.html
```

## API Usage Examples

### 1. Create a Product

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "code": "CONSUMER_LOAN",
    "name": "消费贷款",
    "description": "面向个人消费者的贷款产品",
    "businessModel": "## 业务模式\n- 线上申请\n- 自动审批\n- 即时放款",
    "participants": "## 参与方\n- 借款人\n- 平台\n- 资金方",
    "fundFlow": "## 资金流向\n资金方 → 平台 → 借款人"
  }'
```

### 2. Create a Scenario under Product

```bash
# First get the product ID from step 1 response
curl -X POST http://localhost:8080/api/v1/scenarios \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "code": "DISBURSEMENT",
    "name": "放款",
    "description": "贷款发放场景",
    "triggerDescription": "## 触发条件\n- 审批通过\n- 签约完成",
    "fundFlowPath": "## 资金路径\n资金方账户 → 平台过渡户 → 借款人账户"
  }'
```

### 3. Create a Transaction Type under Scenario

```bash
curl -X POST http://localhost:8080/api/v1/transaction-types \
  -H "Content-Type: application/json" \
  -d '{
    "scenarioId": 1,
    "code": "NORMAL_DISBURSEMENT",
    "name": "正常放款",
    "description": "标准放款流程"
  }'
```

### 4. Associate a Rule with Transaction Type

```bash
# Assuming rule with ID 1 exists (from accounting-rules module)
curl -X POST http://localhost:8080/api/v1/transaction-types/1/rules \
  -H "Content-Type: application/json" \
  -d '{
    "ruleId": 1,
    "sequenceNumber": 1
  }'
```

### 5. View Product Hierarchy Tree

```bash
curl http://localhost:8080/api/v1/products/1/tree
```

Response:
```json
{
  "id": 1,
  "code": "CONSUMER_LOAN",
  "name": "消费贷款",
  "status": "DRAFT",
  "scenarios": [
    {
      "id": 1,
      "code": "DISBURSEMENT",
      "name": "放款",
      "status": "DRAFT",
      "transactionTypes": [
        {
          "id": 1,
          "code": "NORMAL_DISBURSEMENT",
          "name": "正常放款",
          "status": "DRAFT",
          "ruleCount": 1
        }
      ]
    }
  ]
}
```

### 6. Lifecycle Operations

```bash
# Activate product
curl -X POST http://localhost:8080/api/v1/products/1/activate

# Archive product
curl -X POST http://localhost:8080/api/v1/products/1/archive

# Restore archived product to draft
curl -X POST http://localhost:8080/api/v1/products/1/restore
```

### 7. Clone a Product

```bash
curl -X POST http://localhost:8080/api/v1/products/1/clone \
  -H "Content-Type: application/json" \
  -d '{
    "newCode": "CONSUMER_LOAN_V2",
    "newName": "消费贷款 V2"
  }'
```

### 8. Query Associated Rules and Accounts

```bash
# Get all rules used in a product
curl http://localhost:8080/api/v1/products/1/rules

# Get all accounts used in product's rules
curl http://localhost:8080/api/v1/products/1/accounts
```

## Common Workflows

### Workflow 1: Design New Product Line

1. Create Product with business descriptions
2. Create Scenarios for each business flow
3. Create Transaction Types for each scenario
4. Associate existing rules or create new rules
5. Review hierarchy tree
6. Activate when ready

### Workflow 2: Copy Existing Design

1. Find source product: `GET /api/v1/products?search=keyword`
2. Clone product: `POST /api/v1/products/{id}/clone`
3. Modify cloned entities as needed
4. Associate different rules if required

### Workflow 3: Impact Analysis

1. Query product's rules: `GET /api/v1/products/{id}/rules`
2. Query product's accounts: `GET /api/v1/products/{id}/accounts`
3. Review which accounts would be affected by rule changes

## Validation Rules

| Entity | Constraint | Error Code |
|--------|------------|------------|
| Product | code unique globally | 409 Conflict |
| Scenario | code unique within product | 409 Conflict |
| TransactionType | code unique within scenario | 409 Conflict |
| Delete Product | cannot have child scenarios | 409 Conflict |
| Delete Scenario | cannot have child types | 409 Conflict |
| Create child | parent cannot be ARCHIVED | 400 Bad Request |
| Associate rule | rule cannot be ARCHIVED | 400 Bad Request |

## Next Steps

- Use `/speckit.tasks` to generate implementation tasks
- Implement domain entities following `data-model.md`
- Implement REST controllers following `contracts/openapi.yaml`
