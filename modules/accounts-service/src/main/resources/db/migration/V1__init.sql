CREATE TABLE IF NOT EXISTS accounts
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    UUID           NOT NULL, -- Keycloak User ID
    balance    NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP      NULL
);

CREATE TABLE IF NOT EXISTS accounts_outbox
(
    id         BIGSERIAL PRIMARY KEY,
    type       VARCHAR(255)   NOT NULL,
    user_id    UUID           NOT NULL, -- Keycloak User ID
    amount     NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    processed  BOOLEAN        NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS notification_idempotency_keys
(
    idempotency_key VARCHAR(255) PRIMARY KEY,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    -- В продакшене стоит добавить задачу для очистки старых записей (TTL)
);

-- Индекс для ускорения выборки необработанных записей
-- CREATE INDEX IF NOT EXISTS idx_accounts_outbox_processed ON accounts_outbox(processed, created_at);

-- Индекс для ускорения выборки по ключу
-- CREATE INDEX IF NOT EXISTS idx_idempotency_created_at ON notification_idempotency_keys(created_at);