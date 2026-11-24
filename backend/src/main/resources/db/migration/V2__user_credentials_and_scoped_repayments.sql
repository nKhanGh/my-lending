ALTER TABLE users ADD COLUMN email VARCHAR(255);
ALTER TABLE users ADD COLUMN password_hash VARCHAR(100);
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER';
ALTER TABLE users ADD CONSTRAINT ck_users_role CHECK (role IN ('CUSTOMER', 'ADMIN'));
CREATE UNIQUE INDEX uq_users_email_lower ON users (lower(email)) WHERE email IS NOT NULL;

ALTER TABLE repayment_transactions ADD COLUMN user_id UUID REFERENCES users(id);
UPDATE repayment_transactions r SET user_id = a.user_id
FROM instalments i, credit_drawdowns d, credit_accounts a
WHERE r.instalment_id = i.id AND i.drawdown_id = d.id AND d.account_id = a.id;
ALTER TABLE repayment_transactions ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE repayment_transactions DROP CONSTRAINT repayment_transactions_idempotency_key_key;
ALTER TABLE repayment_transactions ADD CONSTRAINT uq_repayment_user_key UNIQUE (user_id, idempotency_key);
