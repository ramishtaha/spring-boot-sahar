# Sahar — a Spring Boot codealong that builds your real routine app

**Sahar** (Arabic: the pre-dawn hours before Fajr — the time the day is won) is a guided,
step-by-step course **and** a real personal app at the same time. By the end you will have a working
Spring Boot 4 application that serves and lets you **edit** your personal training / prayer / nutrition
routine, persisted in a database and runnable in Docker — and along the way you will have learned Spring
Boot, JDBC, validation, PostgreSQL, Flyway, Docker, and a taste of CI/CD, every concept taught *why
before how*. Every step ships a full, **runnable** checkpoint, so you can run, read, and `diff` any point
in the build.

- **Stack:** Spring Boot **4.0.6**, Java **25** (current LTS; Java 17 is the minimum), Maven, IntelliJ IDEA Community.
- **Heads-up:** tutorials written before 2026 target Spring Boot **3.x** and differ mainly in *dependency names* (e.g. `spring-boot-starter-web` → `spring-boot-starter-webmvc`). The docs call every such difference out — see [docs/00-setup.md](docs/00-setup.md) and the [Maven cheatsheet](reference/cheatsheet-maven.md).
- **The app's domain** is the real "Sahar" routine: a Sunnah + 5-prayer baseline, an MMA training block that waves Foundation → Build → Peak → Deload, supplements, nutrition, and bullet-journal prompts — seeded so the app boots with real content and lets you edit the parts that change every month.

---

## How this codealong works

Each step is a small, self-contained increment:

- **A doc** in [`docs/steps/`](docs/steps/) teaches the step: the *why*, the theory (with diagrams), the inline code to add, the end state, common mistakes, and questions to check yourself.
- **A starter** = the *previous* step's checkpoint.
- **An end** = *this* step's checkpoint, under [`checkpoints/`](checkpoints/). So `checkpoints/step-07` is exactly `checkpoints/step-06` **plus** step 7's changes — you can `diff` any two steps.
- **[`app/`](app/)** holds the finished, complete application — your main line and the thing to run when you just want to use Sahar.

The recommended loop: read the step doc → build the change yourself in `app/` (or alongside) → compare against that step's checkpoint → tick it off in [`progress.md`](progress.md) → jot anything confusing in [`questions.md`](questions.md) to bring to your mentor. You learn by typing it, not by copying — the checkpoints are there to unstick you and to diff against, not to skip the thinking.

> **Browsing checkpoints safely:** to explore a step, *open or copy* its `checkpoints/step-NN-…` folder and run it there. Keep `app/` as your single working line so you never lose your place. Each checkpoint is a complete Maven project — run it with `./mvnw spring-boot:run` (macOS/Linux) or `.\mvnw.cmd spring-boot:run` (Windows), or open the folder in IntelliJ. Because the checkpoints are sibling folders, the cleanest way to see exactly what a step changed is a folder diff:
> ```bash
> git diff --no-index checkpoints/step-05-validation-and-rules checkpoints/step-06-jdbctemplate-h2
> ```
> (or point any visual diff tool at the two folders).

---

## Full table of contents

