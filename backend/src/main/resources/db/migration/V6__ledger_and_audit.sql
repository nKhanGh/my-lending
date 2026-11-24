CREATE TABLE principal_movements (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    account_id UUID NOT NULL REFERENCES credit_accounts(id),
    drawdown_id UUID NOT NULL REFERENCES credit_drawdowns(id),
    event_type VARCHAR(20) NOT NULL CHECK (event_type IN ('DRAWDOWN', 'REPAYMENT')),
    reference_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount <> 0),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_principal_movement_event UNIQUE (event_type, reference_id)
);
CREATE INDEX idx_principal_movements_account ON principal_movements(account_id, occurred_at);

INSERT INTO principal_movements(id, user_id, account_id, drawdown_id, event_type, reference_id, amount, occurred_at)
SELECT gen_random_uuid(), a.user_id, d.account_id, d.id, 'DRAWDOWN', d.id, d.original_amount, d.created_at
FROM credit_drawdowns d JOIN credit_accounts a ON a.id = d.account_id;
INSERT INTO principal_movements(id, user_id, account_id, drawdown_id, event_type, reference_id, amount, occurred_at)
SELECT gen_random_uuid(), a.user_id, d.account_id, d.id, 'REPAYMENT', r.id, -r.amount, r.paid_at
FROM repayment_transactions r
JOIN instalments i ON i.id = r.instalment_id
JOIN credit_drawdowns d ON d.id = i.drawdown_id
JOIN credit_accounts a ON a.id = d.account_id
WHERE r.status = 'SUCCESS';

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    actor_user_id UUID REFERENCES users(id),
    subject_user_id UUID NOT NULL REFERENCES users(id),
    action VARCHAR(60) NOT NULL,
    resource_type VARCHAR(40) NOT NULL,
    resource_id UUID NOT NULL,
    detail VARCHAR(255),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_audit_subject ON audit_events(subject_user_id, occurred_at DESC);

CREATE FUNCTION reject_append_only_mutation() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'append-only table cannot be changed';
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER principal_movements_append_only BEFORE UPDATE OR DELETE ON principal_movements
    FOR EACH ROW EXECUTE FUNCTION reject_append_only_mutation();
CREATE TRIGGER audit_events_append_only BEFORE UPDATE OR DELETE ON audit_events
    FOR EACH ROW EXECUTE FUNCTION reject_append_only_mutation();
