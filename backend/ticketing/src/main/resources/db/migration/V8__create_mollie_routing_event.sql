CREATE TABLE mollie_routing_event (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(255) NOT NULL,
    partner_amount NUMERIC(10, 2) NOT NULL,
    routed_at TIMESTAMP NOT NULL,

    CONSTRAINT uk_mollie_routing_event_payment_id
        UNIQUE (payment_id)
);