### Start here
- [docs/00-setup.md](docs/00-setup.md) — install & verify the toolchain, version notes, how to use the checkpoints safely.
- [PLAN.md](PLAN.md) — the multi-session plan mapped to the steps and a time budget.
- [progress.md](progress.md) — your checklist, one box per step.
- [questions.md](questions.md) — where to log questions for your mentor.
- [docs/diet-plan.md](docs/diet-plan.md) — a suggested weekly meal plan derived from the domain (shown on the home page's *This week* board).

### The steps (build the app while learning)
| # | Step | Doc | Checkpoint |
|---|------|-----|-----------|
| 00 | Baseline: generate & dissect the project | [doc](docs/steps/00-baseline.md) | [step-00](checkpoints/step-00-baseline/) |
| 01 | Serve the static Sahar site | [doc](docs/steps/01-serve-static.md) | [step-01](checkpoints/step-01-serve-static/) |
| 02 | The first REST endpoint (static → dynamic) | [doc](docs/steps/02-first-rest-endpoint.md) | [step-02](checkpoints/step-02-first-rest-endpoint/) |
| 03 | Model the domain with records | [doc](docs/steps/03-model-the-domain.md) | [step-03](checkpoints/step-03-model-the-domain/) |
| 04 | Editing in memory: the service layer | [doc](docs/steps/04-in-memory-edit.md) | [step-04](checkpoints/step-04-in-memory-edit/) |
| 05 | Validation & business rules | [doc](docs/steps/05-validation-and-rules.md) | [step-05](checkpoints/step-05-validation-and-rules/) |
| 06 | Persistence with JdbcTemplate & H2 | [doc](docs/steps/06-jdbctemplate-h2.md) | [step-06](checkpoints/step-06-jdbctemplate-h2/) |
| 07 | Full CRUD for the editable parts | [doc](docs/steps/07-full-crud.md) | [step-07](checkpoints/step-07-full-crud/) |
| 08 | The editable admin UI (core goal) | [doc](docs/steps/08-editable-admin-ui.md) | [step-08](checkpoints/step-08-editable-admin-ui/) |
| 09 | Swap to PostgreSQL with profiles | [doc](docs/steps/09-swap-to-postgres.md) | [step-09](checkpoints/step-09-swap-to-postgres/) |
| 10 | Seeding & Flyway migrations | [doc](docs/steps/10-seed-and-migrations.md) | [step-10](checkpoints/step-10-seed-and-migrations/) |
| 11 | Dockerize with a multi-stage build | [doc](docs/steps/11-dockerize.md) | [step-11](checkpoints/step-11-dockerize/) |
| 12 | docker compose: the whole stack | [doc](docs/steps/12-compose.md) | [step-12](checkpoints/step-12-compose/) |
| 13 | A taste of DevOps: GitHub Actions CI | [doc](docs/steps/13-ci-with-github-actions.md) | [step-13](checkpoints/step-13-ci-with-github-actions/) |
| 14 | Deploy (optional) & orchestration overview | [doc](docs/steps/14-deploy.md) | [step-14](checkpoints/step-14-deploy/) |
| 99 | Roadmap: where to go next | [doc](docs/steps/99-roadmap.md) | — |

### Beyond the core course
These extend the finished app past the 14-step course. Steps 00–14 stay frozen; **15 & 16 add their own runnable checkpoints** and `app/` equals step 16.
- **15 — Location-aware prayer times** ([doc](docs/steps/15-geolocation-prayer-times.md) · [checkpoint](checkpoints/step-15-geolocation-prayer-times/)) — a server-side prayer-time calculator (Karachi 18°, Hanafi Asr), OpenStreetMap geocoding via `RestClient`, a persisted location, and a unit test.
- **16 — Location picker, UI polish & PWA** ([doc](docs/steps/16-ui-and-pwa.md) · [checkpoint](checkpoints/step-16-ui-and-pwa/)) — light/dark theming, the *This week* board, a live prayer countdown, a **user-controlled location picker** (device / search / map pin), an editable meal plan, and an installable offline PWA.
- **17 — Visual polish** ([doc](docs/steps/17-visual-polish.md) · [checkpoint](checkpoints/step-17-visual-polish/)) — a design-system pass (tokens, typography, depth, motion) that makes it genuinely good-looking, CSS-only. `app/` equals step 17.
- [Suggested diet plan](docs/diet-plan.md) — the generated weekly meal plan.

### Theory deep-dives ([docs/theory/](docs/theory/))
- [Spring, servlets, and dependency injection](docs/theory/spring-and-di.md)
- [HTTP and REST](docs/theory/http-and-rest.md)
- [The persistence landscape](docs/theory/persistence-landscape.md) — the full comparison
- [JdbcTemplate vs Spring Data JPA](docs/theory/jdbc-vs-jpa.md)
- [Containers and DevOps](docs/theory/containers-and-devops.md)

### Reference ([reference/](reference/))
- [Glossary](reference/glossary.md)
- [Modern Java refresher](reference/java-refresher.md)
- [Maven cheatsheet](reference/cheatsheet-maven.md)
- [Spring annotations cheatsheet](reference/cheatsheet-spring-annotations.md)
- [HTTP & REST cheatsheet](reference/cheatsheet-http-rest.md)
- [SQL & JdbcTemplate cheatsheet](reference/cheatsheet-sql-jdbc.md)
- [Docker & Podman cheatsheet](reference/cheatsheet-docker-podman.md)

---

## Progress overview

Every step below has a finished, **runnable** checkpoint and a full teaching doc; track your own progress in [progress.md](progress.md).

```
Foundations   00 ▸ 01 ▸ 02 ▸ 03        (project, static site, REST, domain model)
Editing       04 ▸ 05                  (service layer, validation & rules)
Persistence   06 ▸ 07 ▸ 08             (JdbcTemplate+H2, CRUD, editable admin UI)  ← core goal at 08
Production    09 ▸ 10 ▸ 11 ▸ 12        (Postgres, Flyway, Docker, compose)
DevOps        13 ▸ 14 ▸ 99             (CI, deploy, roadmap)
```

---

## Running it

### The finished app — zero setup (embedded H2)
```bash
cd app
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run
# open http://localhost:8080      (read-only view)
# open http://localhost:8080/admin.html   (edit your routine)
```
Data persists in a local file (`app/data/sahar.mv.db`). The H2 console is at `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/sahar`, user `sa`, no password).

### The whole stack with PostgreSQL (Docker)
```bash
cd app
docker compose up --build       # app + Postgres, data in a named volume
# open http://localhost:8080
docker compose down             # stop (keeps your data)
docker compose down -v          # stop and wipe the database volume
```

### Any checkpoint
```bash
cd checkpoints/step-06-jdbctemplate-h2
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run
```

---

## Put it on GitHub

This folder is already a git repository with an initial commit. To publish it:

```bash
# Option A — GitHub CLI (creates the repo and pushes in one go)
gh repo create spring-boot-sahar --public --source=. --remote=origin --push

# Option B — manually (after creating an empty repo on github.com)
git remote add origin https://github.com/<your-username>/spring-boot-sahar.git
git branch -M main
git push -u origin main
```

Once pushed, GitHub Actions runs the workflow in [.github/workflows/ci.yml](.github/workflows/ci.yml) on every push — it builds and tests `app/` and builds the Docker image. A green check on your commit means everything still works.

---

*Recover, build, fight.*
