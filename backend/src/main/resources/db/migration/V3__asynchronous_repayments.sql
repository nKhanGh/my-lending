ALTER TABLE repayment_transactions DROP CONSTRAINT repayment_transactions_instalment_id_key;
ALTER TABLE repayment_transactions ALTER COLUMN paid_at DROP NOT NULL;
ALTER TABLE repayment_transactions DROP CONSTRAINT repayment_transactions_status_check;
ALTER TABLE repayment_transactions ADD CONSTRAINT ck_repayment_status
    CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED'));
ALTER TABLE repayment_transactions ADD COLUMN requested_at TIMESTAMP WITH TIME ZONE;
UPDATE repayment_transactions SET requested_at = paid_at;
ALTER TABLE repayment_transactions ALTER COLUMN requested_at SET NOT NULL;
ALTER TABLE repayment_transactions ADD COLUMN provider_event_id VARCHAR(120);
CREATE UNIQUE INDEX uq_repayment_provider_event ON repayment_transactions(provider_event_id)
    WHERE provider_event_id IS NOT NULL;
CREATE INDEX idx_repayment_pending ON repayment_transactions(status, requested_at)
    WHERE status = 'PENDING';
