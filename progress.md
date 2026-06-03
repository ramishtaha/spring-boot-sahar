# Progress

Tick a step when you can **explain** it, not just when it compiles. For each step the loop is:
read the doc → build it in `app/` (or alongside) → run it → diff against the checkpoint.

Legend: `[ ]` not started · `[~]` in progress · `[x]` done.

## Setup
- [ ] [00-setup](docs/00-setup.md) — toolchain installed and verified (`java -version` shows 25, `mvn -version`, Docker up)

## Week 1 — Foundation
- [ ] **00 Baseline** — [doc](docs/steps/00-baseline.md) · [checkpoint](checkpoints/step-00-baseline/)
  - [ ] read the doc  - [ ] built it  - [ ] ran on :8080  - [ ] diffed vs checkpoint
- [ ] **01 Serve static** — [doc](docs/steps/01-serve-static.md) · [checkpoint](checkpoints/step-01-serve-static/)
  - [ ] read  - [ ] built  - [ ] site renders  - [ ] diffed
- [ ] **02 First REST endpoint** — [doc](docs/steps/02-first-rest-endpoint.md) · [checkpoint](checkpoints/step-02-first-rest-endpoint/)
  - [ ] read  - [ ] built  - [ ] `GET /api/config` works  - [ ] diffed
- [ ] **03 Model the domain** — [doc](docs/steps/03-model-the-domain.md) · [checkpoint](checkpoints/step-03-model-the-domain/)
  - [ ] read  - [ ] built  - [ ] typed config returns  - [ ] diffed
- [ ] **04 In-memory edit** — [doc](docs/steps/04-in-memory-edit.md) · [checkpoint](checkpoints/step-04-in-memory-edit/)
  - [ ] read  - [ ] built  - [ ] PUTs work (and reset on restart)  - [ ] diffed
- [ ] **05 Validation & rules** — [doc](docs/steps/05-validation-and-rules.md) · [checkpoint](checkpoints/step-05-validation-and-rules/)
  - [ ] read  - [ ] built  - [ ] bad input → 400  - [ ] diffed

## Week 2 — Build
- [ ] **06 JdbcTemplate & H2** — [doc](docs/steps/06-jdbctemplate-h2.md) · [checkpoint](checkpoints/step-06-jdbctemplate-h2/)
  - [ ] read  - [ ] built  - [ ] edit → restart → persists  - [ ] diffed
- [ ] **07 Full CRUD** — [doc](docs/steps/07-full-crud.md) · [checkpoint](checkpoints/step-07-full-crud/)
  - [ ] read  - [ ] built  - [ ] schedule + block CRUD works  - [ ] diffed
- [ ] **08 Editable admin UI** — [doc](docs/steps/08-editable-admin-ui.md) · [checkpoint](checkpoints/step-08-editable-admin-ui/)  ← **core goal**
  - [ ] read  - [ ] built  - [ ] edit in browser → saves → shows on a 2nd device  - [ ] diffed

## Week 3 — Peak
- [ ] **09 Swap to PostgreSQL** — [doc](docs/steps/09-swap-to-postgres.md) · [checkpoint](checkpoints/step-09-swap-to-postgres/)
  - [ ] read  - [ ] built  - [ ] same flow on Postgres  - [ ] diffed
- [ ] **10 Seed & Flyway** — [doc](docs/steps/10-seed-and-migrations.md) · [checkpoint](checkpoints/step-10-seed-and-migrations/)
  - [ ] read  - [ ] built  - [ ] fresh DB pre-filled  - [ ] diffed
- [ ] **11 Dockerize** — [doc](docs/steps/11-dockerize.md) · [checkpoint](checkpoints/step-11-dockerize/)
  - [ ] read  - [ ] built the image  - [ ] ran the container  - [ ] diffed
- [ ] **12 docker compose** — [doc](docs/steps/12-compose.md) · [checkpoint](checkpoints/step-12-compose/)
  - [ ] read  - [ ] `compose up` runs the stack  - [ ] data persists in the volume  - [ ] diffed
- [ ] **13 GitHub Actions CI** — [doc](docs/steps/13-ci-with-github-actions.md) · [checkpoint](checkpoints/step-13-ci-with-github-actions/)
  - [ ] read  - [ ] pushed  - [ ] CI is green  - [ ] diffed

## Week 4 — Deload
- [ ] **14 Deploy & orchestration overview** — [doc](docs/steps/14-deploy.md) · [checkpoint](checkpoints/step-14-deploy/)
  - [ ] read  - [ ] deployed a public URL **or** finished a clean local stack  - [ ] understand the K8s mental model
- [ ] **99 Roadmap** — [doc](docs/steps/99-roadmap.md) — picked my next topic: ______________________

## Beyond the core course
- [ ] **15 Location-aware prayer times** — [doc](docs/steps/15-geolocation-prayer-times.md) · [checkpoint](checkpoints/step-15-geolocation-prayer-times/) — prayer-time calculator + geocoding + persisted location (API)
  - [ ] read  - [ ] calculate from coords  - [ ] geocode search/reverse  - [ ] location persists
- [ ] **16 Location picker, UI polish & PWA** — [doc](docs/steps/16-ui-and-pwa.md) · [checkpoint](checkpoints/step-16-ui-and-pwa/) — theming, board, countdown, location picker (device/search/map), editable meals, installable offline
  - [ ] read  - [ ] set location by search/map/device  - [ ] light/dark  - [ ] installed as PWA

## Done?
- [ ] The app serves and **edits** my routine, persisted in a database, runnable in Docker.
- [ ] I can explain DI, the request lifecycle, the repository layer, migrations, and a multi-stage build.
- [ ] I pushed it to GitHub with green CI.
