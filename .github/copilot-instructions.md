# Core User Flows – Detailed Breakdown

---

## 1) Client Posts a Job

### Steps

1. **Select Project Template**

   * Templates: SaaS MVP, Shopify App, Next.js Site, REST API, Data Pipeline, LLM App, Mobile App, DevOps Task, Security Audit.
   * Each template pre-populates milestones, deliverables, and best‑practice DoD (Definition of Done).

2. **Define Scope & Milestones**

   * Break job into milestones (e.g., *UI prototype → Backend API → Integration → QA/Deploy*).
   * For each milestone, define:

     * Deliverable description (e.g., login page with JWT auth).
     * DoD checklist (unit tests, docs, deploy link).
     * Payment amount + due date.

3. **Budget & Terms**

   * Budget type: *Fixed price* (escrow per milestone) or *Hourly* (weekly payouts).
   * Client sets budget range (e.g., \$1,000–\$2,500).
   * Timeline with expected delivery dates.
   * Repo access rules (read/write, branch restrictions).
   * Legal: NDA & IP assignment toggle.

4. **Optional Attachments**

   * Upload design mockups (Figma, PSD), specs (PDF/Markdown), sample datasets (CSV/JSON).
   * Auto‑scope wizard analyzes inputs → suggests draft milestones & timeline.

---

## 2) Matching & Search

### Algorithm Inputs

* **Verified Skills**: badges/tests matching job stack.
* **Portfolio Fit**: overlap between client’s required stack and freelancer’s repo tags/projects.
* **Reviews**: weighted average (recent reviews boosted).
* **Delivery Score**: % on‑time milestone completion.
* **Availability**: freelancer’s declared bandwidth.

### Ranking Formula

```
score = w1*skill_match + w2*portfolio_fit + w3*reviews + w4*delivery_score + w5*availability
```

* Boost: verified badges, past on‑time delivery, sandbox demos.

### Interaction

* **Client Actions**:

  * Browse results, filter by stack, budget, timezone, languages.
  * Invite freelancers to apply.
* **Freelancer Actions**:

  * Apply with a scoped proposal (milestones, pricing, ETA, clarifying questions).
  * Attach case studies or demo repos.

---

## 3) Contracting

### Payments

* **Escrow (Fixed Price)**: Client deposits funds → held until milestone acceptance.
* **Hourly Contracts**: Weekly timesheet approvals, capped hours, auto‑billing.

### Agreement

* Platform generates **Statement of Work (SoW)**:

  * Scope, deliverables, timeline, payment schedule, NDA/IP terms.
  * Both parties accept via digital signature.

### Infrastructure

* **Stripe Connect Custom** for marketplace payments, split fees, KYC/AML.
* Admin can refund, hold, or override payouts.

---

## 4) Delivery Workspace (Job Room)

### Features

* **Communication**: Real‑time chat, threaded discussions, mentions, file uploads.
* **Collaboration Links**: GitHub/GitLab PRs, preview URLs (Vercel/Netlify/Render).
* **CI Integration**: show lint/test/coverage status on PR submissions.
* **Task Board**: Kanban with milestone tasks, drag/drop progress.
* **Calendar**: Standup scheduling, milestone deadlines.

### Workflow

1. Freelancer submits milestone deliverable.
2. CI pipeline auto‑runs (tests, lint, deploy preview).
3. Client reviews, accepts/rejects with feedback.
4. On acceptance → escrow funds released automatically.

---

## 5) Disputes

### Resolution Ladder

1. **Self‑Resolution**: Client & freelancer negotiate directly in chat, can edit scope or partial payment.
2. **Platform Mediation**: Support agent moderates, referencing chat logs & agreed milestones.
3. **Expert Reviewer (Optional Paid Service)**: Senior engineer audits deliverable code, CI logs, and repo history. Provides recommendation.
4. **Admin Decision**: Final ruling based on evidence.

### Evidence Sources

* Pull requests & commit logs.
* CI results (test coverage, error logs).
* Preview/demo links.
* Chat & file exchange history.
* Acceptance test runs and milestone DoD checklists.

---

