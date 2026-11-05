ALTER TABLE orders
    ADD COLUMN public_id VARCHAR(20);

UPDATE orders
SET public_id = 'LEGACY-' || id
WHERE public_id IS NULL;

ALTER TABLE orders
    ALTER COLUMN public_id SET NOT NULL;

ALTER TABLE orders
    ADD CONSTRAINT uk_orders_public_id UNIQUE (public_id);