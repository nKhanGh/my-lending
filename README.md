# Mini Lending — Credit Line Simulator

Spring Boot 3 / Java 21 backend, React / TypeScript / CSS Modules frontend, and PostgreSQL. This is a multi-user simulator. It does not make real credit decisions or move real money.

The original brief is preserved in [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md). The five later features are described in [EXTENSION_PLAN.md](EXTENSION_PLAN.md).

## Run locally

Requirements: Java 21, Maven 3.9+, Node.js 20+, Docker Desktop.

From the repository root, run:

    docker compose up -d postgres
    cd backend
    mvn spring-boot:run

In another terminal, run:

    cd frontend
    npm ci
    npm run dev

Open http://127.0.0.1:5173/. Swagger UI is at http://localhost:8080/swagger-ui.html. Vite proxies /api to Spring Boot. Compose publishes PostgreSQL on port 55432. Flyway applies V1–V6, including backfills for older repayment data.

Register in the UI or sign in locally as demo@local.test with password DemoPass123!. Change DEMO_PASSWORD before exposing the demo beyond localhost. JWT_SECRET accepts at least 32 bytes; when blank, the backend generates a fresh signing key at each start and existing sessions expire. Set ADMIN_EMAIL and ADMIN_PASSWORD (at least eight characters) to seed an admin user for simulator controls. Export overrides to the backend process; Compose's .env file alone does not configure Maven. See [.env.example](.env.example).

## Try the scenario

1. Submit income 3000, living expenses 1400, and existing debt payments 500. With no optional profile, DEMO_V1 grants SGD 1000. Enable simulated repayment history on the form to use DEMO_V2.
2. Draw SGD 800. Four monthly principal instalments are created; available limit is SGD 200.
3. Request full or partial payment for the earliest open instalment. The response is PENDING and leaves balances unchanged. The simulator settles after five seconds; its default deterministic failure rate is 10%. The UI polls for the result. A failed attempt allows a new retry.
4. A successful payment reduces the instalment remaining amount, drawdown principal, and used limit by the same amount. The daily UTC job marks past-due open instalments OVERDUE.

## API

All business endpoints require an Authorization: Bearer token. Register and login are public. Admin paths require the ADMIN role. Responses use the ApiResponse envelope with the payload in result.

| Method     | Path                                                      | Purpose                                                                   |
| ---------- | --------------------------------------------------------- | ------------------------------------------------------------------------- |
| POST       | /api/auth/register, /api/auth/login                       | Account creation and one-hour JWT                                         |
| GET        | /api/auth/me                                              | Current user                                                              |
| PUT / GET  | /api/credit-profile                                       | Optional simulated on-time/missed payment counts                          |
| POST / GET | /api/credit-applications, /api/credit-applications/latest | Decision and stored explanation                                           |
| GET        | /api/credit-account                                       | Own limit and used balance                                                |
| POST / GET | /api/credit-drawdowns, /api/credit-drawdowns/{id}         | Drawdown, list, schedule                                                  |
| POST       | /api/instalments/{id}/repay                               | Payment request; Idempotency-Key required; optional body {"amount":30.25} |
| GET        | /api/payments/{id}                                        | Poll PENDING, SUCCESS, or FAILED                                          |
| GET        | /api/credit-drawdowns/{id}/repayments                     | Own payment attempt history                                               |
| GET        | /api/audit-events                                         | Own audit history                                                         |
| POST       | /api/admin/payments/{id}/settle                           | Admin callback with outcome and unique providerEventId                    |
| GET        | /api/admin/reconciliation                                 | Compare account, drawdown, instalment, and ledger sums                    |
| GET        | /api/admin/users/{id}/audit-events                        | Admin audit inspection                                                    |

The simulator callback and admin callback share one transaction. A duplicate callback with the same event and outcome returns the terminal attempt. Contradictory or reused events return 409 without changing balances. A fresh idempotency key is needed after a confirmed failure. The frontend retains the key while the network result is unknown.

DEMO_V2 rejects two or more simulated missed payments or monthly debt payments greater than 40% of net income, then applies the original surplus tiers. Each application stores its profile snapshot, displayed ratio, reason, and policy version. These are invented simulator rules, not real underwriting criteria.

Each drawdown and successful repayment writes a signed principal movement in the same database transaction. Flyway backfills older data. Ledger and audit tables reject UPDATE and DELETE. Reconciliation reports matched=false when sums differ. There is no external event publisher, so the simulator does not need an outbox.

## Verify

    cd backend
    mvn clean test
    cd ../frontend
    npm run format:check
    npm run build

Backend tests use PostgreSQL Testcontainers for two-user isolation, concurrent drawdown locking, async success/failure and repeated callbacks, partial payments, fixed-clock overdue dates, policy snapshots, and ledger mismatches. A running backend JAR can lock Maven's package output on Windows; stop it before mvn clean verify.