# Database & ERD (v1)

Below is a pragmatic, production‑ready relational model for **PostgreSQL**. It’s split into modules so you can ship incrementally. Types are indicative; adjust to your ORM (e.g., Prisma) and naming conventions.

## Conventions

* `id` = ULID/UUID (recommended: ULID).
* `*_id` FK with `ON UPDATE CASCADE ON DELETE RESTRICT` unless noted.
* Monetary fields as `BIGINT amount_cents` + `TEXT currency` (ISO‑4217).
* Timestamps in UTC, `created_at`, `updated_at` with triggers.
* Soft delete only where needed (`deleted_at`).
* Enumerations via `CHECK` or native `ENUM`.

## Module A — Identity & Access

```
users(id PK, email UNIQUE, password_hash, role CHECK(role IN ('client','freelancer','admin')),
      name, handle UNIQUE, country, timezone, is_active BOOL DEFAULT TRUE,
      stripe_account_id, kyc_status CHECK(kyc_status IN ('none','pending','verified','rejected')),
      created_at, updated_at)

profiles(user_id PK/FK→users.id, headline, bio, hourly_rate_cents, currency,
         availability CHECK(availability IN ('full_time','part_time','occasional','unavailable')),
         languages TEXT[], skills TEXT[], location_text,
         github_username, gitlab_username, website_url, linkedin_url,
         delivery_score NUMERIC(5,2) DEFAULT 0.0, -- % on‑time
         review_avg NUMERIC(3,2) DEFAULT 0.0, reviews_count INT DEFAULT 0,
         created_at, updated_at)

gigs(id PK, profile_id FK→profiles.user_id, title, description, status CHECK(status IN ('draft','active','paused','archived')),
     category, tags TEXT[], review_avg NUMERIC(3,2), reviews_count INT, created_at, updated_at)

gig_packages(id PK, gig_id FK→gigs.id, tier CHECK(tier IN ('basic','standard','premium')), title, description,
             price_cents BIGINT, currency, delivery_days INT, revisions INT, created_at, updated_at)

gig_media(id PK, gig_id FK→gigs.id, url, content_type, kind CHECK(kind IN ('image','video','document')),
          order_index INT, created_at)

profile_badges(id PK, user_id FK, type, score NUMERIC(4,1), issued_at, expires_at)

sessions(id PK, user_id FK, user_agent, ip, expires_at, created_at)

api_keys(id PK, user_id FK, name, hashed_key, last_used_at, created_at)
```

## Module B — Jobs & Proposals

```
jobs(id PK, client_id FK→users.id, title, description, stack TEXT[],
     budget_type CHECK(budget_type IN ('fixed','hourly')),
     min_budget_cents, max_budget_cents, currency,
     nda_required BOOL DEFAULT FALSE, ip_assignment BOOL DEFAULT TRUE,
     repo_link, status CHECK(status IN ('draft','open','in_progress','completed','cancelled')) DEFAULT 'open',
     created_at, updated_at)

job_attachments(id PK, job_id FK, kind CHECK(kind IN ('spec','wireframe','dataset','other')),
                url, filename, bytes BIGINT, created_at)

proposals(id PK, job_id FK, freelancer_id FK→users.id, cover TEXT,
          total_cents, currency, delivery_days INT,
          status CHECK(status IN ('submitted','withdrawn','declined','accepted')) DEFAULT 'submitted',
          created_at, updated_at)

proposal_milestones(id PK, proposal_id FK, title, description,
                    amount_cents, currency, due_date, order_index INT,
                    dod JSONB, -- Definition Of Done checklist
                    created_at, updated_at)

invites(id PK, job_id FK, client_id FK, freelancer_id FK,
        status CHECK(status IN ('sent','accepted','declined','expired')),
        created_at, updated_at)
```

## Module C — Contracts & Milestones

