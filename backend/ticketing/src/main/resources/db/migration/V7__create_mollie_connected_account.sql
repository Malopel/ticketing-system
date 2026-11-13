CREATE TABLE mollie_connected_account (
    id BIGSERIAL PRIMARY KEY,
    organization_id VARCHAR(255) NOT NULL,
    refresh_token TEXT NOT NULL,
    access_token_expires_at TIMESTAMP NOT NULL,

    CONSTRAINT uk_mollie_connected_account_organization_id
        UNIQUE (organization_id)
);