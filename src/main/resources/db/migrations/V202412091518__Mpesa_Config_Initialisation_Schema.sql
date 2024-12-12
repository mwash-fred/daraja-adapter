-- Create mpesa_shortcode_configs table with support for different types
CREATE TABLE mpesa_shortcode_configs
(
    id                        BIGSERIAL PRIMARY KEY,
    uuid                      UUID                              DEFAULT gen_random_uuid(),
    shortcode                 VARCHAR(10)              NOT NULL,
    environment               VARCHAR(20)              NOT NULL DEFAULT 'SANDBOX'
        CHECK (environment IN ('SANDBOX', 'PRODUCTION')),
    shortcode_type            VARCHAR(20)              NOT NULL
        CHECK (shortcode_type IN ('COLLECTION', 'DISBURSEMENT', 'BOTH')),
    consumer_key              VARCHAR(500)             NOT NULL,
    consumer_secret           VARCHAR(500)             NOT NULL,
    passkey                   VARCHAR(500),
    initiator_name            VARCHAR(100),            -- For B2C/disbursement
    security_credential       VARCHAR(500),            -- For B2C/disbursement (encrypted)

    -- Collection URLs
    collection_callback_url   VARCHAR(255),
    collection_validation_url VARCHAR(255),
    collection_timeout_url    VARCHAR(255),
    collection_result_url     VARCHAR(255),

    -- Disbursement URLs
    disbursement_result_url   VARCHAR(255),
    disbursement_timeout_url  VARCHAR(255),
    disbursement_queue_url    VARCHAR(255),

    -- STK Push URLs
    stk_callback_url          VARCHAR(255),

    is_active                 BOOLEAN                  NOT NULL DEFAULT true,
    description               TEXT,
    created_date              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_date             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by               VARCHAR(50),
    modified_by              VARCHAR(50),

    CONSTRAINT unique_shortcode_env UNIQUE (shortcode, environment),
    -- Ensure collection URLs are present for COLLECTION or BOTH types
    CONSTRAINT collection_urls_check
        CHECK (
            shortcode_type != 'COLLECTION' OR
            (collection_callback_url IS NOT NULL AND
            collection_validation_url IS NOT NULL AND
            collection_timeout_url IS NOT NULL AND
            collection_result_url IS NOT NULL)
),
    -- Ensure disbursement URLs are present for DISBURSEMENT or BOTH types
    CONSTRAINT disbursement_urls_check
        CHECK (
            shortcode_type != 'DISBURSEMENT' OR
            (disbursement_result_url IS NOT NULL AND
             disbursement_timeout_url IS NOT NULL)
        )
);

-- Create mpesa_payments table with enhanced transaction support
CREATE TABLE mpesa_payments
(
    id                    BIGSERIAL PRIMARY KEY,
    uuid                  UUID                              DEFAULT gen_random_uuid(),
    transaction_id        VARCHAR(50) UNIQUE,
    origin_transaction_id VARCHAR(50),                      -- For linking refunds/reversals

    -- Transaction details
    transaction_type      VARCHAR(20)              NOT NULL
        CHECK (transaction_type IN
               ('STK_PUSH', 'PAYBILL_COLLECTION', 'BUY_GOODS_COLLECTION',
                'B2C_PAYMENT', 'B2C_SALARY', 'B2C_PROMOTION',
                'REVERSAL', 'REFUND')),
    transaction_status    VARCHAR(20)              NOT NULL DEFAULT 'PENDING'
        CHECK (transaction_status IN
               ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REVERSED', 'REFUNDED')),

    -- Financial details
    amount                DECIMAL(10, 2)           NOT NULL,
    currency              VARCHAR(3)               NOT NULL DEFAULT 'KES',
    charges_amount        DECIMAL(10, 2),
    org_account_balance   DECIMAL(10, 2),

    -- Party information
    business_short_code   VARCHAR(10),
    initiator_identifier  VARCHAR(50),             -- For B2C transactions
    phone_number          VARCHAR(15),
    payer_identifier      VARCHAR(50),             -- Could be phone number or business shortcode
    payee_identifier      VARCHAR(50),             -- Could be phone number or business shortcode

    -- Reference information
    account_reference     VARCHAR(50),
    bill_ref_number       VARCHAR(50),
    invoice_number        VARCHAR(50),
    transaction_desc      TEXT,
    third_party_trans_id  VARCHAR(50),

    -- Customer details (for collections)
    msisdn               VARCHAR(15),
    first_name           VARCHAR(50),
    middle_name          VARCHAR(50),
    last_name            VARCHAR(50),

    -- Timestamps
    created_date         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_date         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_date       TIMESTAMP WITH TIME ZONE,

    -- Metadata
    raw_request          JSONB,
    raw_callback         JSONB,
    error_message        TEXT,
    retry_count         INTEGER                  NOT NULL DEFAULT 0,
    created_by          VARCHAR(50),
    modified_by         VARCHAR(50),

    CONSTRAINT amount_positive CHECK (amount > 0)
);

-- Add comments to document encrypted fields
COMMENT ON COLUMN mpesa_shortcode_configs.consumer_key IS 'Encrypted consumer key';
COMMENT ON COLUMN mpesa_shortcode_configs.consumer_secret IS 'Encrypted consumer secret';
COMMENT ON COLUMN mpesa_shortcode_configs.passkey IS 'Encrypted passkey';
COMMENT ON COLUMN mpesa_shortcode_configs.security_credential IS 'Encrypted security credential';

-- Create indexes
CREATE INDEX idx_mpesa_payments_phone ON mpesa_payments (phone_number);
CREATE INDEX idx_mpesa_payments_status ON mpesa_payments (transaction_status);
CREATE INDEX idx_mpesa_payments_created ON mpesa_payments (created_date);
CREATE INDEX idx_mpesa_payments_transaction ON mpesa_payments (transaction_id);
CREATE INDEX idx_mpesa_payments_type ON mpesa_payments (transaction_type);
CREATE INDEX idx_mpesa_payments_payer ON mpesa_payments (payer_identifier);
CREATE INDEX idx_mpesa_payments_payee ON mpesa_payments (payee_identifier);

CREATE INDEX idx_shortcode_configs_active ON mpesa_shortcode_configs (is_active);
CREATE INDEX idx_shortcode_configs_env ON mpesa_shortcode_configs (environment);
CREATE INDEX idx_shortcode_configs_type ON mpesa_shortcode_configs (shortcode_type);

-- Update timestamp trigger
CREATE OR REPLACE FUNCTION update_modified_date_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.modified_date = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers
CREATE TRIGGER update_mpesa_payments_modifed_date
    BEFORE UPDATE
    ON mpesa_payments
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_date_column();

CREATE TRIGGER update_mpesa_shortcode_configs_modified_date
    BEFORE UPDATE
    ON mpesa_shortcode_configs
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_date_column();