# PLAN — the multi-session schedule

This maps the 15 build steps onto the **June 2026 training block** that Sahar itself is seeded with, because
the block's *backend focus* per week was designed to be this course:

- **Week 1 — Foundation (8–14 Jun):** Spring Boot core — setup, dependency injection, controllers, a CRUD REST API.
- **Week 2 — Build (15–21 Jun):** Persistence — repositories, a real database, validation.
- **Week 3 — Peak (22–28 Jun):** Docker & DevOps — Dockerfile, compose, env config, basic CI.
- **Week 4 — Deload (29 Jun–5 Jul):** Ship it — deploy the container, refactor, write docs.

Your main study windows (from the routine): the **05:00 deep-work sprint (~120 min)** on weekdays,
**Saturday** (applied only — build/debug/ship, no new theory), and **Sunday** (4–5 h of real focus, then build).
Each step below is sized to fit one or two morning sprints. Don't rush; a step you can *explain* beats two you copied.

> [!TIP]
> Tick boxes in [progress.md](progress.md) as you go, and drop anything fuzzy into [questions.md](questions.md).

---

## 🌱 Week 0 — Foundations (before you start)

New to Java, the terminal, or Spring Boot? Spend about an hour on the [Foundations primers](docs/foundations/) — ten short reads that make everything below click. Already comfortable? Skip straight to Week 1.

| Sitting | Read | Outcome |
|--------|------|---------|
| ~30 min | [Web app](docs/foundations/how-a-web-app-works.md) · [Command line](docs/foundations/the-command-line.md) · [Java 1](docs/foundations/java-1-hello-world.md)–[2](docs/foundations/java-2-types-and-logic.md)–[3](docs/foundations/java-3-classes-and-objects.md) | You can read Java and run a command. |
| ~30 min | [Maven](docs/foundations/what-is-maven.md) · [Spring Boot](docs/foundations/what-is-spring-boot.md) · [HTTP](docs/foundations/http-basics.md) · [Databases](docs/foundations/databases-basics.md) · [Git](docs/foundations/git-basics.md) | You know what every tool in the stack is for. |

Then install your toolchain ([00-setup](docs/00-setup.md)) and start Week 1.

---

## 🏗️ Week 1 — Foundation · "Spring Boot core" → steps 00–05

| Session | Focus | Step(s) | Outcome |
|--------|-------|---------|---------|
| Mon 05:00 | Project & tooling | [00 Baseline](docs/steps/00-baseline.md) | Empty app boots on :8080; you can read `pom.xml` and `@SpringBootApplication`. |
| Tue 05:00 | Serving the front end | [01 Serve static](docs/steps/01-serve-static.md) | The Sahar site renders through Spring Boot. |
| Wed 05:00 | First API | [02 First REST endpoint](docs/steps/02-first-rest-endpoint.md) | `GET /api/config` drives the page; static → dynamic pivot. |
| Thu 05:00 | Domain modelling | [03 Model the domain](docs/steps/03-model-the-domain.md) | Typed records replace the untyped Map; full seed data. |
| Fri / Sat | Editing & rules | [04 In-memory edit](docs/steps/04-in-memory-edit.md), [05 Validation](docs/steps/05-validation-and-rules.md) | Service layer + PUTs; invalid edits rejected with clean 400s. |
| Sun | Catch up & review | — | Re-read [Spring & DI](docs/theory/spring-and-di.md) and [HTTP & REST](docs/theory/http-and-rest.md); diff your code vs the checkpoints. |

**Read alongside:** [Spring, servlets & DI](docs/theory/spring-and-di.md), [HTTP & REST](docs/theory/http-and-rest.md), the [Java refresher](reference/java-refresher.md), and the [Spring annotations cheatsheet](reference/cheatsheet-spring-annotations.md).

## 🗄️ Week 2 — Build · "Persistence" → steps 06–08 (+ start 09)

