# 🔢 Java 2 — values & logic

_How Java stores values, makes choices, and repeats work._

Why care: almost every line you write in the Sahar app stores a value, checks a condition, or loops over a list. Get these four ideas and you can read most code.

## 🏷️ Variables have a type

A **variable** is a named box that holds a value. In Java, every variable has a **type** — a label that says what kind of value lives in the box.

Java is **statically typed**: the **compiler** (the program that turns your code into something runnable) checks the types *before* the program runs. If you put text where a number belongs, it stops you early — that catches whole classes of bugs before your app ever starts.

The four everyday types:

- `int` — a whole number, like `4`
- `double` — a decimal number, like `1.5`
- `boolean` — `true` or `false`
- `String` — text, written in double quotes

You **declare** a variable (give it a type and name) and **assign** it a value:

```java
String fajr = "04:37";
int weeks = 4;
boolean deload = true;
```

When the type is obvious from the value, you can write `var` and let Java figure it out:

```java
var weeks = 4;   // Java knows this is an int
```

> [!TIP]
> You don't need to memorise the types. You'll see them so often they'll stick on their own.

## 🔀 Making decisions: if / else

To do something only *when* a condition is true, use `if`. Add `else` for the other case:

```java
if (deload) {
    System.out.println("Take it easy this week");
} else {
    System.out.println("Full training week");
}
```

The condition inside `( )` must be a `boolean` — true or false. You build conditions with these operators: `==` (equal), `!=` (not equal), `<` (less than), `>` (greater than), `&&` (and), `||` (or).

```java
if (weeks > 3 && deload) { /* ... */ }
```

> [!NOTE]
> `=` *assigns* a value; `==` *compares* two values. Easy to mix up at first — that's normal.

## 🔁 Repeating: loops

When you have several values to handle, loop over them. The friendliest loop is the **for-each**, which visits each item in turn:

```java
for (String time : times) {
    System.out.println(time);
}
```

Read it as: "for each `String` called `time` in `times`, do this." There are also the classic `for` loop (count a fixed number of times) and `while` loop (repeat while a condition holds) — you'll meet those later.

## 📋 Lists hold many values

A `List<String>` is a container that holds many values of one type — here, many `String`s. The `<String>` part says what kind of item it holds.

```java
List<String> times = List.of("04:37", "13:10", "19:55");
for (String time : times) {
    System.out.println(time);
}
```

## 🧩 Tiny combined example

Here's everything together: a list of prayer times from the Sahar app, printed one per line, with a friendly note for the first one.

```java
List<String> prayerTimes = List.of("04:37", "13:10", "19:55");
for (String t : prayerTimes) {
    boolean isFajr = t.equals("04:37");
    System.out.println(isFajr ? "Fajr: " + t : t);
}
```

If you can read that, you've already got the core of everyday Java.

## ✅ Recap

- A **variable** holds a value, and its **type** (`int`, `double`, `boolean`, `String`) is checked by the compiler before you run.
- `if` / `else` make decisions using comparison operators like `==`, `!=`, `<`, `>`, `&&`, `||`.
- A **for-each** loop visits each item in a collection; classic `for` and `while` loops also exist.
- A `List<String>` holds many values of one type, and you can loop over it.

⬅️ Prev: [Java 1 — your first program](./java-1-hello-world.md) · Next: [Java 3 — classes, objects & records](./java-3-classes-and-objects.md) · Go deeper: the [Modern Java refresher](../../reference/java-refresher.md)
