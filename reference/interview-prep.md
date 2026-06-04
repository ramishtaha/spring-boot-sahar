# Interview prep — the Sahar Q&A bank

> [!NOTE]
> A standalone, interview-grade question bank for a backend / Java / Spring role. Every answer teaches the concept on its own — no need to chase a link to understand it — but each section points at where you **already built it** in Sahar, so you can answer from lived experience instead of memorised trivia.

**Here is the thing to internalise before you read another word:** you did not "follow a tutorial." You shipped a layered Spring Boot 4 app — a REST API with validation, a real database behind a repository layer, Flyway migrations, a multi-stage Docker image, and a CI pipeline. The interviewer's questions below are *about the code you wrote*. Your job in the room is to **talk about it**: not "I think dependency injection means…", but "in Sahar, `ConfigController` never called `new RoutineService(...)` — it declared the dependency in its constructor and Spring handed one in, which is exactly why I could unit-test it with a stub."

That framing — concept, then *the place you met it* — is how this page is organised. Read a section, then say the answer out loud in terms of Sahar.

---

## 1. Spring core: beans, DI & IoC

> Where in Sahar: [`spring-and-di.md`](../docs/theory/spring-and-di.md) · the whole `web → service → repo` graph.

**What is Inversion of Control, and how does Dependency Injection relate to it?**
IoC is the principle that the *framework* controls when your objects are created and called, not your own code — the "inversion" is who calls whom. DI is the concrete technique that delivers IoC for collaborators: instead of an object building its dependencies with `new`, they are *handed in* from outside. In Sahar, `RoutineService` is handed its seven repositories; it never constructs them.

**Why is constructor injection preferred over field injection?**
Four reasons: dependencies can be `final` (set once, never null); the object can't exist half-wired (a missing bean fails loudly at startup, not as a later `NullPointerException`); the constructor signature *is* an honest list of what the class needs (seven params is a visible "this class does too much" smell); and you can `new` it in a plain JUnit test with no Spring at all.

```java
public ConfigController(RoutineService routine) {   // a single constructor needs no @Autowired
    this.routine = routine;                          // since Spring 6 it's autowired automatically
}
```

**What's the difference between `@Component` and `@Bean`?**
`@Component` (and its stereotypes `@Service`, `@Repository`, `@RestController`) goes on *your own class* and is picked up by component scanning. `@Bean` goes on a *method inside a `@Configuration` class* and is how you register an object you don't own — a third-party type, or one needing custom construction. Sahar's classes are all `@Component`-family; you'd reach for `@Bean` to expose, say, a configured `RestClient`.

**What is the ApplicationContext, and what's a bean's default scope?**
The ApplicationContext is Spring's runtime container: the in-memory registry of every bean, keyed mostly by type, that knows the dependency graph and wires it at startup. The default scope is **singleton** — one shared instance per app. That's why an in-memory edit in step 04 was visible to the next request: every controller got *the same* `RoutineService`.

> [!TIP]
> Interview tip: when asked "how does Spring find your beans?", name **component scanning** starting at the `@SpringBootApplication` package and going *down* — and mention the classic trap: a class outside the base package is silently never registered.

**Why does `@Repository` exist if it's "just" a `@Component`?**
The stereotypes document a class's role, but `@Repository` also does real work: it activates **exception translation**, turning a vendor-specific `SQLException` (from H2 or Postgres) into Spring's uniform `DataAccessException`. So the service layer catches one consistent exception type regardless of which database threw it.

---

## 2. Spring Boot: auto-config, starters & properties

> Where in Sahar: [`spring-and-di.md` §6](../docs/theory/spring-and-di.md) · [`00-baseline.md`](../docs/steps/00-baseline.md) · [`09-swap-to-postgres.md`](../docs/steps/09-swap-to-postgres.md).

**What does `@SpringBootApplication` actually combine?**
Three annotations: `@Configuration` (this class can define beans), `@ComponentScan` (scan this package and below for stereotypes), and `@EnableAutoConfiguration` (configure beans based on the classpath). One annotation on `SaharApplication`, and `SpringApplication.run(...)` builds the context and starts embedded Tomcat.

