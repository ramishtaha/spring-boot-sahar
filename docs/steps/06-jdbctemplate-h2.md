# 06 - Persistence with JdbcTemplate and H2

_The day Sahar grows a memory: edit a prayer time, restart the app, and the change is still there._

> [!IMPORTANT]
> **Checkpoint:** [`step-06-jdbctemplate-h2`](../../checkpoints/step-06-jdbctemplate-h2/) — Sahar with a real
> data layer: a `DataSource` + HikariCP pool, seven SQL repositories, an H2 file-mode database that survives
> restarts, and an in-memory H2 for isolated tests. Package is `com.ramishtaha.sahar`.

> [!TIP]
> **IntelliJ IDEA** — the **Database** tool window (free since 2025.3) connects to the H2 file `jdbc:h2:file:./data/sahar`, so you can browse tables and run SQL; **SQL support** even completes column names inside `schema.sql` and your `JdbcTemplate` strings. [Setup →](../../reference/intellij-ultimate.md)

## 🎯 Why this matters

Up to step 05, Sahar forgot everything on restart. The whole routine lived in one field, `private RoutineConfig config = RoutineSeed.defaultConfig();`, inside `RoutineService`. Edit Fajr to `04:35`, stop the app, start it again, and you are back to the seed. For a notebook that is fine; for a real personal app it is useless.

This step gives Sahar durable storage. We introduce the **data layer**: a `DataSource`, a connection pool, a set of **repositories** that speak SQL, and an embedded database (H2) running in **file mode** so the data is a real file on disk that survives restarts.

The headline lesson is not "SQL". It is the **payoff of layering** you set up in steps 02-05. The controllers do not change one character. `RoutineService` keeps the exact same method signatures (`getConfig()`, `updatePrayerTimes(...)`, `updateMonth(...)`, `updateBlock(...)`). We rip out the in-memory field and rewire the service to read and write a database behind those methods, and the web layer never notices. That is exactly what good layering buys you, and you will feel it again in step 09 when we swap H2 for PostgreSQL and only the wiring changes.

See [../theory/persistence-landscape.md](../theory/persistence-landscape.md) for where embedded databases, connection pools, and the JDBC API sit in the bigger picture, and [../theory/jdbc-vs-jpa.md](../theory/jdbc-vs-jpa.md) for why this course uses `JdbcTemplate` instead of JPA/Hibernate.

