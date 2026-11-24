# Portfolio extension plan

This plan extends the MVP in `IMPLEMENTATION_PLAN.md` after the 30-hour brief. The rules below are simulator assumptions, not lending or payment advice.

## 1. Accounts and authorization

- Add email, BCrypt password hash, and `CUSTOMER` / `ADMIN` role to users through Flyway V2. Keep the seeded demo user and its data.
- Add register, login, and `/me` endpoints. Issue short-lived signed access tokens. Protect business endpoints; resolve the user ID from the verified token rather than a fixed demo ID.
- Scope applications, accounts, drawdowns, instalments, idempotency keys, and repayment history by authenticated user. Admin endpoints cover simulator operations and reconciliation only.
- Test two accounts: a customer cannot read or mutate another customer's records. Test 401 and 403 separately.

## 2. Asynchronous simulated payments

- A payment request creates a `PENDING` attempt with an idempotency key and does not change the debt or limit.
- A simulator callback transitions exactly once to `SUCCESS` or `FAILED`. Only SUCCESS writes a repayment and changes balances; FAILED leaves them unchanged. Repeat and late contradictory callbacks are recorded or ignored without a second balance change.
- Lock the account before settling, enforce a unique callback event ID, and keep all updates in one PostgreSQL transaction. A small simulator worker may settle pending attempts for the UI demonstration; tests control settlement explicitly.
- The frontend shows PENDING/FAILED and polls for completion. It retains the same idempotency key when a network result is unknown and clears it only after a definite result.

## 3. Partial payment and overdue

- Add remaining principal to each instalment. Allocate a payment to the oldest not fully paid instalment, principal first, without spilling to a later instalment. Reject zero, negative, and overpayment amounts.
- On SUCCESS, reduce instalment remaining, drawdown outstanding, and account used by the same exact amount. Allow multiple successful repayments per instalment with separate keys. Mark PAID only at zero.
- A scheduled job uses injected `Clock` to mark an unpaid or partially paid instalment OVERDUE after its due date. Expose nonnegative `daysPastDue`; partial payment does not erase overdue state.
- Test cent amounts, duplicate attempts, month boundaries, late callbacks, and concurrent settlement.

## 4. Explainable demo assessment

- Add an optional simulated credit profile with on-time and missed payment counts. Define a deterministic `DEMO_V2` policy with a debt-to-income threshold and missed-payment rule while retaining `DEMO_V1` for applications without a profile.
- Store the profile snapshot, calculated ratio, policy version, decision, and reason codes with each application. Never represent the rules as real underwriting criteria.
- Test every new threshold and prove decisions can be reproduced from stored inputs.

## 5. Ledger, reconciliation, and audit

- Write an append-only signed principal movement for each drawdown and successful repayment in the same transaction. Backfill existing MVP movements in a migration.
- Reconciliation compares the ledger sum, drawdown outstanding sum, instalment remaining sum, and account used limit. Expose an admin-only read endpoint with mismatches.
- Record actor, action, resource, and time for account/application/drawdown/payment state changes. Expose own history and admin inspection.
- No external event broker is in this simulator. Add a transactional outbox only if a later request adds external publishing.

## Commit order and gates

1. Docs: this plan.
2. Accounts: migration, auth, ownership refactor, isolation tests; `mvn clean verify`; push.
3. Async payments: attempt state machine, callbacks, tests; `mvn clean verify`; push.
4. Partial/overdue: migration, service/job, clock tests; `mvn clean verify`; push.
5. Policy V2: profile, snapshot/reasons, boundary tests; `mvn clean verify`; push.
6. Ledger/audit: migrations, reconciliation, audit, tests; `mvn clean verify`; push.
7. Frontend: auth, payment polling, partial amounts and retry handling; `npm run format:check && npm run build`; push.
8. README and end-to-end review; push the verified state to `develop` in `nKhanGh/my-lending`.
