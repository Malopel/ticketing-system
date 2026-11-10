CREATE TABLE printed_ticket_batch (
    id BIGSERIAL PRIMARY KEY,
    concert_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL UNIQUE,
    label VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_printed_ticket_batch_concert
        FOREIGN KEY (concert_id)
            REFERENCES concert(id),

    CONSTRAINT fk_printed_ticket_batch_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id)
);