**Explain auto-configuration in one breath.**
Boot looks at what's on the classpath and creates sensible default beans — *but always backs off if you've defined your own bean of that type*. The conditions are "only if class X is present" and "only if no bean of type Y exists yet." Because `spring-boot-starter-jdbc` is present, Sahar gets a `DataSource`, a HikariCP pool, and a ready-to-inject `JdbcTemplate` for free — none of which I wrote.

**What's a starter, and which Boot 4 names trip people up?**
A starter is a curated, dependency-only Maven module that pulls a version-aligned set of libraries so you don't assemble them by hand. The chain is: *add a starter → its libs land on the classpath → auto-config sees them → beans appear.* Boot 4 renames worth knowing: `spring-boot-starter-web` → **`spring-boot-starter-webmvc`**; Flyway via `spring-boot-starter-flyway` + `flyway-database-postgresql` (not `flyway-core`); and the single `spring-boot-starter-test` split into modular `-webmvc-test`, `-jdbc-test`, `-validation-test`, `-flyway-test`.

**How do profiles and externalised config work?**
A **profile** is a named config set switched on per environment. Sahar's default runs on H2; activating the `postgres` profile (`SPRING_PROFILES_ACTIVE=postgres`) loads `application-postgres.properties` and reads the DB connection from `SAHAR_DB_URL` / `SAHAR_DB_USERNAME` / `SAHAR_DB_PASSWORD` environment variables. Same jar, two environments — the 12-factor "config in the environment" idea.

**What does `@ConfigurationProperties` buy you over `@Value`?**
`@Value("${some.key}")` injects one property at a time. `@ConfigurationProperties(prefix = "sahar")` binds a whole group of related properties onto a typed object, with validation and IDE completion — far cleaner once you have more than a couple of settings. It's the idiomatic way to model a feature's configuration as a single object.

---

## 3. Web & REST

> Where in Sahar: [`http-and-rest.md`](../docs/theory/http-and-rest.md) · [`02-first-rest-endpoint.md`](../docs/steps/02-first-rest-endpoint.md) · [`07-full-crud.md`](../docs/steps/07-full-crud.md).

**What is the DispatcherServlet?**
The single front-controller servlet at the heart of Spring MVC. Embedded Tomcat hands *every* request to it; it consults a HandlerMapping to find the matching `@RequestMapping` method, binds the arguments, invokes your controller, and renders the response. You write `ConfigController`; Tomcat never calls it directly — it calls the dispatcher, which calls you.

**`@Controller` vs `@RestController`?**
`@Controller` returns *view names* (for server-rendered HTML). `@RestController` = `@Controller` + `@ResponseBody`, meaning every method's return value *is* the response body, serialized to JSON by an `HttpMessageConverter` (Jackson 3 in Boot 4). All of Sahar's API controllers are `@RestController`.

**PUT vs POST — when do you use each, and why does it matter?**
`PUT` *replaces* the resource at a known URL and is **idempotent** — send it once or ten times, same end state. `POST` *creates* a new sub-resource (the server mints the id) or *runs a command*, and is **not** idempotent — each call makes another row. In Sahar, `PUT /api/prayer-times` replaces the one prayer-times row; `POST /api/schedule` creates a new slot (`id` is `null` in, real id out).

> [!TIP]
> Interview tip: the killer follow-up is *"my request timed out — can I retry?"* Answer with the retry test: `PUT`/`DELETE` yes (idempotent), `POST` no (you might create a duplicate). That's not bureaucracy — it's what makes an API safe over an unreliable network.

