-- ============================================================
-- CineVerse — Modelagem do Banco (PostgreSQL 16)
-- Etapa 2: SaaS de entretenimento (sem hospedagem de vídeo)
-- Convenções: snake_case, PK bigint identity, timestamps UTC
-- ============================================================

-- Extensão para embeddings de recomendação por IA
CREATE EXTENSION IF NOT EXISTS vector;

-- ==================== MÓDULO: USERS ====================

CREATE TABLE users (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(120) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER',      -- USER | ADMIN
    locale          VARCHAR(5)   NOT NULL DEFAULT 'pt-BR',     -- pt-BR | en | es
    plan            VARCHAR(20)  NOT NULL DEFAULT 'FREE',      -- FREE | PREMIUM
    premium_until   TIMESTAMPTZ,
    email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ                                 -- soft delete (LGPD)
);

-- Perfis por conta (estilo streaming: até N perfis por usuário)
CREATE TABLE profiles (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name        VARCHAR(60) NOT NULL,
    avatar_url  VARCHAR(500),
    is_kids     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, name)
);

CREATE TABLE refresh_tokens (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE
);

-- ==================== MÓDULO: CATALOG ====================
-- Cache local dos metadados do TMDB (a fonte da verdade é a API;
-- guardamos só o necessário p/ busca, recomendação e afiliados)

CREATE TABLE titles (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tmdb_id         BIGINT NOT NULL,
    media_type      VARCHAR(10) NOT NULL,          -- movie | tv
    name            VARCHAR(300) NOT NULL,
    original_name   VARCHAR(300),
    overview        TEXT,
    poster_path     VARCHAR(300),
    backdrop_path   VARCHAR(300),
    release_date    DATE,
    runtime_min     INT,
    vote_average    NUMERIC(3,1),
    genres          TEXT[],                        -- ex.: {Drama,Crime}
    languages       TEXT[],
    embedding       vector(768),                   -- p/ recomendação por similaridade
    synced_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tmdb_id, media_type)
);

CREATE INDEX idx_titles_name_trgm ON titles USING gin (name gin_trgm_ops);
CREATE INDEX idx_titles_embedding ON titles USING ivfflat (embedding vector_cosine_ops);

-- Onde assistir legalmente (base dos links de afiliado)
CREATE TABLE watch_providers (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title_id    BIGINT NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    provider    VARCHAR(80) NOT NULL,              -- Netflix, Prime, Cinema...
    kind        VARCHAR(20) NOT NULL,              -- stream | rent | buy | cinema
    url         VARCHAR(700),
    country     VARCHAR(2) NOT NULL DEFAULT 'BR',
    UNIQUE (title_id, provider, kind, country)
);

-- ==================== MÓDULO: WATCHLIST & ATIVIDADE ====================

CREATE TABLE watchlists (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    profile_id  BIGINT NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    name        VARCHAR(120) NOT NULL,
    is_default  BOOLEAN NOT NULL DEFAULT FALSE,    -- "Favoritos"
    share_slug  VARCHAR(24) UNIQUE,                -- NULL = privada; slug = compartilhável
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE watchlist_items (
    watchlist_id BIGINT NOT NULL REFERENCES watchlists(id) ON DELETE CASCADE,
    title_id     BIGINT NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    added_by     BIGINT REFERENCES profiles(id),
    added_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (watchlist_id, title_id)
);

-- Colaboradores de listas compartilhadas
CREATE TABLE watchlist_members (
    watchlist_id BIGINT NOT NULL REFERENCES watchlists(id) ON DELETE CASCADE,
    profile_id   BIGINT NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    role         VARCHAR(10) NOT NULL DEFAULT 'EDITOR',   -- OWNER | EDITOR | VIEWER
    PRIMARY KEY (watchlist_id, profile_id)
);

-- Histórico / "continuar de onde parou" (marcação manual: assisti, parei no ep. X)
CREATE TABLE activity (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    profile_id  BIGINT NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    title_id    BIGINT NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    status      VARCHAR(20) NOT NULL,              -- WATCHING | WATCHED | DROPPED | PLANNED
    season      INT,
    episode     INT,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (profile_id, title_id)
);

-- ==================== MÓDULO: SOCIAL ====================

CREATE TABLE reviews (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    profile_id  BIGINT NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    title_id    BIGINT NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    rating      SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 10),
    body        TEXT,
    spoiler     BOOLEAN NOT NULL DEFAULT FALSE,
    status      VARCHAR(15) NOT NULL DEFAULT 'PUBLISHED', -- PUBLISHED | HIDDEN (moderação)
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (profile_id, title_id)
);