```
contracts(id PK, job_id FK UNIQUE, proposal_id FK UNIQUE, -- 1 active contract per job
          terms_json JSONB, start_date, end_date,
          payment_model CHECK(payment_model IN ('fixed','hourly')),
          status CHECK(status IN ('active','paused','completed','cancelled')),
          created_at, updated_at)

milestones(id PK, contract_id FK, title, description,
           amount_cents, currency, due_date,
           status CHECK(status IN ('planned','funding_required','funded','in_progress','submitted','accepted','rejected','disputed','paid')) DEFAULT 'funding_required',
           order_index INT, dod JSONB,
           submitted_at, accepted_at, rejected_at, disputed_at, paid_at,
           created_at, updated_at)
```

## Module D — Escrow, Payouts & Refunds (Stripe Connect)

```
escrow(id PK, milestone_id FK UNIQUE, payment_intent_id UNIQUE,
       amount_cents, currency,
       status CHECK(status IN ('held','released','refunded','disputed')) DEFAULT 'held',
       created_at, updated_at)

payouts(id PK, milestone_id FK, transfer_id UNIQUE, destination_account_id,
        amount_cents, fee_cents, currency,
        status CHECK(status IN ('initiated','paid','failed','reversed')),
        created_at, updated_at)

refunds(id PK, escrow_id FK, refund_id UNIQUE,
        amount_cents, currency,
        status CHECK(status IN ('initiated','succeeded','failed')),
        created_at, updated_at)

ledger(id PK, type CHECK(type IN ('charge','transfer','refund','fee','reverse_transfer')),
       source_ref TEXT, dest_ref TEXT, amount_cents, currency,
       meta JSONB, created_at)
```

## Module E — Workspace (Collab)

```
rooms(id PK, job_id FK UNIQUE, created_at)

messages(id PK, room_id FK, user_id FK, body TEXT, attachments JSONB,
         reply_to_id FK→messages.id, edited_at, created_at)

files(id PK, room_id FK, uploader_id FK, url, filename, content_type,
      bytes BIGINT, checksum, created_at)

tasks(id PK, room_id FK, title, status CHECK(status IN ('todo','doing','done')),
      assignee_id FK→users.id, order_index INT, created_at, updated_at)

calendar_events(id PK, room_id FK, title, starts_at, ends_at, timezone,
                kind CHECK(kind IN ('standup','deadline','meeting')), created_at)
```

## Module F — Code & CI Integrations

```
repos(id PK, job_id FK, provider CHECK(provider IN ('github','gitlab','bitbucket')),
      external_id, name, default_branch, installation_id, created_at)

pull_requests(id PK, repo_id FK, number INT, title, url, author,
              status CHECK(status IN ('open','merged','closed')),
              created_at, updated_at)

ci_checks(id PK, pr_id FK, name, status CHECK(status IN ('queued','running','passed','failed')),
          summary TEXT, url, created_at)

previews(id PK, milestone_id FK, provider CHECK(provider IN ('vercel','netlify','render','other')),
         url, created_at)
```

## Module G — Reviews & Reputation

```
reviews(id PK, job_id FK, milestone_id FK NULL, from_user_id FK, to_user_id FK,
        ratings JSONB, -- {quality, comms, timeliness}
        text TEXT, created_at)

reputation_snapshots(id PK, user_id FK, delivery_score NUMERIC(5,2), review_avg NUMERIC(3,2),
                     on_time_rate NUMERIC(5,2), completed_milestones INT,
                     captured_at)
```

## Module H — Disputes & Moderation

```
disputes(id PK, milestone_id FK UNIQUE, opened_by_user_id FK,
         reason TEXT, status CHECK(status IN ('open','in_mediation','expert_review','resolved_client','resolved_freelancer','resolved_split','dismissed')),
         resolution_json JSONB, created_at, updated_at)

dispute_evidence(id PK, dispute_id FK, submitted_by_user_id FK,
                 kind CHECK(kind IN ('message','file','pr','ci','other')),
                 ref TEXT, note TEXT, created_at)
```

## Module I — Notifications & Admin

```
notifications(id PK, user_id FK, kind, payload JSONB, read_at, created_at)

audit_log(id PK, actor_user_id FK NULL, action, entity, entity_id, diff JSONB, created_at)

admin_flags(id PK, user_id FK NULL, job_id FK NULL, type, note, created_at)
```

