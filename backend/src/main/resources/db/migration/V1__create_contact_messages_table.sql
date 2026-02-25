-- V1__create_contact_messages_table.sql
-- Initial schema migration

CREATE TABLE IF NOT EXISTS contact_messages (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(150)  NOT NULL,
    subject     VARCHAR(200),
    message     TEXT          NOT NULL,
    is_read     BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_contact_messages_is_read ON contact_messages (is_read);
CREATE INDEX idx_contact_messages_created_at ON contact_messages (created_at DESC);

COMMENT ON TABLE contact_messages IS 'Messages submitted via portfolio contact form';