CREATE TABLE review_likes (
    review_id   BIGINT NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    profile_id  BIGINT NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    PRIMARY KEY (review_id, profile_id)
);

CREATE TABLE follows (
    follower_id  BIGINT NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    following_id BIGINT NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (follower_id, following_id),
    CHECK (follower_id <> following_id)
);

-- ==================== MÓDULO: GAMIFICATION ====================

CREATE TABLE gamification_profiles (
    profile_id      BIGINT PRIMARY KEY REFERENCES profiles(id) ON DELETE CASCADE,
    xp              INT NOT NULL DEFAULT 0,
    level           INT NOT NULL DEFAULT 1,
    streak_days     INT NOT NULL DEFAULT 0,
    longest_streak  INT NOT NULL DEFAULT 0,
    last_active_on  DATE
);

CREATE TABLE achievements (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR(60) NOT NULL UNIQUE,       -- ex.: FIRST_REVIEW, MARATHON_10
    name        VARCHAR(120) NOT NULL,
    description VARCHAR(300),
    icon        VARCHAR(60),
    xp_reward   INT NOT NULL DEFAULT 0
);

CREATE TABLE profile_achievements (
    profile_id     BIGINT NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    achievement_id BIGINT NOT NULL REFERENCES achievements(id) ON DELETE CASCADE,
    earned_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (profile_id, achievement_id)
);

-- Missões (diárias/semanais) geradas por template
CREATE TABLE missions (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR(60) NOT NULL,              -- ex.: RATE_3_TITLES
    name        VARCHAR(160) NOT NULL,
    cadence     VARCHAR(10) NOT NULL,              -- DAILY | WEEKLY
    target      INT NOT NULL DEFAULT 1,
    xp_reward   INT NOT NULL DEFAULT 10,
    active      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE mission_progress (
    profile_id  BIGINT NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    mission_id  BIGINT NOT NULL REFERENCES missions(id) ON DELETE CASCADE,
    period_key  VARCHAR(10) NOT NULL,              -- ex.: 2026-07-03 ou 2026-W27
    progress    INT NOT NULL DEFAULT 0,
    completed   BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (profile_id, mission_id, period_key)
);

-- ==================== MÓDULO: BILLING ====================

CREATE TABLE subscriptions (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    gateway         VARCHAR(20) NOT NULL,          -- MERCADO_PAGO | STRIPE
    gateway_sub_id  VARCHAR(120) UNIQUE,
    status          VARCHAR(20) NOT NULL,          -- ACTIVE | PAST_DUE | CANCELLED
    price_cents     INT NOT NULL,                  -- 990 = R$ 9,90
    current_period_end TIMESTAMPTZ,
    cancel_at_period_end BOOLEAN NOT NULL DEFAULT FALSE, -- cancelamento limpo, sem dark pattern
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE payments (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    subscription_id BIGINT REFERENCES subscriptions(id),
    kind            VARCHAR(15) NOT NULL,          -- SUBSCRIPTION | DONATION
    method          VARCHAR(15) NOT NULL,          -- PIX | CARD
    amount_cents    INT NOT NULL,
    status          VARCHAR(20) NOT NULL,          -- PENDING | PAID | FAILED | REFUNDED
    gateway_ref     VARCHAR(160),                  -- id do pagamento no gateway
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ==================== MÓDULO: AFFILIATE ====================

CREATE TABLE affiliate_links (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider_id BIGINT NOT NULL REFERENCES watch_providers(id) ON DELETE CASCADE,
    network     VARCHAR(60) NOT NULL,              -- ex.: Rakuten, Awin, Lomadee
    tracked_url VARCHAR(900) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE affiliate_clicks (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    link_id     BIGINT NOT NULL REFERENCES affiliate_links(id),
    profile_id  BIGINT REFERENCES profiles(id),
    clicked_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_aff_clicks_link_date ON affiliate_clicks (link_id, clicked_at);

-- ==================== MÓDULO: ADMIN / AUDIT ====================

CREATE TABLE audit_logs (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT,
    action      VARCHAR(60) NOT NULL,
    details     VARCHAR(1000),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Eventos de produto p/ dashboard de métricas (DAU, retenção, funil premium)
CREATE TABLE product_events (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    profile_id  BIGINT,
    event       VARCHAR(60) NOT NULL,              -- ex.: SEARCH, TITLE_VIEW, PAYWALL_VIEW
    metadata    JSONB,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_events_event_date ON product_events (event, created_at);
