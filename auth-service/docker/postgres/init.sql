-- PostgreSQL initialization script
-- The database 'auth_db' is already created by the POSTGRES_DB environment variable

CREATE TABLE IF NOT EXISTS users (
    id                  BIGSERIAL PRIMARY KEY,
    email               VARCHAR(255) UNIQUE NOT NULL,
    password_hash       VARCHAR(255),
    role                VARCHAR(50) NOT NULL,
    name                VARCHAR(255),
    handle              VARCHAR(255) UNIQUE,
    country             VARCHAR(255),
    timezone            VARCHAR(255),
    is_active           BOOLEAN DEFAULT TRUE,
    stripe_account_id   VARCHAR(255),
    kyc_status          VARCHAR(50) DEFAULT 'NONE',
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
