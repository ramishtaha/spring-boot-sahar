# ⌨️ The command line

*The command line is a place where you type one instruction at a time and press Enter to run it.*

Why care? Almost every tool in this course (including starting the Sahar app) is launched by typing one short line. Once you know a handful of commands, the scary black window becomes friendly.

## 🧱 What it really is

The **command line** (also called the **terminal** or the **shell**) is a text-based way to talk to your computer. Instead of clicking icons, you type a **command** — a short instruction — and press Enter. The computer does the thing, then prints some text back.

That is the whole loop: type, press Enter, read the result. It looks technical, but it is just typing.

> [!TIP]
> You can copy a command from these docs and paste it straight into your terminal. You do not have to type it by hand, and you do not need to memorise any of these commands.

## 🚪 How to open it

You already have a terminal installed. Pick your system:

- **Windows**: open **Windows Terminal** (or **PowerShell**) from the Start menu.
- **macOS**: open the **Terminal** app (find it with Spotlight: press Cmd+Space, type "Terminal").
- **Linux**: open your **Terminal** app from the menu, or press Ctrl+Alt+T.

When it opens you will see a blinking cursor waiting for you. That is normal.

## 📍 Where am I? The current directory

A **directory** is just another word for a **folder**. At any moment your terminal is "standing inside" one folder — this is called the **current directory**. Commands act on this folder unless you say otherwise.

A few commands to find your feet:

- See which folder you are in:

```
pwd
```

(That stands for "print working directory". It works on macOS and Linux. On Windows, `cd` with nothing after it shows the same thing.)

- Move into a folder named `app`:

```
cd app
```

(`cd` means "change directory". To go back up one level, type `cd ..`.)

- List the files in the current folder:

```
ls
```

(`ls` works on macOS and Linux. On Windows use `dir`.)

## ▶️ Running and stopping a program

To run a program, you type its name (and sometimes extra instructions) and press Enter. To **stop** a program that is currently running, press **Ctrl+C**. That key combo tells the program "please stop now".

One more time-saver: press the **up arrow** key to bring back the last command you typed, so you can run it again without retyping. Press it more times to step further back through your history.

## 🤔 Decoding `./mvnw spring-boot:run`

In this course you will start the Sahar app (a personal prayer, training, and nutrition routine web app) by typing something like:

```
./mvnw spring-boot:run
```

Let's break it apart, piece by piece:

- `./mvnw` means "run the program called `mvnw` that lives **right here** in the current folder". The `./` part just means "here".
- `spring-boot:run` is the **instruction** you are handing to that program. Here it says "start the Spring Boot app".

On **Windows** the same line looks slightly different:

```
.\mvnw.cmd spring-boot:run
```

Same idea — only the slash direction and the `.cmd` ending change.

## 📜 Output and errors are just text

When a program runs, it prints text to tell you what it is doing. That stream of text is called **output**. It is nothing magical — just the program talking.

If something goes wrong, you get an **error**, which is *also* just text. Do not panic at a wall of red.

> [!NOTE]
> When you hit an error, read the **first few lines**, not the last ones. The top of the message usually says what actually went wrong.

## ✅ Recap

- The command line lets you run programs by typing one line and pressing Enter.
- You always sit inside a **current directory** (folder); `pwd`/`cd`/`ls` (or `dir`) help you look around and move.
- Ctrl+C stops a running program, and the up arrow repeats your last command.
- A line like `./mvnw spring-boot:run` is just "run this local program, with this instruction" — and output and errors are both plain text you can read.

⬅️ Prev: [Browser & JavaScript basics](./browser-javascript-basics.md) · Next: [Java 1 — your first program](./java-1-hello-world.md) · Go deeper: the [Maven cheatsheet](../../reference/cheatsheet-maven.md)
