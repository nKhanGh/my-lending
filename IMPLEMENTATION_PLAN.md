# Mini Lending Credit Line — Implementation Plan

## 1. Goal and boundaries

Build the 30-hour proof of concept described in `mini-lending-credit-line-30h.md`: one seeded demo user applies for simulated SGD credit, receives a `DEMO_V1` decision, uses an approved limit, sees four monthly principal instalments, and repays each instalment in full. All money movement is simulated. This repository contains a Spring Boot backend and a React frontend.

This plan implements F1–F8 and the three UI screens in that brief. It excludes authentication and multiple users, real payment processing, interest, fees, partial repayment, overdue jobs, ledger, policy V2, shopping/checkout, and external events. Later portfolio extensions require a separate request and separate commits.

## 2. Reference conventions and technology

Follow the established style in the `develop` plan of `lending-backend` and the e-learning/BKJob projects: controllers delegate to services, request/response DTOs do not expose JPA entities, constructor injection with Lombok, `ApiResponse<T>` envelope, `AppException` and `ErrorCode`, Bean Validation at HTTP boundaries, transactions in services, and feature-oriented packages.

| Area | Choice | Purpose |
|---|---|---|
| Java | 21, Spring Boot 3.5 | Existing workspace convention |
| Storage | PostgreSQL 17, JPA, Flyway | One database and reviewable migrations |
| Money | `BigDecimal`, `NUMERIC(19,2)` | Exact cents, no floating-point balances |
| IDs | UUID | Stable public identifiers |
| Tests | JUnit 5, Spring Boot Test, Testcontainers PostgreSQL | Real transaction and row-lock behavior |
| Local database | Docker Compose | Reproducible demo setup |
| UI | React, TypeScript, Vite, CSS Modules | Three-screen demo; user preference takes precedence over the brief's Tailwind suggestion |

## 3. Repository layout

```text
backend/
  pom.xml
  src/main/java/com/nkhang/lending/
    application/       demo user, application, DTO, controller, service
    assessment/        DEMO_V1 policy
    creditaccount/     account balance and locking
    drawdown/          drawdown and four-instalment schedule
    repayment/         simulated full instalment payment
    common/            API envelope, exceptions, idempotency key validation
    config/            clock and OpenAPI
  src/main/resources/db/migration/
  src/test/
frontend/
  src/lib/             typed API client and formatters
  src/components/      application, account, drawdown detail
  src/*.module.css
docker-compose.yml
README.md
IMPLEMENTATION_PLAN.md
```

## 4. Schema and state

Flyway V1 creates exactly six core tables:

| Table | Required fields and constraints |
|---|---|
| `users` | UUID primary key, display name; seed one demo user |
| `credit_applications` | user, income, living expenses, debt payments, surplus, APPROVED/REJECTED, reason, `DEMO_V1`, approved limit, assessed time |
| `credit_accounts` | unique user, total and used limit, SGD, ACTIVE, version; `0 <= used <= total` |
| `credit_drawdowns` | account, original and outstanding principal, ACTIVE/PAID_OFF, created time, unique `(account_id,idempotency_key)` |
| `instalments` | drawdown, unique sequence 1–4, positive amount, due date, UNPAID/PAID, paid time |
| `repayment_transactions` | unique instalment, positive amount, SUCCESS, paid time, unique idempotency key |

Use foreign keys, `NUMERIC(19,2)`, `CHECK` constraints, and indexes on account/drawdown foreign keys. A new database can be reset to rerun the one-user demo.

Allowed transitions: application `SUBMITTED -> APPROVED|REJECTED` (decision happens in the submit transaction); drawdown `ACTIVE -> PAID_OFF`; instalment `UNPAID -> PAID`; repayment is an immutable `SUCCESS` record. No drawdown exists before approval. No second account is created for the demo user.

## 5. Business rules and transaction boundaries

### Credit assessment

Validate nonnegative income, expenses, debt payments, at most two decimal places, and SGD. Compute `surplus = income - livingExpenses - debtPayments`.

| Surplus | Decision | Limit | Reason |
|---|---|---:|---|
| `< 400` | REJECTED | 0 | `INSUFFICIENT_SURPLUS` |
| `400..799.99` | APPROVED | 500 | `APPROVED_TIER_500` |
| `800..1499.99` | APPROVED | 1000 | `APPROVED_TIER_1000` |
| `>= 1500` | APPROVED | 2000 | `APPROVED_TIER_2000` |

Persist input snapshot, surplus, decision, reason, policy version, and time. Serialize submission on the demo-user row. If an account already exists, reject further applications. A rejection may be followed by another application.

### Limit and drawdown

