CREATE TABLE users (
    id UUID PRIMARY KEY,
    display_name VARCHAR(100) NOT NULL
);

CREATE TABLE credit_applications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    monthly_net_income NUMERIC(19, 2) NOT NULL CHECK (monthly_net_income >= 0),
    living_expenses NUMERIC(19, 2) NOT NULL CHECK (living_expenses >= 0),
    debt_payments NUMERIC(19, 2) NOT NULL CHECK (debt_payments >= 0),
    surplus NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('APPROVED', 'REJECTED')),
    decision_reason VARCHAR(40) NOT NULL,
    policy_version VARCHAR(20) NOT NULL,
    approved_limit NUMERIC(19, 2) NOT NULL CHECK (approved_limit >= 0),
    assessed_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_applications_user_assessed ON credit_applications(user_id, assessed_at DESC);

CREATE TABLE credit_accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    total_limit NUMERIC(19, 2) NOT NULL CHECK (total_limit > 0),
    used_limit NUMERIC(19, 2) NOT NULL DEFAULT 0 CHECK (used_limit >= 0 AND used_limit <= total_limit),
    currency VARCHAR(3) NOT NULL CHECK (currency = 'SGD'),
    status VARCHAR(20) NOT NULL CHECK (status = 'ACTIVE'),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE credit_drawdowns (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES credit_accounts(id),
    original_amount NUMERIC(19, 2) NOT NULL CHECK (original_amount >= 0.04),
    outstanding_principal NUMERIC(19, 2) NOT NULL CHECK (outstanding_principal >= 0 AND outstanding_principal <= original_amount),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'PAID_OFF')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    CONSTRAINT uq_drawdown_key UNIQUE (account_id, idempotency_key)
);
CREATE INDEX idx_drawdowns_account ON credit_drawdowns(account_id);

CREATE TABLE instalments (
    id UUID PRIMARY KEY,
    drawdown_id UUID NOT NULL REFERENCES credit_drawdowns(id),
    sequence_no INTEGER NOT NULL CHECK (sequence_no BETWEEN 1 AND 4),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('UNPAID', 'PAID')),
    paid_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_instalment_sequence UNIQUE (drawdown_id, sequence_no),
    CONSTRAINT ck_instalment_paid_at CHECK ((status = 'PAID') = (paid_at IS NOT NULL))
);
CREATE INDEX idx_instalments_drawdown ON instalments(drawdown_id);

CREATE TABLE repayment_transactions (
    id UUID PRIMARY KEY,
    instalment_id UUID NOT NULL UNIQUE REFERENCES instalments(id),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    status VARCHAR(20) NOT NULL CHECK (status = 'SUCCESS'),
    paid_at TIMESTAMP WITH TIME ZONE NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL UNIQUE
);

INSERT INTO users(id, display_name)
VALUES ('00000000-0000-0000-0000-000000000001', 'Demo User');