| Session | Focus | Step(s) | Outcome |
|--------|-------|---------|---------|
| Mon–Tue 05:00 | The data layer | [06 JdbcTemplate & H2](docs/steps/06-jdbctemplate-h2.md) | Edit a prayer time, restart, it persists. |
| Wed–Thu 05:00 | CRUD | [07 Full CRUD](docs/steps/07-full-crud.md) | Every monthly-editable field has working create/read/update/delete. |
| Fri / Sat | The editor | [08 Editable admin UI](docs/steps/08-editable-admin-ui.md) | **Core goal:** change a value in the browser → it saves → shows on a second device. |
| Sun | Reading + start Postgres | [09 Postgres](docs/steps/09-swap-to-postgres.md) | Skim profiles & 12-factor; spin up Postgres in Docker. |

**Read alongside:** [The persistence landscape](docs/theory/persistence-landscape.md), [JdbcTemplate vs JPA](docs/theory/jdbc-vs-jpa.md), the [SQL & JdbcTemplate cheatsheet](reference/cheatsheet-sql-jdbc.md).

## 🐳 Week 3 — Peak · "Docker & DevOps" → steps 09–13

| Session | Focus | Step(s) | Outcome |
|--------|-------|---------|---------|
| Mon 05:00 | Finish Postgres | [09 Postgres](docs/steps/09-swap-to-postgres.md) | Identical behaviour on a real server DB. |
| Tue 05:00 | Migrations | [10 Seed & Flyway](docs/steps/10-seed-and-migrations.md) | A fresh DB comes up pre-filled with your routine. |
| Wed 05:00 | Containerize | [11 Dockerize](docs/steps/11-dockerize.md) | The app runs as a container. |
| Thu 05:00 | The stack | [12 docker compose](docs/steps/12-compose.md) | `docker compose up` runs app + Postgres with a persistent volume. |
| Fri / Sat | Automation | [13 GitHub Actions CI](docs/steps/13-ci-with-github-actions.md) | Green CI on every push. |
| Sun | Reading + build | [Containers & DevOps](docs/theory/containers-and-devops.md) | Consolidate the mental model. |

**Read alongside:** [Containers & DevOps](docs/theory/containers-and-devops.md), the [Docker & Podman cheatsheet](reference/cheatsheet-docker-podman.md).

## 🚀 Week 4 — Deload · "Ship it" → step 14 (+ refactor, docs, roadmap)

| Session | Focus | Step(s) | Outcome |
|--------|-------|---------|---------|
| Mon 05:00 | Deploy or finish local | [14 Deploy & K8s overview](docs/steps/14-deploy.md) | A live URL, or a clean local `docker compose` stack. |
| Tue–Thu (lighter) | Refactor & document | — | Tidy names, comments, your own README notes. Deload week = lower volume, more sleep. |
| Sun | Plan the next block | [99 Roadmap](docs/steps/99-roadmap.md) | Pick your next topic: JPA, testing, Spring Security, K8s, observability. |

**Read alongside:** [99 Roadmap](docs/steps/99-roadmap.md).

## 🧭 Beyond the course (extra blocks)

Once the 14-step course is done, these extend the real app — each with its own runnable checkpoint, and
`app/` equals the last one:

| Block | Focus | Step | Checkpoint |
|-------|-------|------|-----------|
| Location | Compute prayer times for any place; geocoding + a persisted, user-set location | [15](docs/steps/15-geolocation-prayer-times.md) | [step-15](checkpoints/step-15-geolocation-prayer-times/) |
| Location UI + PWA | The picker (device / search / map), theming, the weekly board, installable offline | [16](docs/steps/16-ui-and-pwa.md) | [step-16](checkpoints/step-16-ui-and-pwa/) |
| Visual polish | A tokens-first design system (CSS only) | [17](docs/steps/17-visual-polish.md) | [step-17](checkpoints/step-17-visual-polish/) |

---

## ⏱️ If you only have a weekend

Do **00 → 02** Saturday morning (you'll have a server-driven page), then **03 → 06** across Saturday
afternoon and Sunday (typed model, editing, validation, and persistence). You'll have a real, saving app
by Sunday night; pick up Docker the following weekend.