---

## Relationships (high-level)

* `users 1–1 profiles`
* `users 1–n jobs (client)`
* `jobs 1–n proposals`
* `contracts 1–n milestones`
* `milestones 1–1 escrow 1–n payouts 1–n refunds`
* `jobs 1–1 rooms → 1–n messages/files/tasks/calendar_events`
* `jobs 1–n repos → 1–n pull_requests → 1–n ci_checks`
* `users ↔ users reviews (per job/milestone)`
* `milestones 1–1 disputes → 1–n dispute_evidence`

---

## Calculated Metrics (delivery score, etc.)

* **On‑time milestone rate** = `count(m.status IN ('accepted','paid') AND accepted_at <= due_date) / count(m.status IN ('accepted','paid'))`.
* **Delivery score** (store snapshot periodically): combine on‑time rate, rejection rate, dispute occurrence, recent weighting.

---

## Indexing & Constraints (examples)

* `CREATE INDEX ON proposals(job_id, status);`
* `CREATE INDEX ON milestones(contract_id, status, due_date);`
* `CREATE UNIQUE INDEX ON escrow(milestone_id);`
* `CREATE INDEX ON messages(room_id, created_at);`
* `CREATE INDEX ON reviews(to_user_id, created_at);`
* Partial index: `WHERE status='open'` on `jobs`.

---

## Sample DDL (Postgres)

```sql
CREATE TABLE users (
  id           TEXT PRIMARY KEY,
  email        CITEXT UNIQUE NOT NULL,
  password_hash TEXT,
  role         TEXT NOT NULL CHECK (role IN ('client','freelancer','admin')),
  name         TEXT,
  handle       CITEXT UNIQUE,
  country      TEXT, timezone TEXT,
  stripe_account_id TEXT, kyc_status TEXT CHECK (kyc_status IN ('none','pending','verified','rejected')),
  is_active    BOOLEAN DEFAULT TRUE,
  created_at   TIMESTAMPTZ DEFAULT now(),
  updated_at   TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE jobs (
  id TEXT PRIMARY KEY,
  client_id TEXT NOT NULL REFERENCES users(id),
  title TEXT NOT NULL, description TEXT,
  stack TEXT[], budget_type TEXT CHECK (budget_type IN ('fixed','hourly')),
  min_budget_cents BIGINT, max_budget_cents BIGINT, currency TEXT,
  nda_required BOOLEAN DEFAULT FALSE, ip_assignment BOOLEAN DEFAULT TRUE,
  repo_link TEXT,
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('draft','open','in_progress','completed','cancelled')),
  created_at TIMESTAMPTZ DEFAULT now(), updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE proposals (
  id TEXT PRIMARY KEY,
  job_id TEXT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
  freelancer_id TEXT NOT NULL REFERENCES users(id),
  cover TEXT,
  total_cents BIGINT, currency TEXT, delivery_days INT,
  status TEXT NOT NULL DEFAULT 'submitted' CHECK (status IN ('submitted','withdrawn','declined','accepted')),
  created_at TIMESTAMPTZ DEFAULT now(), updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE contracts (
  id TEXT PRIMARY KEY,
  job_id TEXT UNIQUE NOT NULL REFERENCES jobs(id),
  proposal_id TEXT UNIQUE NOT NULL REFERENCES proposals(id),
  terms_json JSONB,
  start_date DATE, end_date DATE,
  payment_model TEXT CHECK (payment_model IN ('fixed','hourly')),
  status TEXT CHECK (status IN ('active','paused','completed','cancelled')),
  created_at TIMESTAMPTZ DEFAULT now(), updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE milestones (
  id TEXT PRIMARY KEY,
  contract_id TEXT NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
  title TEXT NOT NULL, description TEXT,
  amount_cents BIGINT NOT NULL, currency TEXT NOT NULL,
  due_date DATE,
  status TEXT NOT NULL DEFAULT 'funding_required'
    CHECK (status IN ('planned','funding_required','funded','in_progress','submitted','accepted','rejected','disputed','paid')),
  order_index INT,
  dod JSONB,
  submitted_at TIMESTAMPTZ, accepted_at TIMESTAMPTZ, rejected_at TIMESTAMPTZ,
  disputed_at TIMESTAMPTZ, paid_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT now(), updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE escrow (
  id TEXT PRIMARY KEY,
  milestone_id TEXT UNIQUE NOT NULL REFERENCES milestones(id) ON DELETE CASCADE,
  payment_intent_id TEXT UNIQUE NOT NULL,
  amount_cents BIGINT NOT NULL, currency TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'held' CHECK (status IN ('held','released','refunded','disputed')),
  created_at TIMESTAMPTZ DEFAULT now(), updated_at TIMESTAMPTZ DEFAULT now()
);
```

