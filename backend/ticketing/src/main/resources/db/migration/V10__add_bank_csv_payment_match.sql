CREATE TABLE bank_csv_payment_match (
    id BIGSERIAL PRIMARY KEY,

    order_id BIGINT NOT NULL
        REFERENCES orders(id),

    file_sha256 VARCHAR(64) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    approved_by VARCHAR(200) NOT NULL,
    approved_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_bank_csv_payment_order
        UNIQUE (order_id)
);