**Which status codes, and when?**
`200 OK` (default for GET/PUT), `201 Created` (POST that made a resource — return it *with* its new id), `204 No Content` (DELETE — nothing to send back, pairs with a `void` method), `400 Bad Request` (client sent something invalid — e.g. a validation failure), `404 Not Found`. The leading digit assigns blame: `4xx` = your request is wrong (don't retry as-is), `5xx` = the server broke (retry might help).

**What is content negotiation?**
The sender's `Content-Type` header declares the format it's *sending*; the client's `Accept` header lists formats it will *take back*. Spring matches `Accept` against what it can produce and picks one. With only JSON on the menu the match is trivial, but the same machinery could serve XML if the library were present.

**Safe vs idempotent — are they the same?**
No. **Safe** = changes nothing on the server (`GET`). **Idempotent** = N calls leave the same state as one call. Safe implies idempotent, but the interesting cases *do* change state yet stay idempotent: `PUT` and `DELETE`.

---

## 4. Validation & error handling

> Where in Sahar: [`05-validation-and-rules.md`](../docs/steps/05-validation-and-rules.md) · [`http-and-rest.md` §3](../docs/theory/http-and-rest.md) · [`99-roadmap.md` §2](../docs/steps/99-roadmap.md).

**How does Bean Validation work in a controller?**
You annotate fields with constraints (`@NotBlank`, `@Size`, `@Pattern`) and put `@Valid` on the `@RequestBody` parameter. Spring runs the validator before your method body; on failure it throws `MethodArgumentNotValidException` *before* any business logic runs. Sahar validates `PrayerTimes`, `ScheduleItem`, and `BlockPlan` this way.

```java
@PutMapping("/api/prayer-times")
public PrayerTimes update(@Valid @RequestBody PrayerTimes prayerTimes) { ... }  // @Valid triggers it
```

**How do you write a custom, cross-field rule?**
A class-level annotation backed by a `ConstraintValidator`. Sahar's `@DeloadLast` on `BlockPlan` enforces "the deload week is always last" — a rule that spans the whole list, so it produces a *global* error rather than a field error. That's the difference between per-field constraints and a business invariant.

**What is `@ControllerAdvice` / `@RestControllerAdvice` for?**
Centralised, cross-cutting exception handling. Instead of try/catch in every controller, one `@RestControllerAdvice` class maps exception types to responses with `@ExceptionHandler`. Sahar's `ApiExceptionHandler` catches `MethodArgumentNotValidException` and turns it into a tidy `400` with a predictable body the admin UI can render.

**What is RFC 9457 / `ProblemDetail`, and why prefer it over a custom error record?**
RFC 9457 ("Problem Details for HTTP APIs") is the *standard* error body: `application/problem+json` with agreed fields `type`, `title`, `status`, `detail`, `instance`. Spring supports it first-class via the `ProblemDetail` class. Sahar today returns a hand-rolled `ApiError(status, error, messages)` record — fine to start, but every bespoke error shape is something clients must special-case. Migrating to `ProblemDetail` means any standard client library already understands your errors. (This is item 2 on the [roadmap](../docs/steps/99-roadmap.md).)

> [!TIP]
> Interview tip: a strong answer names the trade-off honestly — "we shipped a custom `ApiError` to keep step 05 simple; the production move is `ProblemDetail` so the contract is standard, *and* extending the advice to map `DataAccessException` and not just validation failures."

---

## 5. Persistence

> Where in Sahar: [`06-jdbctemplate-h2.md`](../docs/steps/06-jdbctemplate-h2.md) · [`jdbc-vs-jpa.md`](../docs/theory/jdbc-vs-jpa.md) · [`10-seed-and-migrations.md`](../docs/steps/10-seed-and-migrations.md).

**What does `JdbcTemplate` do for you over raw JDBC?**
It removes the boilerplate — opening/closing connections, creating statements, binding parameters, iterating the `ResultSet`, exception handling — so you write just the SQL and a `RowMapper`. There's no hidden cache, no managed object, no surprise flush: *what you call is what runs*. Sahar's repositories use `jdbc.query(sql, MAPPER)` to read and `jdbc.update(sql, args...)` to write.

**JDBC/JdbcTemplate vs Spring Data JPA — how do you choose?**
The core difference isn't syntax, it's *who writes the SQL*. With JdbcTemplate **you** write every query and map each column by hand — full control, predictable, more code. With JPA you declare `@Entity` classes and Hibernate generates the SQL, tracks objects in a persistence context, and dirty-checks changes at commit — far less code, but more implicit behaviour to learn. Choose JdbcTemplate for small, stable schemas and hand-tuned reads (Sahar: 8 tables); reach for JPA for rich object graphs and CRUD-heavy apps across many tables.

**What is the N+1 problem?**
With lazy JPA associations, loading N parents and then touching each one's children fires **1 + N** queries instead of 1 — e.g. `findAll()` blocks then a `SELECT` per block on first `getWeeks()`. Nothing in your code looks wrong; Hibernate emits the extra round-trips silently, so it surfaces as a production latency incident. Fixes: `JOIN FETCH`, `@EntityGraph`, batch fetching, or DTO projections. With Sahar's JdbcTemplate there *is* no N+1 — you can't accidentally fire a query; `BlockRepository.find()` runs exactly two SELECTs, always.

**What is a transaction, and how do you get one in Spring?**
A group of DB operations that all succeed or all fail (atomicity), so a partial update never leaves data inconsistent. You mark a service method `@Transactional`. Sahar's block "replace" — UPDATE the parent, DELETE the old weeks, re-INSERT them — is wrapped in one transaction so a half-written block is never visible.

**What is Flyway and why not just let Hibernate generate the schema?**
Flyway runs versioned SQL scripts (`V1__…`, `V2__…`) in order and records what it applied in `flyway_schema_history`, so every environment converges on the same schema and every change is reviewable in a PR. Hibernate's `ddl-auto=update` *feels* magical but is silent, un-reviewed, and has no down path — dangerous in prod. The right combo even with JPA is `ddl-auto=validate` + Flyway. Sahar owns its DDL via Flyway from step 10.

---

## 6. Testing

> Where in Sahar: [`99-roadmap.md` §3](../docs/steps/99-roadmap.md) · the modular `*-test` starters in `pom.xml`.

**What is the testing pyramid?**
Lots of fast, focused unit tests at the base; fewer integration/slice tests in the middle; a thin top of slow end-to-end tests. The shape keeps the suite fast and failures specific. The `PrayerTimeCalculator` (pure math, no Spring) is a perfect base-of-pyramid unit test — it needs no context to run.

**`@WebMvcTest` vs `@SpringBootTest` — when each?**
`@WebMvcTest(BlockController.class)` is a **slice** test: it loads *only* the web layer with a mocked service, and you drive it with **MockMvc** (`mockMvc.perform(put("/api/block")...).andExpect(status().isOk())`). Fast; tests HTTP + JSON + validation. `@SpringBootTest` wires the **whole** app for end-to-end flows (PUT a block, GET `/api/config`, assert it changed). Use the slice for focused layer tests, the full context sparingly for true integration.

```java
@WebMvcTest(ScheduleController.class)   // web layer only
// MockMvc + a mocked RoutineService -> fast, isolated HTTP/JSON/validation tests
```

**Why mock a dependency?**
To test one unit in isolation. Mocking `RoutineService` lets you test the controller's *own* logic (routing, status codes, JSON) without standing up the service, its repositories, and a database. This is the payoff of constructor injection from §1 — you just pass a stub.

**What does Testcontainers add over an in-memory H2 test?**
It runs your integration tests against a **real Postgres in a throwaway Docker container**, catching dialect/type/reserved-word differences that H2 hides. Because Sahar runs on both H2 and Postgres, a Testcontainers Postgres test is the only way to prove the Postgres path actually works in CI without a hosted database.

> [!TIP]
> Interview tip: Sahar ships only a context-load `@SpringBootTest` — be honest and turn it into a strength: "the modular test starters are already in the `pom.xml`; the gap is *using* them — `@WebMvcTest` for the controllers and a Testcontainers Postgres test for the repo, because the `@DeloadLast` rule and the wipe-and-reinsert deserve real coverage."

---

## 7. Security

> Where in Sahar: [`99-roadmap.md` §4](../docs/steps/99-roadmap.md) (the top-priority next step — Sahar ships with **no** auth).

**Authentication vs authorization?**
**Authentication** = *who are you* (proving identity — a password, a token, an OIDC login). **Authorization** = *what may you do* (permissions — can this identity edit the block?). Spring Security answers both with the same filter chain. Sahar maps cleanly onto this: `/` (read view) should be public, `/admin.html` and the write endpoints owner-only.

**How does Spring Security actually intercept a request?**
Via a **servlet filter chain** that sits *in front of* the DispatcherServlet — so a request is authenticated and authorized *before* it ever reaches `BlockController`. You configure it with a `SecurityFilterChain` bean: permit `GET` on the public paths, require authentication for everything else.

**Stateless (token) vs session-based auth — what's the trade-off?**
Session-based stores server-side state and hands the client a session cookie — simple, but the server must remember sessions (awkward to scale horizontally). Stateless (e.g. a JWT) carries identity in the token itself, so any replica can serve any request with no shared session store — which fits the REST statelessness principle but pushes token-revocation complexity onto you.

**What is CSRF and when do you need protection?**
Cross-Site Request Forgery tricks a logged-in user's browser into firing a state-changing request using their cookie. It matters for **cookie/session** auth with browser forms; Spring Security enables CSRF protection by default there. For a **stateless token** API (no ambient cookie), it's commonly disabled because the attack vector doesn't apply.

**How should passwords be stored?**
Never plaintext, never plain hashes. Use a slow, salted, adaptive hash — **BCrypt** is the standard via Spring Security's `PasswordEncoder`. The work factor makes brute-forcing expensive, and the per-password salt defeats rainbow tables.

**Basic vs Form vs OAuth2 login — pick for Sahar?**
HTTP **Basic** or **Form** login with one configured user is minutes of config and closes the open `/admin.html` door today (cheapest). A **form login with a DB user store** (`UserDetailsService` + BCrypt) is the step up for multiple users. **OAuth2/OIDC** ("Sign in with Google") means you never store a password — overkill for one user, right if you publish it. Start with the gate.

> [!TIP]
> Interview tip: if Sahar comes up, say it out loud — "right now *nothing* stops a stranger who can load `/admin.html` from rewriting my training block; the smallest fix is a `SecurityFilterChain` permitting GET on the public paths and requiring auth everywhere else." Knowing your app's biggest hole is a strong signal.

---

## 8. Observability

> Where in Sahar: [`99-roadmap.md` §7](../docs/steps/99-roadmap.md) (a recommended next step).

**What does Spring Boot Actuator give you?**
Add `spring-boot-starter-actuator` and you instantly get operational endpoints: `/actuator/health` (is it up? is the DB reachable?), `/actuator/info`, `/actuator/metrics`. It turns a running app from a black box into something that *tells you* how it's doing — essential once you can't attach a debugger.

**Liveness vs readiness — what's the difference, and who consumes them?**
**Liveness** = "is the process alive, or wedged and needing a restart?" **Readiness** = "is it ready to *accept traffic* right now?" (e.g. DB connected, warmup done). Kubernetes uses `/actuator/health/liveness` for its `livenessProbe` (restart if failing) and `/readiness` for the `readinessProbe` (route traffic only when ready). Without them, K8s has no idea your app has hung.

**What is Micrometer?**
The metrics *facade* Actuator uses — a vendor-neutral API you instrument against, then point at a backend (Prometheus, etc.). You get JVM memory, HTTP request timings, HikariCP pool usage, and your own custom counters ("blocks rolled forward this month") — all scrapeable and graphable. Metrics on the connection pool would have made an N+1 problem *visible* instead of mysterious.

**How would you add a custom health check?**
Implement `HealthIndicator` (or `ReactiveHealthIndicator`) as a bean; its `health()` method returns `Health.up()` / `Health.down()` and is folded into `/actuator/health`. A check that verifies the Postgres connection is the smallest, highest-value addition to a deployed app — it's what lets the platform auto-restart a wedged instance.

---

## 9. Caching & resilience

> Where in Sahar: conceptually adjacent to the [`GeocodingService`](../docs/steps/15-geolocation-prayer-times.md) `RestClient` call (calling an external service) and the [roadmap](../docs/steps/99-roadmap.md).

**Explain the cache-aside pattern.**
On read: check the cache; on a hit, return it; on a miss, load from the source, *put it in the cache*, then return. Writes invalidate or update the entry. The app owns the cache logic, and the cache is just a fast side-store — if it's empty or down, you still work, just slower. It's the most common application caching pattern.

**How does `@Cacheable` work in Spring?**
Annotate a method `@Cacheable("places")` and Spring wraps it with a proxy: before invoking the method it checks the named cache keyed by the arguments; on a hit it returns the cached value and *skips* the method entirely. `@CacheEvict` / `@CachePut` manage invalidation. Sahar's geocoding (place name → coordinates) is a textbook candidate — the same query maps to the same result, and Nominatim rate-limits you.

**Why is a missing timeout dangerous?**
A call with no timeout can block *forever* if the remote service hangs. Each blocked call holds a Tomcat worker thread (one-thread-per-request model); enough hung calls exhaust the pool and your *whole* app stops responding — a slow dependency becomes a total outage. Sahar's `GeocodingService` calls an external service (OpenStreetMap), exactly where this bites; every outbound client needs a connect *and* read timeout.

> [!TIP]
> Interview tip: "a missing timeout turns a slow dependency into a full outage" is a line that lands. Pair it with retries: retry only **idempotent** operations, with **backoff + jitter**, and a **circuit breaker** so you stop hammering a service that's already down.

**Retries and circuit breakers — what's the discipline?**
Retry transient failures, but only for idempotent calls (a retried `POST` may double-charge). Use exponential backoff with jitter so clients don't synchronise into a thundering herd. A circuit breaker "opens" after repeated failures to fail fast and give the downstream time to recover, then half-opens to test. Together: timeout + bounded retry + breaker = resilience.

---

## 10. Events & decoupling

> Where in Sahar: conceptually the `web → service` boundary; a natural extension of the layered design in [`spring-and-di.md`](../docs/theory/spring-and-di.md).

**What is `ApplicationEventPublisher` for?**
In-process, decoupled communication between beans. A publisher fires an event (`publisher.publishEvent(new BlockRolledForward(...))`); any bean with a matching `@EventListener` method handles it — and the publisher neither knows nor cares who listens. It lets you bolt on a side-effect (audit log, notification) without editing the code that does the main work.

**What does `@TransactionalEventListener(phase = AFTER_COMMIT)` solve?**
A plain `@EventListener` runs *inside* the publishing transaction — so if you fire "block saved" and then the transaction rolls back, you've already sent the side-effect for a write that never happened. `@TransactionalEventListener(phase = AFTER_COMMIT)` defers the handler until the transaction **successfully commits**, so you never email/notify about a change that got rolled back. It's the standard way to trigger side-effects safely off a DB write.

**In-process events vs a message broker (Kafka/RabbitMQ) — when do you cross over?**
Spring's `ApplicationEvent` is **in-memory, same process, synchronous-by-default** — great for decoupling beans inside one app, but the event dies if the process dies. A **message broker** gives you durability, cross-service delivery, buffering, and async fan-out across a fleet. Use events for intra-app decoupling; reach for a broker when the producer and consumer are *different services* or the message must survive a crash.

---

## 11. Containers, CI/CD & 12-factor

> Where in Sahar: [`containers-and-devops.md`](../docs/theory/containers-and-devops.md) · [`11-dockerize.md`](../docs/steps/11-dockerize.md) · [`13-ci-with-github-actions.md`](../docs/steps/13-ci-with-github-actions.md).

**Image vs container?**
An **image** is an immutable, read-only template — a stack of filesystem layers plus metadata (entrypoint, env, port). A **container** is a running (or stopped) *instance* of an image with a thin writable layer on top. Class vs object. You `build` an image once and `run` it many times into independent containers. Consequence: a container is disposable, so any state that matters must live in a **volume** outside it.

**Why a multi-stage Docker build?**
To *build* you need a full JDK + Maven (hundreds of MB); to *run* you need only a JRE + the jar. A multi-stage build uses one stage to compile and a slim final stage that `COPY --from=build` pulls in *only the jar* — leaving the compiler and Maven behind. Smaller image, faster pulls, smaller attack surface. Sahar also orders `COPY pom.xml` *before* `COPY src` so a code edit doesn't re-download dependencies (layer caching).

**CI vs CD — what's the line?**
**CI** = on every push/PR, a clean machine checks out, builds, and tests, keeping `main` always green. **CD** = on green, the artifact is shipped (Delivery = ready to release; Deployment = pushed automatically). Mantra: *CI = build + test on every push; CD = deploy on green.* Sahar's `ci.yml` is pure CI plus a `docker build` sanity check — the deploy/publish job is deliberately left commented out.

**Why does image tagging matter — what's wrong with `:latest`?**
`:latest` is a *mutable* pointer — "the image at this tag" changes under you, so a rollback or an audit can't pin *which* build ran. Tag with something immutable and traceable (a git SHA or a semantic version) so every deploy maps to exact source. `:latest` for convenience, an immutable tag for what you actually ship.

**Name a few 12-factor principles you used.**
**Config in the environment** (the same image runs on H2 or Postgres purely via env vars — no rebuild); **one codebase, many deploys**; the executable **jar as a single build artifact**; the **container as the unit of deploy**; **logs as streams** (write to stdout, let the platform collect them). Sahar leans on all of these.

> [!TIP]
> Interview tip: on secrets, say "base64 in a committed Kubernetes manifest is *encoding, not encryption* — not a secret." Real secrets live in GitHub Actions secrets / a secret manager and are injected at runtime; `SAHAR_DB_PASSWORD` never goes in Git.

---

## 12. API contracts

> Where in Sahar: the JSON contract every controller speaks; see [`http-and-rest.md`](../docs/theory/http-and-rest.md).

**What is OpenAPI / Swagger, and why does a contract matter?**
OpenAPI is a standard, machine-readable description of your HTTP API — every path, verb, request/response schema, and status code. (Swagger is the tooling ecosystem around it, including Swagger UI.) In Spring you generate it from your annotated controllers (e.g. springdoc). The contract matters because it's the single source of truth between backend and clients: it generates interactive docs and client SDKs, enables contract testing, and lets a frontend build against the API *before* it's finished — instead of reverse-engineering endpoints by trial and error.

**How does an explicit contract reduce bugs across teams?**
Without one, every client guesses your error shapes, field names, and status codes — and breaks silently when you change them. A versioned OpenAPI doc makes the API a *reviewable artifact*: a breaking change shows up as a diff, clients regenerate against it, and contract tests fail loudly in CI instead of in production. (This is the same instinct as adopting RFC 9457 `ProblemDetail` in §4 — standardise the shape so clients don't special-case yours.)

---

## How to use this

- **Don't memorise — rehearse.** For each question, say the answer *out loud* and end with "…and in Sahar, that's the `X` in [step/file]." Lived examples beat recited definitions in an interview.
- **Lead with the trade-off.** The strong answers above (`JdbcTemplate` vs JPA, custom error vs `ProblemDetail`, gate vs OAuth2, `:latest` vs SHA tags) are *judgement calls* — interviewers reward "here's the trade-off and why I chose this for *this* app" over a one-word definition.
- **Know your app's gaps.** Sahar deliberately stops short on Security, Testing, and Observability. Naming those — and the *smallest* fix for each — signals senior instincts. The [roadmap](../docs/steps/99-roadmap.md) is your gap list.
- **Practice the demo line.** "I built a Spring Boot 4 REST API — layered web/service/repo, Bean Validation with a custom rule, JdbcTemplate over H2 and Postgres via profiles, Flyway migrations, a multi-stage Docker image, and GitHub Actions CI." That single sentence opens most backend conversations.

---

⬅️ [README](../README.md) · 📚 [Steps](../docs/steps/) · 🗺️ [Roadmap](../docs/steps/99-roadmap.md) · 📖 [Glossary](glossary.md)
