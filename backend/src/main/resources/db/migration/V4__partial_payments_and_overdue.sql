ALTER TABLE instalments ADD COLUMN remaining_amount NUMERIC(19, 2);
UPDATE instalments SET remaining_amount = CASE WHEN status = 'PAID' THEN 0 ELSE amount END;
ALTER TABLE instalments ALTER COLUMN remaining_amount SET NOT NULL;
ALTER TABLE instalments ADD CONSTRAINT ck_instalment_remaining
    CHECK (remaining_amount >= 0 AND remaining_amount <= amount);
ALTER TABLE instalments ADD COLUMN days_past_due INTEGER NOT NULL DEFAULT 0
    CHECK (days_past_due >= 0);
ALTER TABLE instalments DROP CONSTRAINT instalments_status_check;
ALTER TABLE instalments ADD CONSTRAINT ck_instalment_status
    CHECK (status IN ('UNPAID', 'OVERDUE', 'PAID'));