*(More DDL for payouts/refunds/messages/disputes left in this doc to keep things readable.)*

---

## Migration Plan

1. **A+B**: users, profiles, jobs, proposals.
2. **C+D**: contracts, milestones, escrow/payouts/refunds/ledger.
3. **E**: workspace (rooms, messages, files, tasks, calendar).
4. **F+G+H**: integrations, reviews, disputes.
5. **I**: notifications, admin, audit.

## ORM Mapping (Prisma snapshot)

```prisma
model User { id String @id @default(cuid()) email String @unique role Role ... Profiles Profile? Jobs Job[] }
model Job  { id String @id @default(cuid()) client   User @relation(fields:[clientId], references:[id]) clientId String proposals Proposal[] }
model Proposal { id String @id @default(cuid()) job Job @relation(fields:[jobId], references:[id]) jobId String freelancer User @relation(fields:[freelancerId], references:[id]) freelancerId String milestones ProposalMilestone[] }
model Contract { id String @id @default(cuid()) job Job @unique proposal Proposal @unique milestones Milestone[] }
```

---

## What to implement first

* **Minimal path to money:** jobs → proposals → contract → milestone → escrow.
* Add workspace next; then reviews; then disputes.
* Instrument a **ledger** early to track every \$ move.

---

# Microservices Architecture Blueprint (v1)

## Goals

* Independent deployability, clear domain ownership, fault isolation.
* Async-first for business events (escrow, milestone acceptance), sync for read APIs.
* Strong observability + data integrity (idempotency, outbox, sagas).

## Service Boundaries (domains own their data)

1. **Auth & Identity Service**
   Owns users, sessions, roles, JWT issuing, OAuth (GitHub/Google), 2FA.
   DB: `users`, `sessions`, `api_keys`.

2. **Profile & Gigs Service**
   Owns profiles, badges, gigs (service listings), gig media.
   DB: `profiles`, `profile_badges`, `gigs`, `gig_media`, `gig_packages`.

3. **Jobs & Proposals Service**
   Owns jobs, attachments metadata, proposals, invites.
   DB: `jobs`, `job_attachments`, `proposals`, `proposal_milestones`, `invites`.

4. **Contracts & Milestones Service**
   Owns contracts, milestones, status machine (planned→funded→submitted→accepted→paid).
   Publishes events that drive escrow releases.
   DB: `contracts`, `milestones`.

5. **Payments/Escrow Service**
   Integrates Stripe Connect. Holds escrow, creates transfers/refunds, ledger.
   DB: `escrow`, `payouts`, `refunds`, `ledger`, minimal mirror of `milestone_id`.

6. **Workroom (Collab) Service**
   Chat, threads, files metadata, tasks, calendar.
   DB: `rooms`, `messages`, `files`, `tasks`, `calendar_events`.

7. **Code & CI Integration Service**
   GitHub/GitLab apps, PRs, CI checks, preview links.
   DB: `repos`, `pull_requests`, `ci_checks`, `previews`.

8. **Reviews & Reputation Service**
   Ratings per milestone/gig, reputation snapshots, delivery score.
   DB: `reviews`, `reputation_snapshots`.

9. **Disputes & Mediation Service**
   Dispute lifecycle, evidence, outcomes.
   DB: `disputes`, `dispute_evidence`.