Maintain `available = total - used` and `used = sum(outstandingPrincipal)` for committed drawdowns. Lock the credit-account row with `PESSIMISTIC_WRITE` before changing balances. An `ACTIVE` account can use `0 < amount <= available`; because four positive instalments are required, the smallest supported amount is SGD 0.04.

In one transaction, check the idempotency key, increase used limit, create one drawdown and four instalments. A retry with the same key and amount returns the original drawdown ID without changing balances. Reusing a key with another amount returns `409`. Two concurrent requests cannot exceed the limit.

### Schedule and repayment

Generate due dates at drawdown date plus 1, 2, 3, and 4 months. Divide principal by four; round the first three instalments down to two decimals and put the remainder in the fourth. For 100.01, generate 25.00, 25.00, 25.00, 25.01.

Repay only the earliest `UNPAID` instalment of its drawdown; early full payment is allowed. In one transaction, lock the account, verify order and key, write one `SUCCESS` repayment, mark the instalment `PAID`, reduce drawdown outstanding, restore used limit, and mark the drawdown `PAID_OFF` at zero. Same-key retry returns the existing repayment without a second balance change; a key used for a different instalment returns `409`.

The committed-state invariants are `0 <= used <= total`, `available = total - used`, drawdown outstanding sum equals account used, instalment amounts sum to original drawdown, and successful repayments plus outstanding equal original drawdown.

## 6. REST contracts

| Method | Path | Result |
|---|---|---|
| POST | `/api/credit-applications` | Decision, reason, policy version, surplus, approved limit |
| GET | `/api/credit-applications/latest` | Latest application or 404 |
| GET | `/api/credit-account` | Total, used, available or 404 |
| POST | `/api/credit-drawdowns` | Created drawdown; `Idempotency-Key` required |
| GET | `/api/credit-drawdowns` | Current user's demo drawdowns |
| GET | `/api/credit-drawdowns/{id}` | Drawdown and four instalments |
| POST | `/api/instalments/{id}/repay` | Full payment; `Idempotency-Key` required |
| GET | `/api/credit-drawdowns/{id}/repayments` | Simulated history |

Return `400` for malformed input, `404` for missing resources, and `409` for conflicting state, insufficient limit, or key reuse with different input. Include human-readable messages. The frontend calls these endpoints through one typed client and handles retries with a stable key for the same pending action.

## 7. UI scope

1. Credit Application: three financial fields, submit, show approved limit or rejection reason.
2. My Credit Line: total/used/available values, utilization form, drawdown list and insufficient-limit feedback.
3. Drawdown Detail: four instalments, enable Pay only for the next eligible one, and show repayment history.

Refresh account, drawdown, and repayment data after each successful action. Keep pending idempotency keys when a network result is uncertain; clear only after confirmed success or a definite client-side validation failure. Use CSS Modules for layout and visual treatment.

## 8. Verification

Unit tests: all four assessment boundaries; 100.01 schedule and date math. PostgreSQL integration tests: approved/rejected persistence, one-account rule, 800 drawdown and 1200 rejection on a 1000 limit, simultaneous 700 drawdowns, same-key drawdown retry, first repayment, same-key repayment retry, wrong-order rejection, four-payment closure, and rollback on a failed transaction. HTTP tests verify validation and response shape. Frontend `npm run build` must pass, and the three-screen flow should be checked against the running API.

## 9. Commit sequence

Commit after each reviewable feature, in this order:

1. `docs: plan credit line MVP` — this file only.
2. `feat: bootstrap credit line workspace` — Spring/Vite setup, Compose, shared API/error infrastructure.
3. `feat: model credit line schema` — Flyway six tables, seed, entities, repositories.
4. `feat: assess credit applications` — policy, application API, account creation and boundary tests.
5. `feat: manage credit limit and drawdowns` — account API, row lock, idempotent utilization, schedule and tests.
6. `feat: repay instalments` — payment state, history, balance restoration and tests.
7. `feat: build credit line UI` — three screens and typed API calls with CSS Modules.
8. `docs: verify end-to-end demo` — README, test results, demo instructions, final fixes.

No commit should contain partial code for an unrelated later feature. Push the `develop` branch after verification of each completed feature when the new GitHub repository is connected.

## 10. Definition of done

The demo `3000 / 1400 / 500` approves SGD 1000, an SGD 800 drawdown leaves SGD 200 available and creates four SGD 200 instalments, paying the first restores available to SGD 400, and paying all four returns available to SGD 1000 with `PAID_OFF`. Concurrent drawdowns and retries cannot produce duplicated or excessive debt. Backend tests, frontend build, Docker setup, and README instructions all work.
