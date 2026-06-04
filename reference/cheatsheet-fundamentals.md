# JVM fundamentals: classpath, JAR, fat jar, BOM, servlet, LTS

> *The handful of words every Sahar step quietly assumes you already know — defined once, plainly, so nothing trips you up.*

This is a terse lookup, not a tutorial. Each entry is one plain definition plus one sentence of why it matters in Sahar. These terms recur from [step 00 - baseline](../docs/steps/00-baseline.md), [step 01 - serve static](../docs/steps/01-serve-static.md), [step 02 - first REST endpoint](../docs/steps/02-first-rest-endpoint.md), [step 11 - dockerize](../docs/steps/11-dockerize.md), and [what is Maven](../docs/foundations/what-is-maven.md).

> [!NOTE]
> Version story for Sahar: the **older** stack is Spring Boot 3.x / Spring 5–6 / Java 17 / the `javax.*` namespace / Jackson 2; the **newer** stack Sahar targets is Spring Boot 4.0.6 / Spring Framework 7 / Java 25 (LTS) / the `jakarta.*` namespace / Jackson 3. The `javax.*` → `jakarta.*` rename (so `jakarta.servlet`, `jakarta.validation`) landed in the Boot 2→3 / Spring 5→6 jump; Boot 4 then moved to Java 17+ baseline, Spring 7, and Jackson 3. When a tutorial uses `javax.servlet` or `spring-boot-starter-web`, it predates Sahar's stack.

---

## ☕ Runtime and compilation

**Classfile / bytecode**
A `.class` file holds **bytecode** — the compact, platform-neutral instruction set the JVM executes, produced by `javac` from your `.java` source. *Why in Sahar:* Maven compiles `src/main/java` into `target/classes/**.class`; you ship bytecode, not source, and the JVM (not your OS) runs it.

**JRE vs JDK**
The **JRE** (Java Runtime Environment) is just enough to *run* bytecode (JVM + core libraries); the **JDK** (Java Development Kit) is the JRE *plus* the tools to *build* it (`javac`, `jar`, debugger). *Why in Sahar:* you build with a full JDK, but Sahar's multi-stage `Dockerfile` runs the final image on a slim JRE to keep it small (see [step 11](../docs/steps/11-dockerize.md)).

**LTS (Long-Term Support)**
A release line that gets years of updates and fixes, versus short-lived feature releases that age out in months. *Why in Sahar:* Sahar pins **Java 25**, an LTS, so the toolchain stays supported and reproducible — set via `<java.version>25</java.version>` in [`pom.xml`](../checkpoints/step-00-baseline/pom.xml).

---

## 📦 Packaging and the classpath

**Classpath**
The ordered list of places (JARs and compiled-class folders) where the JVM looks up classes at load time. *Why in Sahar:* Spring Boot's auto-configuration reacts to *what is on the classpath* — adding `spring-boot-starter-webmvc` puts embedded Tomcat there, which is why a web server appears with no extra code.

**JAR**
A `.jar` (Java ARchive) is a ZIP of compiled `.class` files plus metadata (`META-INF/MANIFEST.MF`); it is how Java libraries and apps are distributed. *Why in Sahar:* every dependency Maven downloads is a JAR, and Sahar itself is built into one.

**Fat jar (uber jar / executable jar)**
A single self-contained `.jar` bundling your classes, *all* dependencies, *and* an embedded web server, so `java -jar sahar.jar` runs the whole app with nothing else installed. The Spring Boot Maven plugin's **`repackage`** goal (bound to the `package` phase) rewrites the plain jar into this form. *Why in Sahar:* it is Sahar's single deployable unit — the artifact the `Dockerfile` copies and runs (see [the Maven cheatsheet](cheatsheet-maven.md) and [step 11](../docs/steps/11-dockerize.md)).

**BOM (Bill of Materials)**
A special Maven POM that pins a curated, mutually-tested table of dependency versions so you don't hand-pick them. *Why in Sahar:* inheriting `spring-boot-starter-parent` (4.0.6) brings the Spring Boot dependencies BOM, which is why almost every `<dependency>` in [`pom.xml`](../checkpoints/step-00-baseline/pom.xml) can **omit `<version>`** — the BOM already supplies a compatible one.

---

## 🌐 The servlet world

**Servlet**
The Java standard (`jakarta.servlet`, formerly `javax.servlet`) for a component that handles HTTP requests inside a web server. *Why in Sahar:* Spring MVC's `DispatcherServlet` is one servlet that fronts your whole app, routing each request to the right controller — which is why the blocking web starter is `spring-boot-starter-webmvc` (servlet stack), not `webflux` (reactive).

**Servlet container (Tomcat)**
The web server that hosts and runs servlets, managing the HTTP socket, threads, and the request lifecycle; **Tomcat** is the one Spring Boot embeds by default. *Why in Sahar:* `spring-boot-starter-webmvc` pulls embedded Tomcat *inside* the fat jar, so Sahar contains its own server rather than being deployed into an external one.

**`.war` vs `.jar`**
A **`.war`** (Web ARchive) is the old model — you package just your app and deploy it *into* a separately-installed servlet container; a **`.jar`** (Sahar's executable fat jar) bundles the container *in*, so it runs standalone. *Why in Sahar:* Sahar is a `.jar` you launch with `java -jar`, the modern Spring Boot default — no external Tomcat to install or manage.

---

## 🌱 The Spring runtime

**The context (ApplicationContext)**
Spring's runtime container: the object that creates every bean, knows their dependencies, and wires them together at startup. *Why in Sahar:* when `SaharApplication.main` runs, Spring builds one `ApplicationContext`, discovers the controllers, `RoutineService`, and repositories, and connects them — more in the [glossary](glossary.md) and [Spring and DI](../docs/theory/spring-and-di.md).

---

Reference: [Maven](cheatsheet-maven.md) · [Version deltas](cheatsheet-version-deltas.md) · [Glossary](glossary.md) · [Java refresher](java-refresher.md) · [README](../README.md)
