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
| `DB_HOST` | host do **Session pooler** (IPv4), ex. `aws-1-...pooler.supabase.com` | Supabase |
| `DB_PORT` | `5432` (Session pooler) | Supabase |
| `DB_NAME` | `postgres` | Supabase |
| `DB_USER` | `postgres.<ref-do-projeto>` (com o ID do projeto — **não** só `postgres`) | Supabase |
| `DB_PASSWORD` | senha do banco | Supabase |
| `DB_SSLMODE` | `require` | **obrigatório no Supabase** |
| `REDIS_HOST` | endpoint | Upstash |
| `REDIS_PORT` | porta | Upstash |
| `REDIS_PASSWORD` | senha | Upstash |
| `REDIS_SSL` | `true` | **obrigatório no Upstash** |
| `FRONTEND_URL` | URL do frontend na Vercel (aceita lista separada por vírgula p/ CORS) | Vercel |
| `MAIL_USERNAME` | e-mail Gmail completo (opcional — sem ele, reset em modo demo) | Gmail |
| `MAIL_PASSWORD` | "senha de app" de 16 letras do Gmail (opcional) | Gmail |

¹ Gere um segredo forte, por ex. no PowerShell: `[Convert]::ToBase64String((1..32|%{Get-Random -Max 256}))`

> ℹ️ A `spring.datasource.url` é montada como JDBC: `jdbc:postgresql://HOST:5432/postgres?sslmode=require`.
> Usuário e senha vão em variáveis **separadas** (`DB_USER`/`DB_PASSWORD`) — nunca embutidos numa `postgres://...` com credenciais na URL.

### 3.1 Manter o backend acordado (keep-alive)

No free tier o Render **hiberna após ~15 min** de inatividade, e a primeira requisição depois disso leva ~1–3 min (cold start). Para evitar:

1. Crie uma conta grátis no **[UptimeRobot](https://uptimerobot.com)**.
2. **+ New monitor** → tipo **HTTP(s)** → URL: `https://SEU-BACKEND.onrender.com/actuator/health` → intervalo **5 min**.
3. Pronto: o ping constante mantém o serviço no ar (cabe nas 750 h/mês grátis de **um** serviço).

> ⚠️ **Não** use GitHub Actions para o keep-alive em repositório **privado**: um cron a cada 5 min consome ~8.640 min/mês e estoura a cota grátis de 2.000 min/mês. UptimeRobot é externo e não gasta essa cota.
>
> O `/actuator/health` responde `UP` mesmo se Redis/SMTP estiverem fora, porque esses health indicators estão desligados (`management.health.redis.enabled=false` e `mail.enabled=false`) — assim o keep-alive e o deploy não quebram por causa de serviço externo.

### 4. Frontend — Vercel
1. **Add New → Project**, importando este repositório.
2. **Root Directory:** `frontend` (a Vercel detecta Angular; o `frontend/vercel.json` já define build e output `dist/cineverse/browser`).
3. Em `frontend/vercel.json`, confirme que o `destination` do rewrite de `/api/*` aponta para a URL real do backend na Render.

O `vercel.json` faz duas coisas:
- reescreve `/api/*` para a Render (o navegador só enxerga o domínio da Vercel — **sem CORS** no fluxo normal e sem mudar o código Angular);
- tem um **fallback de SPA** (`/(.*) → /index.html`) — essencial para acessar rotas direto pela URL, como o link do e-mail de reset `/redefinir-senha?token=...` (sem ele, a Vercel devolveria 404).

> **Nota de arquitetura:** o CineVerse usa caminhos relativos `/api/...` + rewrite da Vercel (não uma `apiBaseUrl` por `environment.prod.ts`). Por isso **não** há `fileReplacements` no `angular.json`: não é necessário e a página nunca "bate em si mesma", pois `/api` é sempre proxied para a Render.

### 5. E-mail (recuperação de senha)

O fluxo "esqueci minha senha" gera um **token de uso único** com expiração (30 min), salvo no banco (`password_reset_tokens`, guardando só o hash). O envio do link usa **SMTP (Gmail)**:

1. Ative a **verificação em 2 etapas** na sua Conta Google.
2. Gere uma **senha de app** em https://myaccount.google.com/apppasswords (16 letras).
3. Na Render, defina `MAIL_USERNAME` (e-mail completo) e `MAIL_PASSWORD` (a senha de app, sem espaços).

> 🧪 **Modo demo:** enquanto `MAIL_USERNAME`/`MAIL_PASSWORD` estiverem vazias — ou se o SMTP falhar — o backend **não quebra a requisição**: ele apenas **loga o link de reset no console** e retorna `200`. O token **nunca** é devolvido na resposta da API. Assim dá para testar o fluxo antes de ligar o e-mail, e é só preencher as variáveis depois.
>
> A resposta do "esqueci a senha" é **genérica** ("se este e-mail estiver cadastrado, enviaremos um link") — não revela se o e-mail existe (anti-enumeração de contas). Os timeouts de SMTP (8s) evitam que um servidor de e-mail travado pendure a requisição.

Usuário **logado** troca a senha em **Configurações** (`POST /api/account/change-password`, exige a senha atual).

### 6. Reset manual de senha via SQL (suporte por WhatsApp)

Enquanto o e-mail transacional com domínio próprio não está ativo, a tela "Esqueci minha senha" oferece um botão **"Falar com o suporte no WhatsApp"**. O suporte pode redefinir a senha direto no banco com um **hash BCrypt** já calculado (mesmo formato que o `BCryptPasswordEncoder` do Spring gera/valida — prefixo `$2a`/`$2b`):

```sql
-- Tabela: users | coluna do hash: password_hash (ver @Table/@Column da entidade User)
-- Senha temporária "cineverse123" (hash BCrypt força 10, já validado):
UPDATE users
   SET password_hash = '$2a$10$40ZLVVilgY6cyz5hbx0gR.bmwQa5m/vudoL/YrJdzQBH01igtIrR2'
 WHERE email = 'usuario@exemplo.com';
```

Depois disso o usuário entra com a senha temporária **`cineverse123`** e deve trocá-la **imediatamente** em **Configurações** (item acima).

> ⚠️ `cineverse123` é uma senha **conhecida e temporária** — oriente o usuário a trocá-la no primeiro login. Para gerar o hash de outra senha, use o `BCryptPasswordEncoder` (força 10) do próprio backend.

---

## Deploy (K8s)

```bash
docker build -t cineverse-backend:0.1.0 backend
kubectl apply -f k8s/secrets-example.yaml   # edite antes!
kubectl apply -f k8s/infra.yaml -f k8s/backend.yaml
```

CI: `.github/workflows/ci.yml` roda testes + build em cada push (job de deploy comentado, pronto pra configurar seu registry).
