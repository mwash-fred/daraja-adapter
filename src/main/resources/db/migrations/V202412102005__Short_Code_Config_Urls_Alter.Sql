-- Alter table to increase length of various fields
ALTER TABLE mpesa_shortcode_configs
ALTER
COLUMN callback_url TYPE VARCHAR(255),
    ALTER
COLUMN timeout_url TYPE VARCHAR(255),
    ALTER
COLUMN result_url TYPE VARCHAR(255),
    ALTER
COLUMN stk_callback_url TYPE VARCHAR(255),
    ALTER
COLUMN consumer_key TYPE VARCHAR(255),
    ALTER
COLUMN consumer_secret TYPE VARCHAR(255),
    ALTER
COLUMN passkey TYPE VARCHAR(255);