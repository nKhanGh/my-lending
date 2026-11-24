# Mini Lending — Credit Line & Repayment Simulator

Full-stack proof of concept based on the user-provided `mini-lending-credit-line-30h.md` brief. One seeded demo user receives a rule-based credit decision, uses a simulated SGD credit line, sees four monthly instalments, and pays them in full one at a time. No real loan, payment, transfer, or credit decision is made.

The repository has a Spring Boot backend in `backend/`, a React and CSS Modules frontend in `frontend/`, and one PostgreSQL service in `docker-compose.yml`. The [implementation plan](IMPLEMENTATION_PLAN.md) records the exact MVP rules, schema, tests, and commit sequence.

## Requirements

- Java 21 and Maven 3.9+
- Node.js 20+ and npm
- Docker Desktop with a running Linux engine

## Start locally

From the repository root:

```powershell
docker compose up -d postgres
```

In a second terminal:

```powershell
cd backend
mvn spring-boot:run
```

In a third terminal:

```powershell
cd frontend
npm ci
npm run dev
```

Open `http://127.0.0.1:5173/` for the UI or `http://localhost:8080/swagger-ui.html` for the API documentation. Vite proxies `/api` to the backend. Defaults: PostgreSQL `localhost:55432`, API `localhost:8080`, UI `127.0.0.1:5173`. See `.env.example` and `frontend/.env.example` for overrides. Flyway creates the six tables and seeds one demo user on a fresh database.

The app uses one account for that demo user. To rerun the scenario from an empty database, stop the app and use `docker compose down -v`, then restart the stack. That command deletes all data in this repository's Compose volume.

## Three-minute demo

1. Submit monthly net income `3000`, living expenses `1400`, and existing debt payments `500`.
2. See `APPROVED`, reason `APPROVED_TIER_1000`, policy `DEMO_V1`, surplus SGD 1100 and limit SGD 1000.
3. Use SGD 800. Available limit becomes SGD 200; four SGD 200 instalments are created.
4. Pay the first instalment. Outstanding principal becomes SGD 600 and available limit becomes SGD 400.
5. Pay the remaining three. The drawdown becomes `PAID_OFF`, outstanding becomes zero and available returns to SGD 1000.

The system rejects a drawdown above the available limit. A concurrent limit test and idempotency tests prove that duplicate requests do not increase debt or restore the limit twice.

## Backend API

Success and error bodies use the `ApiResponse<T>` envelope, with successful payloads in `result`.

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/credit-applications` | Assess three financial inputs and create an account on approval |
| GET | `/api/credit-applications/latest` | Latest application decision |
| GET | `/api/credit-account` | Total, used and available limit |
| POST | `/api/credit-drawdowns` | Use credit; requires `Idempotency-Key` |
| GET | `/api/credit-drawdowns` | List drawdowns |
| GET | `/api/credit-drawdowns/{id}` | Detail and four instalments |
| POST | `/api/instalments/{id}/repay` | Pay the next unpaid instalment in full; requires `Idempotency-Key` |
| GET | `/api/credit-drawdowns/{id}/repayments` | Simulated payment history |

Example assessment body:

```json
{"monthlyNetIncome":3000.00,"monthlyLivingExpenses":1400.00,"monthlyDebtPayments":500.00,"currency":"SGD"}
```

Example utilization body and header:

```http
POST /api/credit-drawdowns
Idempotency-Key: draw-001
Content-Type: application/json

{"amount":800.00}
```

Use the returned drawdown ID to read its instalments. Pass the first instalment UUID to `POST /api/instalments/{id}/repay` with a new idempotency key such as `repay-001`.

Validation failures return `400`, missing resources return `404`, and insufficient limit, wrong payment order, duplicate payment with another key, or key reuse with different input return `409`. A retry of the same operation with the same key does not change balances again. The React client keeps a key for an uncertain network result and clears it after confirmed success or a definite client error.

## Rules and limits

- Surplus below SGD 400 is rejected; surplus from 400, 800, and 1500 grants limits of SGD 500, 1000, and 2000 respectively.
- Money is stored as `NUMERIC(19,2)` and calculated using `BigDecimal`. The first three instalments are rounded down to cents; the fourth receives the remainder.
- Drawdown must be at least SGD 0.04 so four positive instalments can be created.
- Account balance changes are serialized by a PostgreSQL row lock and committed with drawdown or repayment records in one transaction.
- Only the earliest unpaid instalment can be paid, although early full payment is allowed.
- Authentication, real payment processing, partial payments, overdue processing, interest, ledger, and multiple users are outside this MVP.

## Verify

```powershell
cd backend
mvn clean verify

cd ../frontend
npm ci
npm run build
```

Backend tests use PostgreSQL Testcontainers and cover policy thresholds, money rounding, concurrent drawdowns, retries, payment order, closure, and rollback. The UI was checked against the running API with the `3000 / 1400 / 500 → 800 → first repayment` flow.
