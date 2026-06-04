# 🌱 What is Spring Boot?

_Spring Boot is a toolkit that hands you a ready-made web app skeleton, so you only write the parts that make your app special._

Why care? Because with it, a handful of short notes in your code turn into a real, running web application. That is a wonderful feeling, and you'll have it by step 02.

> [!NOTE]
> This course uses Spring Boot 4 (many older tutorials online are still on Boot 3.x). If you ever follow along elsewhere, see [what changed from 3.x → 4](../../reference/cheatsheet-version-deltas.md).

## 🧰 First, what is a "framework"?

A **framework** is prewritten structure and machinery you build on top of. Think of it like a kitchen that already has a stove, sink, and counters installed. You don't build the kitchen; you just cook your own dish.

Every web app needs the same boring "plumbing": listening for web requests, sending replies, wiring objects together. A framework provides that plumbing once. You write only the unique part, like Sahar's logic for tracking your prayer, training, and nutrition routine.

> [!NOTE]
> You don't need to memorise any of this. We're just building intuition. The course re-teaches each idea later, in context.

## 🚀 What Spring Boot gives you

Spring Boot is a popular Java framework. Three gifts stand out:

1. **An embedded web server.** A "server" is the program that listens for requests from a browser and sends back replies. Normally you'd install one separately. Spring Boot has one built in, so your app can answer web requests all by itself.
2. **Auto-configuration.** Spring Boot looks at which libraries (prewritten code packages) you've added and picks sensible default settings for you. So you configure almost nothing to get started.
3. **Dependency injection.** Spring builds your objects for you and hands them to whoever needs them. More on this next, gently.

## 🧩 Beans and dependency injection

Normally in Java you create an object yourself with the keyword `new`:

```java
NutritionService service = new NutritionService();
```

With Spring, you don't. Spring creates that object once and keeps it ready. An object that Spring manages is called a **bean** (just a nickname, nothing fancy).

When one part of your app needs a bean, it simply *declares* that it needs it, and Spring passes it in. This handing-over is called **dependency injection**. For example, a **controller** (the class that handles incoming web requests) just asks for a service in its **constructor** (the method that sets up a new object), and Spring supplies it:

```java
@RestController
class TrainingController {
    private final TrainingService service;

    TrainingController(TrainingService service) { // no "new"!
        this.service = service;
    }
}
```

Notice: no `new` anywhere. You ask; Spring delivers. Less wiring, fewer mistakes.

## 📦 Starters: bundles that save you time

A **starter** is a convenient bundle of related libraries grouped under one name. Instead of hunting for ten separate pieces, you add one starter and get them all.

For example, `spring-boot-starter-webmvc` pulls in everything needed to build a web API (the part of your app that browsers and other programs talk to). One line in, and you're ready to handle requests.

## ⚡ Why it feels so productive

A few **annotations** (short notes starting with `@`, like `@RestController`, that tell Spring how to treat your code) plus a starter, and you have a running web API. No manual server setup, no endless config files.

That is exactly the feeling you'll get in step 02, when your first Sahar endpoint answers a real request.

> [!TIP]
> Versions: We use Spring Boot 4.0.6, which needs Java 17 or newer. This course uses Java 25, so you're well covered.

## ✅ Recap

- A **framework** gives you the plumbing so you write only your app's unique parts.
- Spring Boot adds an **embedded server**, **auto-configuration**, and **dependency injection**.
- **Beans** are objects Spring creates and hands to whoever needs them, no `new` required.
- **Starters** bundle the libraries you need, and a few **annotations** turn into a running web API.

---

⬅️ Prev: [Foundations overview](./README.md) · Next: [HTTP in five minutes](./http-basics.md) · Go deeper: [Spring, servlets & DI](../theory/spring-and-di.md)
