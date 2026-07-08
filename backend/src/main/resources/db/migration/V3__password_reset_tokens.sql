-- ==================== Redefinição de senha ("esqueci minha senha") ====================
-- Guardamos apenas o HASH do token (nunca o token em si), com validade curta e uso único.
-- Mesmo padrão da tabela refresh_tokens.
CREATE TABLE password_reset_tokens (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_password_reset_tokens_user ON password_reset_tokens(user_id);