10. **Search & Matching Service**
    Full‑text + vector search for jobs, gigs, profiles; ranking pipeline.
    DB: OpenSearch/Elastic + pgvector store; caches only—sources of truth remain in domain DBs.

11. **Notifications Service**
    Multi‑channel (email, push, in‑app), digest, templates.
    DB: `notifications`, `email_queue`, `push_tokens`.

12. **Admin/Backoffice Service**
    KYC review, payouts console, moderation, audit log viewer.
    DB: `audit_log`, `admin_flags`.

13. **AI Services (optional)**
    Scope assistant, proposal helper, chat summaries, embeddings.
    DB: `ai_events`, `embeddings`, `ai_suggestions`.

> Each service has **its own database** (Postgres per service). Never share tables across services. Cross‑service data moves via events or read models.

---

## API Layering

* **API Gateway** (Kong/Traefik/Nginx) terminates TLS, rate limits, authn via JWT.
* **BFFs (Backend‑for‑Frontend)**:

  * `bff-web`: aggregates calls for the web app (compose data from multiple services).
  * `bff-admin`: admin UI specific.
* **Protocols**:

  * **Public**: REST/JSON over HTTPS.
  * **Internal**: gRPC for service‑to‑service *commands/queries*.
  * **Events**: Kafka/Redpanda (or NATS/Redis Streams) for async pub/sub.

---

## Eventing & Sagas (orchestration)

Use **outbox pattern** in each service: DB row → transactionally produce event → broker. Consumers are idempotent.

### Key Domain Events (examples)

* `JobPosted`, `ProposalSubmitted`, `ProposalAccepted`
* `ContractCreated`, `MilestonePlanned`, `MilestoneFunded`, `MilestoneSubmitted`, `MilestoneAccepted`, `MilestoneRejected`, `MilestoneDisputed`
* `EscrowFunded`, `EscrowReleased`, `RefundCompleted`, `PayoutCompleted`
* `ReviewGiven`, `DisputeOpened`, `DisputeResolved`

### Saga 1: **Fund Milestone**

1. **Contracts** emits `MilestoneFundingRequested`.
2. **Payments** reserves/charges via Stripe → writes `escrow(held)` → emits `EscrowFunded{milestoneId}`.
3. **Contracts** marks milestone `funded`.
4. **Notifications** informs freelancer.

### Saga 2: **Accept Milestone → Release Escrow**

1. **Contracts** transitions `submitted` → `accepted` → emits `MilestoneAccepted`.
2. **Payments** on event creates **Transfer** to freelancer → `EscrowReleased` + ledger rows.
3. **Reviews** unlocks review form; **Reputation** updates delivery metrics.
4. **Notifications** sends receipt to both parties.

### Saga 3: **Dispute**

1. **Contracts** or **Disputes** emits `MilestoneDisputed` → **Payments** locks escrow (`status=disputed`).
2. After resolution, **Disputes** emits `DisputeResolved{winner, split}` → **Payments** executes transfer/refund accordingly.

---

## Data Ownership & Read Models

* A service may maintain **read models** (denormalized copies) for UI performance, populated via events (e.g., **Search** keeps an index of jobs/gigs/profiles).
* Use **Change Data Capture** or explicit domain events; avoid reaching into others’ DBs.

---

## Security

* **Auth**: OAuth2/OpenID + short‑lived JWT access tokens; refresh tokens; mTLS between services (optional).
* **AuthZ**: Role (client/freelancer/admin) + resource‑level checks (e.g., only contract participants can access a room).
* **PII**: isolate in Auth/Profile; tokenize where possible; encrypt at rest (pgcrypto/KMS).
* **File scanning**: AV scan on uploads; signed URLs.

---

## Observability

* **OpenTelemetry** across services (traces, metrics, logs).
* Collect with **OTel Collector** → **Jaeger/Tempo** (traces), **Prometheus** (metrics), **Loki** (logs), **Grafana** dashboards.
* Correlate logs with `trace_id`.

---

## Deployment & Infra

