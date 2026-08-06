CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(255) NOT NULL UNIQUE,
    account_holder_name VARCHAR(255) NOT NULL,
    account_currency_type VARCHAR(10),
    balance DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    source_account VARCHAR(255),
    destination_account VARCHAR(255),
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    currency VARCHAR(3),
    error_code VARCHAR(50),
    description VARCHAR(500)
);

-- Payment History Table for Audit Trail
CREATE TABLE IF NOT EXISTS payment_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    CONSTRAINT fk_payment_history_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE,
    INDEX idx_payment_history_payment_id (payment_id)
);
