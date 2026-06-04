# Spring Boot 3.x → 4 / Java 17 → 25 — what changed

_The single home for the version story every other Sahar doc points at: when an older tutorial says X, find the modern X here._

**How to use this page.** Sahar targets **Spring Boot 4.0.6 / Spring Framework 7 / Java 25 / Jakarta EE 11 / Jackson 3**. Most tutorials, Stack Overflow answers, and AI snippets online were written for the *older* stack (**Boot 3.x / Spring 5–6 / Java 17 / `javax` / Jackson 2**). When you hit a symbol that does not resolve, an import that does not exist, or an API that is "deprecated" — look it up in the table below, swap to the NEWER column, and read the one-liner for why it changed and where Sahar shows it. That is the whole job of this page.

> [!TIP]
> **Java 25 is an LTS** (Long-Term Support) release — the production-grade baseline you build on, supported for years, not a short-lived feature drop. Boot 4 needs Java 17+, and Sahar pins 25.

---

## 🔀 The deltas (older → newer)

> [!NOTE]
> **The version story in one place.** Two big jumps stack up here. The **`javax.*` → `jakarta.*`** rename and the single-`spring-boot-starter-test` split-readiness began in the *earlier* Spring 5→6 / Boot 2→3 / Jakarta EE 9 transition. The **Boot 4 / Spring 7 / Java 25 / Jackson 3 (`tools.jackson`)** moves are *this* course's stack. The table treats OLDER = "what an online Boot 3.x tutorial shows" and NEWER = "what Sahar uses" so you can translate either way without tracking which jump introduced what.

| Topic | OLDER (Boot 3.x / Spring 5–6 / Java 17 / `javax` / Jackson 2) | NEWER (Sahar: Boot 4.0.6 / Spring 7 / Java 25 / `jakarta` / Jackson 3) | Why it matters / where in Sahar |
|---|---|---|---|
| **Web starter** | `spring-boot-starter-web` | `spring-boot-starter-webmvc` (blocking servlet stack); `spring-boot-starter-webflux` for reactive | The starter name now states *which* web model you chose. The Boot 3.x name simply won't resolve. See [00](../docs/steps/00-baseline.md), [02](../docs/steps/02-first-rest-endpoint.md). |
| **Test starter** | one `spring-boot-starter-test` for everything | modular `spring-boot-starter-{webmvc,jdbc,validation,flyway}-test` (each `test` scope) | Add only the slices you use; smaller, clearer test classpath. Sahar pulls all four. See [18](../docs/steps/18-testing.md). |
| **Flyway dependency** | `org.flywaydb:flyway-core` (added directly) | `spring-boot-starter-flyway` + `org.flywaydb:flyway-database-postgresql` | Boot 4 auto-configures Flyway via the starter; the Postgres dialect is a separate module (required since Flyway 10). See [10](../docs/steps/10-seed-and-migrations.md). |
| **H2 web console** | `spring.h2.console.enabled=true` flag (console bundled with the driver) | `spring-boot-h2console` module (add the dependency) | The console is now its own module, not a property toggle on the driver. See [06](../docs/steps/06-jdbctemplate-h2.md), [09](../docs/steps/09-swap-to-postgres.md). |
| **EE namespace** | `javax.*` (e.g. `javax.validation`, `javax.servlet`) | `jakarta.*` (Jakarta EE 9+; Boot 4 targets EE 11) | The rename landed in the 2→3 jump and Boot 4 keeps it. Every Sahar import is `jakarta.*`. See [05](../docs/steps/05-validation-and-rules.md). |
| **JSON library** | `com.fasterxml.jackson.*` (Jackson 2) | `tools.jackson.*` (Jackson 3) | New Maven groupId *and* package root. Custom serializers/`ObjectMapper` code from Boot 3 tutorials needs re-importing. Sahar lets the starter handle it. See [02](../docs/steps/02-first-rest-endpoint.md). |
| **HTTP client** | `RestTemplate` (maintenance mode) | `RestClient` (Spring 6.1+, fluent + synchronous) | `RestTemplate` still works but gets no new features; `RestClient` is the modern synchronous client. Sahar's geocoder uses it. See [15](../docs/steps/15-geolocation-prayer-times.md), [21](../docs/steps/21-resilient-geocoder.md). |
| **Error body** | hand-rolled `{"message": ...}` per endpoint | `ProblemDetail` (RFC 9457, `application/problem+json`) | One standard, machine-readable error contract instead of a bespoke blob per controller. See [19](../docs/steps/19-error-handling.md). |
| **Health checks** | `org.springframework.boot.actuate.health.HealthIndicator` | `org.springframework.boot.health.contributor.HealthIndicator` (Boot 4) | The interface moved package in Boot 4 — a copied Boot 3 custom health check won't compile until you fix the import. See [20](../docs/steps/20-observability.md). |
| **Security config** | `WebSecurityConfigurerAdapter` (removed) | a `SecurityFilterChain` `@Bean` | The adapter base class is gone; you now declare a bean. Most Boot 3.x security tutorials still extend the dead class. See [24](../docs/steps/24-security.md). |
| **Mockito in tests** | `@MockBean` | `@MockitoBean` | `@MockBean` was deprecated and replaced; same idea, new annotation. See [18](../docs/steps/18-testing.md). |
| **OpenAPI / springdoc** | springdoc-openapi **2.x** (Boot 3) | springdoc-openapi **3.x** (Boot 4) | The 2.x line targets Boot 3; you need the 3.x line for Boot 4 compatibility. See [25](../docs/steps/25-openapi-cicd.md). |

> [!IMPORTANT]
> Symptom → fix shortcuts: `Could not resolve ... spring-boot-starter-web` → use `-webmvc`. `package javax.validation does not exist` → it's `jakarta.validation`. `package com.fasterxml.jackson... does not exist` → it's `tools.jackson` (Jackson 3). `cannot find symbol: class WebSecurityConfigurerAdapter` → define a `SecurityFilterChain` bean instead.

> [!WARNING]
> Artifact names, package roots, and the minimum Java version can shift across minor releases. This page reflects Boot 4.0.6 / Java 25. When a line ages, confirm against the [Boot 4.0.6 reference](https://docs.spring.io/spring-boot/4.0.6/).

---

Reference: [Maven](cheatsheet-maven.md) · [Spring annotations](cheatsheet-spring-annotations.md) · [Fundamentals](cheatsheet-fundamentals.md) · [Glossary](glossary.md) · [Interview-prep](interview-prep.md) · [README](../README.md)