* **Kubernetes** (GKE/EKS) or Fly.io for simpler scale.
* **Namespace per env** (dev/staging/prod).
* **Stateful**: Postgres (one per service), Redis for cache/queues (optional), object storage (S3/R2).
* **Secrets** via Vault/SSM/SealedSecrets.
* **CI/CD**: GitHub Actions → build, test, scan, push images, run migrations, deploy via ArgoCD/Flux.

---

## Local Dev

* `docker-compose` with:

  * `gateway`, `bff-web`, each service, Postgres instances, Kafka/Redpanda, Redis, Mailhog, MinIO (S3 emu).
* Seed scripts for demo data.
* Makefiles/NPM scripts to run specific services fast.

---

## Tech Choices (suggested)

* **Services**: NestJS (TypeScript) with Fastify adapter; Prisma or TypeORM.
* **Internal RPC**: gRPC (NestJS microservices).
* **Events**: Kafka/Redpanda + `node-rdkafka`/`kafkajs`.
* **Search**: OpenSearch; embeddings with pgvector.
* **Payments**: Stripe Connect SDK.
* **Realtime**: Socket.IO for Workroom; or WebSocket gateway.

---

## Example APIs

### Contracts & Milestones (REST)

```
POST /contracts { jobId, proposalId }
POST /contracts/{id}/milestones { title, amountCents, dueDate, dod }
POST /milestones/{id}/submit
POST /milestones/{id}/accept
POST /milestones/{id}/reject
```

### Payments/Escrow (REST)

```
POST /escrow/fund { milestoneId, amountCents, currency }  // returns client_secret for Stripe
POST /escrow/release { milestoneId }
POST /escrow/refund { milestoneId, amountCents }
POST /webhooks/stripe
```

### Workroom (REST+WS)

```
GET  /rooms/{contractId}
WS   /rooms/{contractId}  // messages, typing, read receipts
POST /rooms/{contractId}/messages
POST /rooms/{contractId}/files  // returns signed URL
```

---

## Idempotency & Reliability

* All mutating endpoints accept **Idempotency‑Key**.
* Use **Outbox** per service (table + background publisher).
* Consumers are **at‑least‑once**; handlers must be idempotent (check `event_id`).
* Compensating transactions for sagas (e.g., reverse transfer on failed acceptance).

---

## Testing Strategy

* **Unit**: business rules per service.
* **Contract tests** (Pact) between services & BFFs.
* **Integration**: spin up service + DB in CI (Testcontainers).
* **E2E**: happy paths (post job → hire → fund → submit → accept → payout), disputes, partial refunds.
* **Chaos**: inject network failures to validate saga resilience.

---

## Incremental Rollout (minimal viable mesh)

1. **Monolith core** or **2–3 services**: Jobs/Proposals + Contracts/Milestones + Payments.
2. Add **Workroom** and **Notifications**.
3. Split **Profiles/Gigs** and **Reviews**.
4. Add **Disputes**, **Search/Matching**, **CI Integrations**.
5. Introduce **AI services** and advanced analytics.

---

## Reference Repo Layout

```
/apps
  /gateway
  /bff-web
  /svc-auth
  /svc-profiles-gigs
  /svc-jobs-proposals
  /svc-contracts
  /svc-payments
  /svc-workroom
  /svc-reviews
  /svc-disputes
  /svc-search
  /svc-notifications
  /svc-ci
  /svc-ai
/packages
  /shared-types (OpenAPI/gRPC proto schemas)
  /shared-lib   (logging, tracing, config)
  /shared-test  (test utils)
/infra
  /k8s (Helm charts, Kustomize)
  /compose (docker-compose.yml)
```

---

## Security & Compliance Notes

* Stripe Connect for payouts; do not store raw card data.
* GDPR tools: export/delete my data per user; data lineage via audit logs.
* Rate limiting & WAF at gateway; bot protection for auth & payments.
* Secret rotation, least privilege IAM for S3/DB.

---

## What you can build first

* Stand up **Auth**, **Jobs/Proposals**, **Contracts**, **Payments**.
* Wire the **Fund→Submit→Accept→Payout** saga via Kafka + outbox.
* Add **Workroom** (WS chat + file uploads).
* Layer **Notifications**.
* Only then split into more services as traffic grows.
