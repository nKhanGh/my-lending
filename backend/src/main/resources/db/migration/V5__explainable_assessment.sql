CREATE TABLE credit_profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    on_time_payments INTEGER NOT NULL CHECK (on_time_payments >= 0),
    missed_payments INTEGER NOT NULL CHECK (missed_payments >= 0),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
ALTER TABLE credit_applications ADD COLUMN on_time_payments INTEGER;
ALTER TABLE credit_applications ADD COLUMN missed_payments INTEGER;
ALTER TABLE credit_applications ADD COLUMN debt_to_income_ratio NUMERIC(8, 4);
ALTER TABLE credit_applications ADD CONSTRAINT ck_application_profile_snapshot CHECK (
    (on_time_payments IS NULL AND missed_payments IS NULL AND debt_to_income_ratio IS NULL)
    OR (on_time_payments >= 0 AND missed_payments >= 0 AND debt_to_income_ratio >= 0)
);
