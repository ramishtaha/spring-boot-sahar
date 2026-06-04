# 📦 What is Maven?

*Maven is the tool that turns your code and a list of ingredients into one runnable app.*

Why care? Because almost no real app is written from scratch. You reuse code other people wrote, and you need a reliable way to build everything together. Maven does that boring-but-important work for you.

## 🧩 The problem it solves

Real apps reuse lots of prewritten code. We call each piece of reusable code a **library**, and a library your project depends on is called a **dependency**.

On top of that, your own code has to be:
- **compiled** (turned from text into something the computer can run),
- **tested** (checked automatically that it still works), and
- **packaged** (bundled into one file you can ship and run).

Doing all of that by hand, in the right order, every time? Painful and easy to get wrong.

A **build tool** automates these steps. **Maven** is one such build tool. It reads one file, downloads your dependencies, compiles your code, runs your tests, and packages the result into a single runnable file called a **JAR** (Java ARchive). For our Sahar app (your personal prayer, training, and nutrition routine web app), Maven is what wraps everything into one thing you can start.

## 📄 The pom.xml in plain terms

Maven reads a single file named `pom.xml`. Think of it as your project's recipe card. It mainly holds three things.

**1. Coordinates** — your project's "address" so tools can identify it:

```xml
<groupId>win.l0ve</groupId>
<artifactId>sahar</artifactId>
<version>0.0.1</version>
```

- `groupId` = who/what org owns it.
- `artifactId` = the project's name.
- `version` = which release this is.

**2. A parent** — a shared base your project inherits from. Sahar uses Spring Boot's parent, which picks **compatible versions** of common libraries for you, so you don't have to:

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>4.0.6</version>
</parent>
```

**3. The dependencies list** — the libraries you want. Spring Boot bundles related libraries into handy packs called **starters**:

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
</dependencies>
```

> [!NOTE]
> Notice the starter has no `<version>`. The parent already chose a version that fits. That's one less thing for you to worry about.

## 🛠️ The Maven Wrapper (mvnw)

Here's a friendly surprise: you don't even need to install Maven.

Most projects include a tiny script called the **Maven Wrapper**, checked into the project itself. It's named `mvnw` (Mac/Linux) or `mvnw.cmd` (Windows). When you run it, it grabs the **exact right Maven version** for this project automatically.

So instead of `mvn ...`, you run `./mvnw ...` (on Windows: `mvnw.cmd ...`). Everyone on the team uses the same version, no setup needed.

## ⚡ The three commands you'll actually use

You only need these to get going:

```bash
./mvnw spring-boot:run   # start the Sahar app
./mvnw test              # run your tests
./mvnw package           # build the runnable JAR
```

> [!TIP]
> On Windows, swap `./mvnw` for `mvnw.cmd`. You don't need to memorise the rest of Maven's commands — these three carry you a long way.

## 😌 Don't worry about editing pom.xml

Early on, you'll rarely touch `pom.xml` by hand. The starter project comes with sensible defaults. When you do need a new library later, you'll add a small `<dependency>` block — and we'll walk through it together when that moment comes.

## ✅ Recap

- Maven is a **build tool**: it reads `pom.xml`, downloads dependencies, compiles, tests, and packages your app into a **JAR**.
- `pom.xml` holds your **coordinates**, a **parent** (Spring Boot picks compatible versions), and your **dependencies** (often **starters**).
- The **Maven Wrapper** (`mvnw` / `mvnw.cmd`) runs the right Maven version, so you don't install anything.
- Your three go-to commands: `spring-boot:run`, `test`, and `package`.

Next: [What is Spring Boot?](./what-is-spring-boot.md) · Go deeper: the [Maven cheatsheet](../../reference/cheatsheet-maven.md)
