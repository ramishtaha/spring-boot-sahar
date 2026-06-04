# 🧱 Java 3 — classes, objects & records

*A class is a blueprint; an object is a real thing built from it — and records make data objects in one line.*

Why care: almost everything in Sahar (our personal prayer/training/nutrition app) is built from small classes and records. Once these click, the rest of the code stops looking like magic.

> [!TIP]
> You don't need to memorise any of this. Read it once, then come back when the code in front of you uses it.

## 🏗️ Class vs object

A **class** is a blueprint — a description of something. An **object** is one real thing made from that blueprint. You build an object with the keyword `new`.

Think of a class as the recipe and an object as the actual cake you baked from it. One recipe, many cakes.

```java
class Greeter {
    String name;                 // a field: data this object holds

    void sayHello() {            // a method: something it can do
        System.out.println("Hello, " + name);
    }
}
```

### 🧩 Fields and methods

A **field** is a piece of data stored inside the object (here, `name`). A **method** is an action the object can perform (here, `sayHello`).

```java
Greeter g = new Greeter();       // make an object with new
g.name = "Sahar";                // set its field
g.sayHello();                    // call its method -> prints "Hello, Sahar"
```

## ⚙️ Methods: data in, result out

A **method** can take **parameters** (values you pass in) and give back a **return type** (the kind of value it hands back). `void` means it returns nothing.

```java
int add(int a, int b) {          // takes two ints, returns an int
    return a + b;
}
```

```java
int total = add(2, 3);           // total is now 5
```

You read it left to right: `int` is what comes out, `add` is the name, and `(int a, int b)` are the values going in.

## 📦 Records: data in one line

A **record** is a tiny, fixed data holder. You write one line and Java fills in the boring parts for you. Sahar's domain (its core data, like prayer times) is built almost entirely from records.

```java
record PrayerTimes(String fajr, String dhuhr) {}
```

> [!NOTE]
> Records are a modern Java feature (standard since Java 16), so they're a normal part of everyday code on Java 25 — no extra setup needed.

That single line automatically gives you:
- a **constructor** (the thing you call with `new` to build it),
- **getters** (methods to read each value, named `fajr()` and `dhuhr()`).

```java
PrayerTimes pt = new PrayerTimes("05:12", "12:45");
System.out.println(pt.fajr());   // prints 05:12
```

### 🔒 Why records are immutable

Records are **immutable** — once created, their values never change. That makes them safe and predictable, which is exactly what you want for data.

> [!NOTE]
> "Immutable" just means "cannot be changed after it's made." To get different values, you make a new record.

## 🕳️ null and Optional

`null` means "no value here" — and forgetting that something might be `null` is one of the most common causes of crashes in Java. `Optional` is a wrapper that makes "this might be absent" obvious, so you remember to handle the empty case.

```java
Optional<String> maybeName = Optional.of("Sahar");
```

## 🚨 Exceptions and reading errors

When something goes wrong, Java **throws an exception** — it stops and prints a **stack trace**, a list of where the error happened. Read the **top line** first (it says what broke and where), then look for any `Caused by:` line for the deeper reason.

```java
try {
    riskyThing();
} catch (Exception e) {          // catch handles the problem instead of crashing
    System.out.println("Handled: " + e.getMessage());
}
```

## 🏷️ Annotations

An **annotation** is a label starting with `@`, like `@Service`. It's just metadata — extra information a framework reads to decide what to do with your code. You'll soon meet `@RestController` and `@Service`. They are not magic, just tags.

## ✅ Recap

- A **class** is a blueprint; an **object** is built from it with `new`. Fields hold data, methods do things.
- A **method** takes parameters in and gives a return type out.
- A **record** is a one-line, **immutable** data holder that auto-generates its constructor and getters — Sahar's domain leans on these.
- `null`/`Optional`, exceptions/stack traces, and `@annotations` are tools you'll meet again in context.

⬅️ Prev: [Java 2 — types & logic](./java-2-types-and-logic.md) · Next: [What is Maven?](./what-is-maven.md) · Go deeper: the [Java refresher](../../reference/java-refresher.md) and [step 03](../steps/03-model-the-domain.md)
