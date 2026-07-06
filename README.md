# CineVerse — SaaS de Entretenimento

Plataforma para descobrir, organizar e compartilhar filmes e séries. **Não hospeda vídeo**: usa metadados legais do TMDB e aponta para onde assistir legalmente (base do sistema de afiliados). Monetização: Premium, afiliados e doações Pix.

## Stack

Java 21 · Spring Boot 3.3 · Spring Security (JWT + refresh rotation) · PostgreSQL 16 + pgvector · Flyway · Redis · Angular 17 · Swagger · Docker · Kubernetes · GitHub Actions · JUnit 5 + Testcontainers

## Módulos (Clean Architecture, monólito modular)

| Módulo | O que faz |
|---|---|
| `auth` | Registro, login, refresh token com rotação, perfis (até 4 por conta) |
| `catalog` | Integração TMDB (busca, trending, detalhes), sync local, onde assistir, recomendações |
| `watchlist` | Listas pessoais + compartilhamento por link público (`share_slug`) |
| `activity` | Histórico e "continuar assistindo" (marcação manual de temporada/episódio) |
| `social` | Avaliações (nota 1–10 + texto) com campo de moderação |
| `gamification` | XP, níveis, streaks 🔥 e conquistas — engajamento estilo Duolingo |
| `billing` | Assinatura Premium R$ 9,90 + doações Pix (**stub**, ver abaixo) |
| `affiliate` | Redirecionamento rastreado de cliques "onde assistir" |
| `admin` | Métricas (usuários, reviews, DAU) — protegido por ROLE_ADMIN |

## Pré-requisito: chave do TMDB (grátis)

1. Crie conta em https://www.themoviedb.org e gere uma **API Key (v3 auth)** em Settings → API (a string curta, não o Read Access Token v4).
2. Copie `.env.example` para `.env` e preencha `TMDB_API_KEY`:
   - Windows (PowerShell): `copy .env.example .env`
   - Linux/macOS: `cp .env.example .env`

## Rodando com Docker (recomendado)

O `docker compose` lê o `.env` automaticamente:

```bash
docker compose up --build
```

- Frontend: http://localhost:4200
- API + Swagger: http://localhost:8080/swagger-ui.html

## Rodando em desenvolvimento

```bash
docker compose up -d postgres redis
# backend — Linux/macOS:
cd backend && TMDB_API_KEY=sua-chave mvn spring-boot:run
# backend — Windows (PowerShell):
#   cd backend; $env:TMDB_API_KEY="sua-chave"; mvn spring-boot:run
cd frontend && npm install && npm start
```

O Flyway cria todo o schema (V1) e popula conquistas/missões (V2) automaticamente.

## Testes

```bash
cd backend && mvn test   # requer Docker (Testcontainers sobe Postgres+Redis reais)
```

## ⚠️ O que é stub / próximos passos

1. **Billing**: as tabelas, fluxo e webhook existem, mas a chamada real ao Mercado Pago precisa ser implementada (`BillingService`, TODOs marcados). **Não ative Premium sem validar o webhook com HMAC.**
2. **Missões diárias**: schema e seed prontos (`missions`, `mission_progress`); falta o serviço que incrementa progresso.
3. **Recomendações v2**: coluna `embedding vector(768)` pronta no schema; a v1 usa sobreposição de gêneros. Evolução: gerar embeddings dos títulos (ex.: API da Anthropic/OpenAI) e consultar por similaridade de cosseno.
4. **Anúncios do plano Free**: integrar AdSense no frontend.
5. **i18n do frontend**: o backend já respeita `locale` do usuário nas chamadas TMDB; falta traduzir a UI (Angular i18n).
6. **E-mail de verificação** (`email_verified` já existe no schema).

## Deploy em produção (Render + Supabase + Upstash + Vercel)

Arquitetura: **frontend na Vercel** → **backend na Render** → **Postgres no Supabase** + **Redis no Upstash**.

> ⚠️ **Segredos como o `TMDB_API_KEY` NÃO ficam no banco de dados** — são cadastrados como *variáveis de ambiente* no serviço que roda o backend (Render). O código lê tudo do ambiente (ver `application.yml`).

### 1. Banco — Supabase
1. Crie um projeto em https://supabase.com. Anote a **senha** do banco.
2. Em **Database → Extensions**, habilite a extensão `vector` (pgvector).
3. Em **Project Settings → Database → Connection info**, pegue host, porta, database e usuário.

### 2. Redis — Upstash
1. Crie um banco Redis em https://upstash.com (free tier).
2. Anote **endpoint (host)**, **porta** e **senha**. O Upstash exige TLS (por isso `REDIS_SSL=true`).

### 3. Backend — Render
1. **New → Web Service**, conectando este repositório.
2. **Root Directory:** `backend` · **Runtime:** Docker (usa o `backend/Dockerfile`).
3. Em **Environment**, cadastre as variáveis:

| Variável | Valor | Origem |
|---|---|---|
| `TMDB_API_KEY` | sua chave v3 | themoviedb.org |
| `JWT_SECRET` | segredo novo, mín. 32 chars | gere você mesmo¹ |
| `DB_HOST` | host do projeto | Supabase |
| `DB_PORT` | `5432` | Supabase |
| `DB_NAME` | `postgres` | Supabase |
| `DB_USER` | `postgres` (ou o do pooler) | Supabase |
| `DB_PASSWORD` | senha do banco | Supabase |
| `DB_SSLMODE` | `require` | **obrigatório no Supabase** |
| `REDIS_HOST` | endpoint | Upstash |
| `REDIS_PORT` | porta | Upstash |
| `REDIS_PASSWORD` | senha | Upstash |
| `REDIS_SSL` | `true` | **obrigatório no Upstash** |

¹ Gere um segredo forte, por ex. no PowerShell: `[Convert]::ToBase64String((1..32|%{Get-Random -Max 256}))`

> 💤 **Cold start:** no free tier o backend "dorme" após inatividade e a primeira requisição pode levar ~50s. Normal.

### 4. Frontend — Vercel
1. **Add New → Project**, importando este repositório.
2. **Root Directory:** `frontend` (a Vercel detecta Angular; o `frontend/vercel.json` já define build e output).
3. Abra `frontend/vercel.json` e troque `https://SEU-BACKEND.onrender.com` pela URL real do backend na Render.

O `vercel.json` reescreve `/api/*` para a Render, então o navegador só enxerga o próprio domínio da Vercel — **sem configurar CORS** e sem mudar o código Angular.

---

## Deploy (K8s)

```bash
docker build -t cineverse-backend:0.1.0 backend
kubectl apply -f k8s/secrets-example.yaml   # edite antes!
kubectl apply -f k8s/infra.yaml -f k8s/backend.yaml
```

CI: `.github/workflows/ci.yml` roda testes + build em cada push (job de deploy comentado, pronto pra configurar seu registry).
