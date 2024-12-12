-- Remove ENUM types and use VARCHAR for type constraints
DROP TYPE IF EXISTS transaction_status CASCADE;
DROP TYPE IF EXISTS transaction_type CASCADE;
DROP TYPE IF EXISTS mpesa_environment CASCADE;

-- Create mpesa_payments table
CREATE TABLE mpesa_payments
(
    id                   BIGSERIAL PRIMARY KEY,
    uuid                 UUID                              DEFAULT gen_random_uuid(),
    transaction_id       VARCHAR(50) UNIQUE,
    phone_number         VARCHAR(15)              NOT NULL,
    amount               DECIMAL(10, 2)           NOT NULL,
    currency             VARCHAR(3)               NOT NULL DEFAULT 'KES',
    account_reference    VARCHAR(50),
    transaction_desc     TEXT,
    transaction_type     VARCHAR(20)              NOT NULL CHECK (transaction_type IN ('STK_PUSH', 'PAYBILL', 'BUY_GOODS')),
    transaction_status   VARCHAR(20)              NOT NULL DEFAULT 'PENDING' CHECK (transaction_status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    business_short_code  VARCHAR(10),
    bill_ref_number      VARCHAR(50),
    invoice_number       VARCHAR(50),
    org_account_balance  DECIMAL(10, 2),
    third_party_trans_id VARCHAR(50),
    msisdn               VARCHAR(15),
    first_name           VARCHAR(50),
    middle_name          VARCHAR(50),
    last_name            VARCHAR(50),
    created_date         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by           VARCHAR(50),
    updated_date         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by          VARCHAR(50),
    modified_date        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_date       TIMESTAMP WITH TIME ZONE,
    raw_request          JSONB,
    raw_callback         JSONB,
    error_message        TEXT,
    retry_count          INTEGER                  NOT NULL DEFAULT 0,
    CONSTRAINT amount_positive CHECK (amount > 0)
);

-- Create mpesa_shortcode_configs table
CREATE TABLE mpesa_shortcode_configs
(
    id               BIGSERIAL PRIMARY KEY,
    uuid             UUID                              DEFAULT gen_random_uuid(),
    shortcode        VARCHAR(10)              NOT NULL,
    environment      VARCHAR(20)              NOT NULL DEFAULT 'SANDBOX' CHECK (environment IN ('SANDBOX', 'PRODUCTION')),
    consumer_key     VARCHAR(100)             NOT NULL,
    consumer_secret  VARCHAR(100)             NOT NULL,
    passkey          VARCHAR(100),
    callback_url     VARCHAR(255)             NOT NULL,
    timeout_url      VARCHAR(255)             NOT NULL,
    result_url       VARCHAR(255)             NOT NULL,
    stk_callback_url VARCHAR(255),
    is_active        BOOLEAN                  NOT NULL DEFAULT true,
    description      TEXT,
    created_date     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_date    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(50),
    modified_by      VARCHAR(50),
    CONSTRAINT unique_shortcode_env UNIQUE (shortcode, environment)
);

-- Create update timestamp trigger function
CREATE OR REPLACE FUNCTION update_updated_date_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_date = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for both tables
CREATE TRIGGER update_mpesa_payments_updated_date
    BEFORE UPDATE ON mpesa_payments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_date_column();

CREATE TRIGGER update_mpesa_shortcode_configs_updated_date
    BEFORE UPDATE ON mpesa_shortcode_configs
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_date_column();

-- Create indexes
CREATE INDEX idx_mpesa_payments_phone ON mpesa_payments (phone_number);
CREATE INDEX idx_mpesa_payments_status ON mpesa_payments (transaction_status);
CREATE INDEX idx_mpesa_payments_created ON mpesa_payments (created_date);
CREATE INDEX idx_mpesa_payments_transaction ON mpesa_payments (transaction_id);

CREATE INDEX idx_shortcode_configs_active ON mpesa_shortcode_configs (is_active);
CREATE INDEX idx_shortcode_configs_env ON mpesa_shortcode_configs (environment);