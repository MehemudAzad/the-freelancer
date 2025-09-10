-- Create escrow table
CREATE TABLE escrow (
    id VARCHAR(255) PRIMARY KEY,
    milestone_id BIGINT NOT NULL,
    payment_intent_id VARCHAR(255) UNIQUE NOT NULL,
    amount_cents BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'HELD',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create payouts table
CREATE TABLE payouts (
    id VARCHAR(255) PRIMARY KEY,
    milestone_id BIGINT NOT NULL,
    transfer_id VARCHAR(255) UNIQUE NOT NULL,
    destination_account_id VARCHAR(255) NOT NULL,
    amount_cents BIGINT NOT NULL,
    fee_cents BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create refunds table
CREATE TABLE refunds (
    id VARCHAR(255) PRIMARY KEY,
    escrow_id VARCHAR(255) NOT NULL,
    refund_id VARCHAR(255) UNIQUE NOT NULL,
    amount_cents BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (escrow_id) REFERENCES escrow(id) ON DELETE CASCADE
);

-- Create ledger table for audit trail
CREATE TABLE ledger (
    id VARCHAR(255) PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    source_ref VARCHAR(255),
    dest_ref VARCHAR(255),
    amount_cents BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    meta TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_escrow_milestone_id ON escrow(milestone_id);
CREATE INDEX idx_escrow_payment_intent_id ON escrow(payment_intent_id);
CREATE INDEX idx_escrow_status ON escrow(status);

CREATE INDEX idx_payouts_milestone_id ON payouts(milestone_id);
CREATE INDEX idx_payouts_transfer_id ON payouts(transfer_id);
CREATE INDEX idx_payouts_destination_account ON payouts(destination_account_id);

CREATE INDEX idx_refunds_escrow_id ON refunds(escrow_id);
CREATE INDEX idx_refunds_refund_id ON refunds(refund_id);

CREATE INDEX idx_ledger_type ON ledger(type);
CREATE INDEX idx_ledger_source_ref ON ledger(source_ref);
CREATE INDEX idx_ledger_dest_ref ON ledger(dest_ref);
CREATE INDEX idx_ledger_created_at ON ledger(created_at);
