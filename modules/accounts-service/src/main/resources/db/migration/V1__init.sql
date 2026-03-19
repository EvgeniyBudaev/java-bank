CREATE TABLE IF NOT EXISTS accounts
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    UUID           NOT NULL, -- Keycloak User ID
    balance    NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP      NULL
);

CREATE TABLE IF NOT EXISTS cash_outbox
(
    id         BIGSERIAL PRIMARY KEY,
    type       VARCHAR(255)   NOT NULL,
    user_id    UUID           NOT NULL, -- Keycloak User ID
    amount     NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    processed  BOOLEAN        NOT NULL DEFAULT FALSE
);

-- Индекс для ускорения выборки необработанных записей
-- CREATE INDEX IF NOT EXISTS idx_cash_outbox_processed
--     ON cash_outbox(processed, created_at);