> [!NOTE]
> **What changed from Spring Boot 3.x.** The persistence basics are stable, but two things bite if you copy old
> tutorials. (1) The **H2 web console** is no longer bundled with the driver-plus-a-flag the way it was in Boot
> 3.x — in Boot 4 it ships as a dedicated module, `spring-boot-h2console`, that you add yourself (see step 1).
> (2) `JdbcTemplate` and HikariCP still arrive through `spring-boot-starter-jdbc`, but on **Java 25** the
> repository code leans on modern Java (records as row targets, text blocks for multi-line SQL) that did not
> exist on Java 8/11. The `javax.sql` → `jakarta` rename does **not** touch you here — `DataSource` lives in
> `javax.sql` either way (it's a JDK package, not Jakarta EE). Full table: [Version deltas](../../reference/cheatsheet-version-deltas.md).

## 🧠 Theory

### DataSource and connection pooling (HikariCP)

A **`DataSource`** (the standard Java factory object that hands your code database connections — you ask it for one, it gives you one) is the abstraction for "the thing that hands me database connections". Opening a real connection to a database is expensive - a TCP handshake, authentication, session setup - far too slow to do per HTTP request. So instead of opening one each time, applications keep a **connection pool** (a small set of already-open connections kept ready and reused, so no request pays the open-and-close cost) and borrow/return them.

Spring Boot auto-configures **HikariCP** (the default pool since Boot 2) the moment it sees `spring.datasource.*` properties and a JDBC driver on the **classpath** (the set of compiled classes and JARs the JVM can load at runtime — see [Fundamentals](../../reference/cheatsheet-fundamentals.md)). You never write pool code; you just configure the URL. When a repository runs a query it borrows a connection from the pool, uses it, and returns it - all invisibly.

### JdbcTemplate: SQL without the ceremony

Raw JDBC is correct but tedious and error-prone: open a connection, create a statement, set parameters, execute, iterate the `ResultSet`, and close everything in the right order in `finally` blocks (leak a connection and your pool eventually starves). `JdbcTemplate` does all of that ceremony for you. You supply two things:

- the **SQL** (with `?` placeholders), and
- a **`RowMapper<T>`** that turns one result row into one object.

Three methods cover almost everything in this step:

- `query(sql, mapper, args...)` - run a `SELECT`, map every row to a `List<T>`.
- `queryForObject(sql, type, args...)` - run a `SELECT` that returns exactly one value/row.
- `update(sql, args...)` - run `INSERT`/`UPDATE`/`DELETE`, returns the affected row count.

`JdbcTemplate` itself is auto-configured by `spring-boot-starter-jdbc` from your `spring.datasource.*` properties, so a repository just asks for one in its constructor and Spring injects it. (See [../theory/spring-and-di.md](../theory/spring-and-di.md) for why constructor injection is the default.)

### Bind parameters, never string concatenation

The `?` in the SQL are **bind parameters**. The driver sends the SQL template and the values separately, so a value can never be parsed as SQL. This is the defence against **SQL injection** and it is non-negotiable:

```java
// NEVER do this - a value like "'; DROP TABLE weeks; --" becomes executable SQL:
jdbc.update("UPDATE app_meta SET month_label = '" + month + "' WHERE id = 1");

// ALWAYS do this - the value is bound, never interpreted as SQL:
jdbc.update("UPDATE app_meta SET month_label = ? WHERE id = 1", month);
```

The `PrayerTimesRepository` Javadoc says it outright: _"never string-concatenate user input into SQL, or you invite SQL injection."_ Quick reference for the JDBC API is in [../../reference/cheatsheet-sql-jdbc.md](../../reference/cheatsheet-sql-jdbc.md).

### The Repository layer

A repository is the **only** place in the app that knows SQL. The service talks to it in domain terms (`PrayerTimes`, `BlockPlan`, `Week`) and never sees a `ResultSet`. That boundary is what makes the database swap in step 09 painless.

### A request reaching the database

```mermaid
flowchart LR
    B[Browser PUT /api/prayer-times] --> C[PrayerTimesController]
    C --> S[RoutineService]
    S --> R["PrayerTimesRepository<br/>(JdbcTemplate)"]
    R --> P[HikariCP pool]
    P --> DB[("H2 file<br/>./data/sahar.mv.db")]
    DB --> P --> R --> S --> C --> B
```

### The eight tables (from `schema.sql`)

```text
app_meta        title, tagline, month_label                (single row, id = 1)
prayer_times    fajr, sunrise, dhuhr, asr, maghrib, isha    (single row, id = 1)
blocks          label                                       (single row, id = 1)
weeks           block_id, ordinal, name, dates, foci, deload   (children of the block)
schedule_items  slot_time, title, detail, category, day_type, sort_order
weekly_grid     day_of_week, discipline, note, sort_order
supplements     time_label, name, dose, purpose, note, sort_order
diet_sections   ordinal, title, body
```

Two design choices worth noting. **Reserved-word-safe names**: a column called `time` or a table called `week` clashes with SQL keywords on some engines, so the schema uses `slot_time`, `day_of_week`, and plural table names. **Portable DDL**: `GENERATED BY DEFAULT AS IDENTITY` and `BOOLEAN` are SQL-standard and behave identically on H2 now and PostgreSQL in step 09, so one `schema.sql` serves both.

## 🚦 Start from

Continue from the step 05 checkpoint, [step-05-validation-and-rules](../../checkpoints/step-05-validation-and-rules/). There, `RoutineService` was pure memory:

```java
@Service
public class RoutineService {
    private RoutineConfig config = RoutineSeed.defaultConfig();

    public synchronized RoutineConfig getConfig() {
        return config;
    }
    // ...withPrayerTimes / withMonth / withBlock, all on the in-memory field
}
```

Its own Javadoc names the limitation we are about to fix: _"It lives in memory only, so it survives requests but NOT a restart... That is the limitation step 06 fixes by moving state into a database."_ The controllers, domain records, and `RoutineSeed` from step 05 are unchanged.

## 🛠️ Build it

### 1. Add the JDBC and H2 dependencies

In Spring Boot 4 the pieces are modular. Add the JDBC starter (which brings in HikariCP and `JdbcTemplate`) and the H2 driver to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

Boot 4 difference worth remembering (this is the one from the [version-story callout](#-why-this-matters) above): the H2 web console is no longer bundled with the driver plus a flag the way it was in Boot 3.x. It now ships as a dedicated module, `spring-boot-h2console`. Add it so `/h2-console` works:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-h2console</artifactId>
</dependency>
```

> [!NOTE]
> Reference: [docs.spring.io/spring-boot/4.0.6/](https://docs.spring.io/spring-boot/4.0.6/). Module names can shift between minor versions - check the docs for your exact Boot version.

### 2. Configure the data source - H2 in file mode

Edit `src/main/resources/application.properties`:

```properties
# --- Data source -----------------------------------------------------------
spring.datasource.url=jdbc:h2:file:./data/sahar;AUTO_SERVER=TRUE
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

# Run schema.sql on every startup. It uses CREATE TABLE IF NOT EXISTS, so this is safe to repeat.
spring.sql.init.mode=always

# The H2 web console, served at http://localhost:8080/h2-console while the app runs.
spring.h2.console.enabled=true
```

Line by line:

- `jdbc:h2:file:./data/sahar` - **file mode**. The database is a real file at `./data/sahar.mv.db`, relative to where you run the app. This is the whole point of the step: the file survives restarts. (The earlier `jdbc:h2:mem:` form is in-memory and vanishes on shutdown - we use that only for tests, below.)
- `AUTO_SERVER=TRUE` - lets a second connection (the H2 console) attach while the app is already holding the file open. Without it, the console would fail with a "database may be already in use" lock error.
- `username=sa`, empty `password` - H2's default credentials.
- `spring.sql.init.mode=always` - tells Boot to run `schema.sql` (and `data.sql` if present) on every startup. The default `embedded` would also trigger it for H2, but `always` is explicit and is exactly what we will still need unchanged when we point at PostgreSQL in step 09.

### 3. Write the schema

Create `src/main/resources/schema.sql`. Spring Boot finds it on the classpath and runs it at startup. Every statement uses `CREATE TABLE IF NOT EXISTS`, so on the second boot the tables already exist and nothing is recreated - your edits survive. Here are two representative tables (the full file defines all eight):

```sql
CREATE TABLE IF NOT EXISTS prayer_times (
    id          INT PRIMARY KEY,
    fajr        VARCHAR(5) NOT NULL,
    sunrise     VARCHAR(5) NOT NULL,
    dhuhr       VARCHAR(5) NOT NULL,
    asr         VARCHAR(5) NOT NULL,
    maghrib     VARCHAR(5) NOT NULL,
    isha        VARCHAR(5) NOT NULL,
    method_note VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS weeks (
    id             BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    block_id       INT NOT NULL,
    ordinal        INT NOT NULL,
    name           VARCHAR(60)  NOT NULL,
    start_date     VARCHAR(30),
    end_date       VARCHAR(30),
    training_focus VARCHAR(500),
    backend_focus  VARCHAR(500),
    deload         BOOLEAN NOT NULL DEFAULT FALSE
);
```

Note `prayer_times` has a fixed `id INT PRIMARY KEY` (it is always row `1`), while child/list tables like `weeks` use `BIGINT GENERATED BY DEFAULT AS IDENTITY` so the database assigns ids. Times are stored as short strings (`VARCHAR(5)`, e.g. `"04:37"`) because that is exactly how the routine is edited and displayed - no parsing round-trip.

### 4. Write the repositories

A repository takes a `JdbcTemplate` in its constructor, defines a `RowMapper`, and exposes domain-shaped methods. We'll meet three shapes, smallest first: a single-row repository, a list repository, and a parent-with-children repository. Once you see the pattern, the other four repositories are variations on it.

#### 4a. A single-row repository (`PrayerTimesRepository`)

The simplest repository reads and writes the single row `id = 1`:

```java
@Repository
public class PrayerTimesRepository {

    private static final RowMapper<PrayerTimes> MAPPER = (rs, rowNum) -> new PrayerTimes(
            rs.getString("fajr"),
            rs.getString("sunrise"),
            rs.getString("dhuhr"),
            rs.getString("asr"),
            rs.getString("maghrib"),
            rs.getString("isha"),
            rs.getString("method_note"));

    private final JdbcTemplate jdbc;

    public PrayerTimesRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<PrayerTimes> find() {
        return jdbc.query("SELECT * FROM prayer_times WHERE id = 1", MAPPER).stream().findFirst();
    }

    public void update(PrayerTimes p) {
        jdbc.update("""
                UPDATE prayer_times
                   SET fajr = ?, sunrise = ?, dhuhr = ?, asr = ?, maghrib = ?, isha = ?, method_note = ?
                 WHERE id = 1
                """,
                p.fajr(), p.sunrise(), p.dhuhr(), p.asr(), p.maghrib(), p.isha(), p.methodNote());
    }
}
```

What is happening:

- `@Repository` registers this as a Spring bean (and marks it as a data-access component, which also enables Spring's translation of low-level SQL exceptions into its `DataAccessException` hierarchy).
- The `RowMapper` is a lambda matching the `RowMapper<T>` functional interface: given a `ResultSet rs` positioned on a row and the `rowNum`, build one `PrayerTimes`. Because `PrayerTimes` is a record, you just pass the columns to its canonical constructor in order. The column name `method_note` (snake_case in SQL) maps to the record component `methodNote()` (camelCase in Java) by hand - `JdbcTemplate` does no magic name conversion.
- `find()` returns `Optional<PrayerTimes>`: there might be zero rows on a brand-new database before seeding, so we return an `Optional` rather than risk a null or an exception.
- The seven `?` in `update` bind in argument order. The `?, ?, ?` style is the safe pattern; the values come last.

#### 4b. A list repository (`ScheduleRepository`)

Most tables are lists. `ScheduleRepository` shows the read-and-seed shape:

```java
@Repository
public class ScheduleRepository {

    private static final RowMapper<ScheduleItem> MAPPER = (rs, rowNum) -> new ScheduleItem(
            rs.getString("slot_time"),
            rs.getString("title"),
            rs.getString("detail"),
            rs.getString("category"),
            rs.getString("day_type"));

    public List<ScheduleItem> findAll() {
        return jdbc.query("SELECT * FROM schedule_items ORDER BY sort_order, slot_time", MAPPER);
    }
    // insertAll(...) numbers sort_order by position so seed order is preserved
}
```

The `ORDER BY sort_order` matters: rows in a table have no inherent order, so we store an explicit `sort_order` column and sort by it to keep the daily timeline in the right sequence.

#### 4c. A parent-with-children repository (`BlockRepository`)

The most interesting repository spans **two** tables. A `BlockPlan` is a parent `blocks` row plus ordered `weeks` children. `BlockRepository.find()` reads both:

```java
public BlockPlan find() {
    String label = jdbc.queryForObject("SELECT label FROM blocks WHERE id = 1", String.class);
    List<Week> weeks = jdbc.query(
            "SELECT * FROM weeks WHERE block_id = 1 ORDER BY ordinal", WEEK_MAPPER);
    return new BlockPlan(label, weeks);
}
```

`queryForObject(sql, String.class)` is for "exactly one scalar". Replacing the block is the classic aggregate-update pattern - update the parent, delete the old children, insert the new ones:

```java
public void replace(BlockPlan block) {
    jdbc.update("UPDATE blocks SET label = ? WHERE id = 1", block.label());
    jdbc.update("DELETE FROM weeks WHERE block_id = 1");
    insertWeeks(block);
}
```

Those are three separate statements. If the app crashed between the `DELETE` and the re-insert, the block would be left with no weeks - so the caller must run them in one transaction (step 6 below). The other repositories (`MetaRepository`, `GridRepository`, `SupplementRepository`, `DietRepository`) follow the same mould; `MetaRepository` even defines a tiny `record Meta(String title, String tagline, String month)` inside itself because only that layer needs it.

### 5. Rewire `RoutineService` - the payoff

This is the heart of the step. The service keeps the same public methods; only the internals change. The in-memory field is gone; it now holds the seven repositories:

```java
@Service
public class RoutineService {

    private final MetaRepository meta;
    private final PrayerTimesRepository prayerTimes;
    private final BlockRepository blocks;
    private final ScheduleRepository schedule;
    private final GridRepository grid;
    private final SupplementRepository supplements;
    private final DietRepository diet;

    public RoutineService(MetaRepository meta, PrayerTimesRepository prayerTimes, BlockRepository blocks,
                          ScheduleRepository schedule, GridRepository grid,
                          SupplementRepository supplements, DietRepository diet) {
        this.meta = meta;
        this.prayerTimes = prayerTimes;
        this.blocks = blocks;
        this.schedule = schedule;
        this.grid = grid;
        this.supplements = supplements;
        this.diet = diet;
    }

    public RoutineConfig getConfig() {
        MetaRepository.Meta m = meta.find().orElseThrow(() -> new IllegalStateException("app_meta not seeded"));
        PrayerTimes pt = prayerTimes.find().orElseThrow(() -> new IllegalStateException("prayer_times not seeded"));
        BlockPlan block = blocks.find();

        // Reference content is not in the database - read it straight from the seed.
        RoutineConfig reference = RoutineSeed.defaultConfig();

        return new RoutineConfig(
                m.title(), m.tagline(), m.month(),
                pt,
                block,
                grid.findAll(),
                schedule.findAll(),
                supplements.findAll(),
                diet.findAll(),
                reference.journal(),
                reference.threeRules(),
                reference.weekend(),
                reference.notes());
    }

    public RoutineConfig updatePrayerTimes(PrayerTimes prayerTimes) {
        this.prayerTimes.update(prayerTimes);
        return getConfig();
    }
    // updateMonth(...) calls meta.updateMonth(...) the same way
}
```

Two teaching points are baked into this code:

- **Not everything has to live in a table.** `getConfig()` assembles most of the config from the database, then bolts on the reference content (journal prompts, the three rules, the weekend protocol, the sleep/deload notes) by reading `RoutineSeed.defaultConfig()`. That content is static text that never changes, so it stays in `RoutineSeed` and is merged into the response by the service.
- **Transactions for multi-statement writes.** Replacing the block touches two tables, so the service marks that one method `@Transactional`:

```java
@Transactional
public RoutineConfig updateBlock(BlockPlan block) {
    blocks.replace(block);
    return getConfig();
}
```

As the Javadoc puts it: _"if anything throws halfway, the delete-and-reinsert is rolled back, so a reader can never catch the block with half its weeks missing."_ Spring opens a **transaction** (a unit of work the database treats as all-or-nothing — see [Transactions & ACID](../theory/transactions-and-acid.md)) around the method, commits on normal return, and rolls back on a runtime exception. The "all-or-nothing" guarantee is the **A**(tomicity) in ACID.

Notice the controllers are not mentioned anywhere in this step - because they did not change. That is the entire reward for layering.

### 6. Seed an empty database on first run

A fresh `./data/sahar.mv.db` has empty tables (the `schema.sql` only creates structure). `DataSeeder` is an `ApplicationRunner`, a Spring Boot hook whose `run` method fires once, right after the context starts and `schema.sql` has run:

```java
@Component
public class DataSeeder implements ApplicationRunner {

    // ...seven repositories injected via the constructor...

    @Override
    public void run(ApplicationArguments args) {
        if (meta.find().isPresent()) {
            return; // already seeded - leave existing data (and any edits) untouched
        }

        RoutineConfig c = RoutineSeed.defaultConfig();
        meta.insert(c.title(), c.tagline(), c.month());
        prayerTimes.insert(c.prayerTimes());
        block.insert(c.block());
        grid.insertAll(c.weeklyGrid());
        schedule.insertAll(c.schedule());
        supplements.insertAll(c.supplements());
        diet.insertAll(c.diet());
    }
}
```

The guard `if (meta.find().isPresent()) return;` is the whole trick behind "edit a value, restart, it persists". On the first run the tables are blank, so we copy the seed in. On every later run the data (including your edits, now rows) is already there, so we skip. The Javadoc flags the future: _"Step 10 retires this class: Flyway will seed the data as a versioned migration instead."_ (See [./07-full-crud.md](./07-full-crud.md) and onward.)

### 7. Keep tests isolated with an in-memory database

> [!IMPORTANT]
> Tests must never touch your real `./data/sahar` file.

Add `src/test/resources/application.properties`, which Boot uses instead of the main one when tests run:

```properties
spring.datasource.url=jdbc:h2:mem:sahar-test;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.sql.init.mode=always
```

`jdbc:h2:mem:sahar-test` is an **in-memory** H2 - a throwaway database that exists only for the test JVM. `DB_CLOSE_DELAY=-1` keeps it alive for the whole JVM (an in-memory H2 normally vanishes the instant its last connection closes, which would drop your tables mid-test). Because `schema.sql` runs here too and `DataSeeder` fills it, the `contextLoads` test exercises the real startup path end to end - just against disposable storage.

## ✅ End state

Sahar now persists. Run the app, open the read-only page at `/`, edit a prayer time through the admin page at `/admin.html` (or `PUT /api/prayer-times`), **stop the app, start it again** - and the new time is still there. The data lives in `./data/sahar.mv.db`. This is verified to work.

While the app is running you can inspect the raw rows in the H2 web console at [http://localhost:8080/h2-console](http://localhost:8080/h2-console). Log in with JDBC URL `jdbc:h2:file:./data/sahar`, user `sa`, no password, and run `SELECT * FROM prayer_times;` to see your edit as a real database row.

Files added or changed in this step:

- `src/main/resources/application.properties` - data source, `spring.sql.init.mode=always`, H2 console enabled.
- `src/main/resources/schema.sql` - the eight tables.
- `src/main/java/com/ramishtaha/sahar/repo/` - new package: `MetaRepository`, `PrayerTimesRepository`, `BlockRepository`, `ScheduleRepository`, `GridRepository`, `SupplementRepository`, `DietRepository`.
- `src/main/java/com/ramishtaha/sahar/service/RoutineService.java` - rewired from in-memory field to repositories.
- `src/main/java/com/ramishtaha/sahar/seed/DataSeeder.java` - new `ApplicationRunner` that seeds an empty DB.
- `src/test/resources/application.properties` - in-memory H2 for isolated tests.
- `pom.xml` - `spring-boot-starter-jdbc`, `h2`, `spring-boot-h2console`.
- **Unchanged:** every controller in the web layer, the domain records, and `RoutineSeed`.

Full sources: [step-06-jdbctemplate-h2](../../checkpoints/step-06-jdbctemplate-h2/).

## 💼 Interview angle

**Q: What is a connection pool and why do applications use one?**
A: Opening a real DB connection is expensive (TCP handshake, auth, session setup) — far too slow per request.
A pool keeps a small set of connections already open; code borrows one, runs its query, and returns it. Spring
Boot auto-configures HikariCP from `spring.datasource.*`.

**Q: How does `JdbcTemplate` protect against SQL injection?**
A: It uses **bind parameters** — the `?` placeholders. The driver sends the SQL template and the values
separately, so a value can never be parsed as SQL. String-concatenating user input into the SQL is the hole;
`update("... = ?", value)` closes it.

**Q: What does a `RowMapper<T>` do, and what must line up for it to work?**
A: It maps one `ResultSet` row to one object — for a record, you read each column and pass it to the canonical
constructor. The column names in the mapper must match `schema.sql` exactly; `JdbcTemplate` does no automatic
snake_case → camelCase conversion, so `method_note` is read by hand into `methodNote()`.

**Q: Why is `updateBlock` annotated `@Transactional` but `updatePrayerTimes` is not?**
A: Replacing the block is delete-then-reinsert across two tables — multiple statements. `@Transactional` makes
them atomic, so a mid-operation failure rolls back and no reader sees a block with half its weeks. Updating
prayer times is a single `UPDATE`, already atomic on its own.

**Q: The controllers didn't change at all this step — why is that a good sign?**
A: Because only the data layer absorbed the change. The repository is the single place that knows SQL, the
service kept its method signatures, and the web layer never saw a `ResultSet`. That clean boundary is exactly
what makes the PostgreSQL swap in step 09 a wiring change, not a rewrite.

**Q: Why `JdbcTemplate` here instead of JPA/Hibernate?**
A: For a small, schema-owned app, `JdbcTemplate` keeps the SQL explicit and the mental model tiny — you see
every query. JPA adds an ORM, entity lifecycle, and lazy-loading semantics that are powerful but heavier than
this app needs. (Full reasoning: [jdbc-vs-jpa](../theory/jdbc-vs-jpa.md).)

## 🐞 Common mistakes and how to debug them

- **`Database may be already in use` when opening the H2 console.** The app already holds the file lock. The fix is `AUTO_SERVER=TRUE` in the JDBC URL, which lets a second process attach. Make sure the console's URL matches the app's exactly (`jdbc:h2:file:./data/sahar`).
- **Your edits vanish on restart anyway.** You are probably on an in-memory URL (`jdbc:h2:mem:...`) instead of file mode (`jdbc:h2:file:./data/sahar`). In-memory is correct for tests, fatal for the real app. Check `src/main/resources/application.properties`.
- **`Table "PRAYER_TIMES" not found` at startup.** `schema.sql` did not run. Confirm it is in `src/main/resources/` and that `spring.sql.init.mode=always` is set. The exception fires the first time a repository queries the missing table.
- **`IncorrectResultSizeDataAccessException` from `queryForObject`.** It expects exactly one row and got zero (or many). On a fresh, unseeded database `blocks` is empty - the `DataSeeder` must run first. This is also why `find()` methods that may return nothing use `query(...).stream().findFirst()` and return `Optional`, not `queryForObject`.
- **`IllegalStateException: app_meta not seeded` from `getConfig()`.** The database is empty and the seeder did not populate it. Check that `DataSeeder` is a `@Component` (so Spring discovers it) and that its guard did not short-circuit against stale rows from a previous schema.
- **A `RowMapper` reads the wrong column.** `JdbcTemplate` will not warn you - `rs.getString("method_note")` must match the SQL column name exactly. A typo gives an "invalid column name" SQL error; a wrong-but-real name silently maps the wrong data. Keep the mapper and the `CREATE TABLE` side by side.
- **Editing the block leaves it with no weeks after a failure.** The `replace` is delete-then-insert. Without `@Transactional` on `updateBlock`, a mid-operation failure leaves a half-written block. The annotation makes it atomic.

## ❓ Check yourself

1. Why does H2 in **file** mode (`jdbc:h2:file:`) make edits survive a restart, while in-memory mode (`jdbc:h2:mem:`) does not - and why do the tests deliberately use in-memory?
2. What two things does a `RowMapper<T>` connect, and why must the column names in the mapper match `schema.sql` exactly?
3. Explain why `jdbc.update("... = ?", value)` is safe but `"... = '" + value + "'"` is a SQL-injection hole.
4. The controllers did not change at all this step. Which layer absorbed all the change, and why is that a sign the architecture is working?
5. Why is `updateBlock` annotated `@Transactional` when `updatePrayerTimes` is not?
6. The journal prompts and three rules are not in any table. Where do they come from in `getConfig()`, and what is the reasoning for keeping them out of the database?

---

⬅️ Prev: [05 - Validation and rules](./05-validation-and-rules.md) · ➡️ Next: [07 - Full CRUD](./07-full-crud.md) · 📍 Checkpoint: [step-06-jdbctemplate-h2](../../checkpoints/step-06-jdbctemplate-h2/) · 🔗 See also: [SQL/JDBC cheatsheet](../../reference/cheatsheet-sql-jdbc.md) · [Transactions & ACID](../theory/transactions-and-acid.md) · [Interview-prep](../../reference/interview-prep.md)
