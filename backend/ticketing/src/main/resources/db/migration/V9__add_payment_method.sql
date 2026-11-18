ALTER TABLE orders
    ADD COLUMN payment_method VARCHAR(32);

UPDATE orders
SET payment_method = 'MOLLIE'
WHERE payment_method IS NULL;