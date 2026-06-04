# IntelliJ IDEA for Sahar — Ultimate (and what's now free)

_How to use IntelliJ IDEA's pro features on this exact project: a real Spring Boot 4 app with JDBC,
PostgreSQL, Flyway, a REST API, and Docker. You have **Ultimate**, so this shows what it buys you — and
flags the bits that became free, so you know what's what._

> [!NOTE]
> **One IntelliJ IDEA (since 2025.3, Dec 2025).** JetBrains merged Community and Ultimate into a single
> product: a **free tier** plus an **Ultimate subscription** that unlocks advanced tooling. Crucially for
> this course, the **Database tools, schema viewer, and full SQL language support are now free**, while the
> **advanced Spring tooling, the HTTP Client, and Docker/Kubernetes integration stay Ultimate**. Sources:
> [Unified release announcement](https://blog.jetbrains.com/idea/2025/12/intellij-idea-unified-release/) ·
> [editions comparison](https://www.jetbrains.com/products/compare/?product=idea&product=idea-ce).

## 🗺️ What helps, and where

| Feature | Tier | Best for these steps |
|---|:---:|---|
| **Database** tool window (connect, browse, run SQL) | **Free** | [06](../docs/steps/06-jdbctemplate-h2.md) · [09](../docs/steps/09-swap-to-postgres.md) · [10](../docs/steps/10-seed-and-migrations.md) |
| **SQL language support** (completion/validation in `.sql` + injected SQL) | **Free** | [06](../docs/steps/06-jdbctemplate-h2.md) · [07](../docs/steps/07-full-crud.md) · [10](../docs/steps/10-seed-and-migrations.md) |
| Spring Boot **project wizard** + basic Spring/Thymeleaf highlighting | **Free** | [00](../docs/steps/00-baseline.md) |
| **Spring** tooling: Beans diagram, **Endpoints** window, `application.properties` completion, autowiring navigation | **Ultimate** | [00](../docs/steps/00-baseline.md) → [08](../docs/steps/08-editable-admin-ui.md) |
| **HTTP Client** (`.http` files) | **Ultimate** | [02](../docs/steps/02-first-rest-endpoint.md) · [04](../docs/steps/04-in-memory-edit.md) · [05](../docs/steps/05-validation-and-rules.md) · [07](../docs/steps/07-full-crud.md) · [15](../docs/steps/15-geolocation-prayer-times.md) |
| **Docker / Compose / Kubernetes** integration | **Ultimate** | [11](../docs/steps/11-dockerize.md) · [12](../docs/steps/12-compose.md) · [14](../docs/steps/14-deploy.md) |
| Profiler, JPA/Hibernate console, dev containers | **Ultimate** | the [roadmap](../docs/steps/99-roadmap.md) (JPA, observability) |

## 🚀 One-time setup for this repo

1. **Open the project.** `File ▸ Open` → pick the `app/` folder (or any `checkpoints/step-NN`). IntelliJ
   detects the Maven `pom.xml` and imports it. Set the SDK to **Java 25** (`File ▸ Project Structure ▸ SDK`).
2. **Run config.** A green ▶ appears next to `SaharApplication.main`. Run it once; IntelliJ saves a
   **Spring Boot** run configuration. (Ultimate marks it with the Spring icon and adds the live dashboard.)
3. **Connect the database** (free) — see below.
4. **Open `app/requests.http`** (Ultimate HTTP Client) to call the API.

---

## 🌱 Spring tooling (Ultimate)

This course spends steps 00–08 on beans, dependency injection, controllers, and configuration — exactly
what Ultimate's Spring support visualises.

- **Beans / dependency diagram.** Right-click the project ▸ *Diagrams ▸ Show Spring Beans*, or open the
  **Spring** tool window. You can *see* the application context: `ConfigController` → depends on →
  `RoutineService` → depends on → the repositories. This is the picture [step 04](../docs/steps/04-in-memory-edit.md)
  and [`spring-and-di.md`](../docs/theory/spring-and-di.md) describe, drawn from your real code.
- **Autowiring navigation.** A little bean gutter icon sits next to each injected dependency
  (e.g. the `RoutineService` constructor param); click it to jump to the bean definition. Great for
  following the wiring from [step 06](../docs/steps/06-jdbctemplate-h2.md) onward.
- **`application.properties` completion + validation.** Ultimate knows the Spring keys: type
  `spring.datasource.` and it completes `url`, `username`, …, shows docs, and flags typos/deprecations.
  Invaluable for [step 06](../docs/steps/06-jdbctemplate-h2.md), [09](../docs/steps/09-swap-to-postgres.md),
  and [10](../docs/steps/10-seed-and-migrations.md).
- **Endpoints tool window.** `View ▸ Tool Windows ▸ Endpoints` lists every mapping (`GET /api/config`,
  `PUT /api/prayer-times`, …) discovered from your `@GetMapping`/`@PutMapping` annotations, and lets you
  call them. A live index of the API you build across [steps 02–08](../docs/steps/02-first-rest-endpoint.md).

## 🌐 The HTTP Client (Ultimate) — replace curl

Ultimate ships an **HTTP Client**: plain-text `.http` files you run with a ▶ in the gutter, with response
history, variables, and assertions. This repo includes one:

- **[`app/requests.http`](../app/requests.http)** — every Sahar endpoint, ready to run against a local app.

```http
@host = http://localhost:8080

### Calculate prayer times for a location (no save)
GET {{host}}/api/prayer-times/calculate?lat=19.22&lng=72.98&tz=330

### Replace prayer times
PUT {{host}}/api/prayer-times
Content-Type: application/json

{ "fajr": "04:37", "sunrise": "05:59", "dhuhr": "12:37", "asr": "17:12", "maghrib": "19:13", "isha": "20:36" }
```

Open it, start the app, and click ▶ next to any request — much nicer than the `curl` examples in the step
docs (which still work everywhere). Use it for [02](../docs/steps/02-first-rest-endpoint.md),
[04](../docs/steps/04-in-memory-edit.md), [05](../docs/steps/05-validation-and-rules.md) (watch the 400s),
[07](../docs/steps/07-full-crud.md), and [15](../docs/steps/15-geolocation-prayer-times.md).

> [!TIP]
> The `.http` file doubles as living API documentation — even on the free tier you can read it to see
> every route, method, and example body at a glance.

## 🗄️ Database tools + SQL (now free)

The persistence steps are where this shines, and it's now **free**.

**Connect to the H2 file (steps 06–08):** run the app once so `app/data/sahar.mv.db` exists, then in the
**Database** tool window (`View ▸ Tool Windows ▸ Database`) ▸ `+` ▸ *Data Source ▸ H2*:

- URL: `jdbc:h2:file:./data/sahar` (relative to where you run the app), user `sa`, no password.
- Use the **file** (embedded) driver, and connect while the app is **stopped** (embedded H2 allows one
  writer) — or keep `AUTO_SERVER=TRUE` (already in our URL) to attach while it runs.

**Connect to the compose Postgres (steps 09–12):** start `docker compose up`, then add a *PostgreSQL* data
source: `jdbc:postgresql://localhost:5432/sahar`, user `sahar`, password `sahar`. Browse the same tables you
seeded — `prayer_times`, `weeks`, `schedule_items`, …, and `flyway_schema_history` to see exactly which
migrations ran ([step 10](../docs/steps/10-seed-and-migrations.md)).

**SQL support in code:** with a data source connected, IntelliJ gives **schema-aware completion and
validation** inside `schema.sql`, the Flyway `V1`/`V2`/… migrations, and even the SQL strings passed to
`JdbcTemplate` (language injection — `Alt+Enter ▸ Inject language ▸ SQL` if it isn't automatic). It will
warn you about an unknown column before you ever run the app.

## 🐳 Docker & Compose (Ultimate)

Steps [11](../docs/steps/11-dockerize.md)–[12](../docs/steps/12-compose.md) build an image and a stack;
Ultimate runs them from the IDE:

- A ▶ gutter icon on the **`Dockerfile`** builds/runs the image; the **Services** tool window
  (`View ▸ Tool Windows ▸ Services`) shows containers, logs, and a shell — no terminal needed.
- A ▶ on the `services:` line of **`docker-compose.yml`** brings the whole stack up; expand `db` and `app`
  to tail logs and watch the healthcheck flip to healthy.
- For [step 14](../docs/steps/14-deploy.md)'s Kubernetes overview, Ultimate's Kubernetes plugin renders the
  illustrative `deploy/k8s/*.yaml` with completion and can apply them to a cluster.

## ✨ Worth knowing (Ultimate extras)

- **Profiler** — sample CPU/allocations of the running app from the run dashboard (useful once you care
  about the N+1 trap from [`jdbc-vs-jpa.md`](../docs/theory/jdbc-vs-jpa.md)).
- **JPA / Hibernate console** — when you tackle Spring Data JPA on the [roadmap](../docs/steps/99-roadmap.md),
  Ultimate adds an entity diagram and a JPQL console.
- **Spring Data / Actuator** dashboards — relevant when you add Actuator (observability, on the roadmap).

> [!IMPORTANT]
> None of this is required to finish the course — every step works from the terminal and the free tier.
> Ultimate just makes the loop faster and lets you *see* the beans, endpoints, and tables you're building.

## 🔗 Related
- 🧰 [Setup](../docs/00-setup.md) · 🧠 [Spring & DI](../docs/theory/spring-and-di.md) · 🗄️ [SQL & JdbcTemplate](cheatsheet-sql-jdbc.md) · 🌐 [HTTP & REST](cheatsheet-http-rest.md)
- 🎤 [Interview prep](interview-prep.md) — the Spring tooling above (Beans diagram, Endpoints window) is a great way to *show* the bean graph and REST mappings you'll be asked about · 🆚 [Boot 3.x → 4 deltas](cheatsheet-version-deltas.md)
- 📄 Official: [IntelliJ IDEA help](https://www.jetbrains.com/help/idea/) · [unified release](https://blog.jetbrains.com/idea/2025/12/intellij-idea-unified-release/)
