# Browser JavaScript for backend learners: fetch, Promise, async/await, the DOM

*You write the server; this is just enough of the browser side to understand the little script that talks to it.*

Why care? In [step 08](../steps/08-editable-admin-ui.md) you'll meet a tiny `admin.js` file that lets a human edit Sahar in a browser. It only uses a handful of ideas. Learn these four — the DOM, `fetch`, Promises, `async/await` — and that file will read like plain English. Nothing here is heavy; relax and skim.

## 🌳 The DOM: the page as a tree

When the browser loads a page, it turns the HTML into a tree of objects that JavaScript can read and change. That tree is the **DOM** (Document Object Model) — "document" meaning the page, "object" meaning each tag becomes a little object you can poke at.

To grab one element, you ask the page for it by its `id`:

```javascript
const input = document.getElementById('fajr'); // the <input id="fajr">
input.value = '04:40';                          // change what's in the box
```

So the DOM is just *the page, as data*. Reading a value, writing a value, swapping out text — all of it is reading and changing objects in that tree.

> [!NOTE]
> A **value** in JS is anything you can store: a string, a number, an element from the DOM. `const` means "this name won't be reassigned" — close enough to Java's `final` for now.

## ⏳ A Promise: a value that arrives later

Some things finish instantly. Others — like asking a server for data — take time. JavaScript doesn't freeze the page while it waits. Instead it hands you a **Promise**: an object that stands in for a value that *will* be there later, once the slow thing finishes (it "resolves").

You don't need to wrestle Promises directly. You mostly just need to know the word, because the next tool — `await` — is how you wait for one politely.

## 🌐 fetch(): ask the server for something

`fetch()` is the browser's built-in way to send an HTTP request — the same request/response loop from [How a web app works](./how-a-web-app-works.md). Because the answer arrives later, **`fetch()` returns a Promise**.

```javascript
fetch('/api/config'); // sends a GET to /api/config, returns a Promise
```

That call kicks off the request and immediately hands back a Promise. The actual response shows up once the server answers.

## ✨ async / await: wait without the mess

Here's the magic word: **`await`** pauses the current function until a Promise resolves, then gives you the resolved value — so asynchronous code reads top-to-bottom like ordinary code. You can only use `await` inside a function marked **`async`** (it tells JS "this function does some waiting").

A four-line example using the same idea `admin.js` uses to load Sahar's config (the real file wraps `fetch` in a small helper, but the primitives are identical):

```javascript
async function load() {
  const resp = await fetch('/api/config'); // wait for the response
  const cfg = await resp.json();           // wait again: parse the JSON body
  document.getElementById('month').value = cfg.month; // put it on the page
}
```

Read it as: *ask the server, wait, read the data, show it.* No callbacks, no tangle.

> [!TIP]
> Interview nudge: if someone asks "what does `await` do?" — "it pauses an `async` function until the Promise resolves, then returns the value." One sentence is plenty here.

## 📦 Reading JSON back

The server answers Sahar with JSON (that tidy text format from [HTTP basics](./http-basics.md)). The response object doesn't hand you the data directly — reading the body is *itself* slow, so it's another Promise:

```javascript
const cfg = await resp.json(); // resp.json() returns a Promise → await it
```

After that line, `cfg` is a normal JavaScript object: `cfg.month`, `cfg.prayerTimes.fajr`, and so on. Now you can read its fields and drop them onto the page.

## 🔄 Updating the page after data arrives

This is the whole point of the round trip. Once the data is in hand, you change the DOM so the human sees it:

```javascript
const cfg = await resp.json();
document.getElementById('month').value = cfg.month; // page now shows the value
```

The same idea runs in reverse when you save: read what the user typed out of the inputs, `fetch` it to the server with a method like `PUT`, and `await` the response to know whether it worked.

## 🍞 Two words you'll see in step 08

- **Toast** — a small message that pops up briefly and then fades away on its own (like toast popping up), e.g. "Prayer times saved ✓". Just feedback; it doesn't block anything.
- **Optimistic UI** — showing the user a result *before* the server has fully confirmed, then reconciling with whatever the server actually returns. Sahar's version is gentle and honest: it shows a toast on success but surfaces the server's real error if the save was rejected.

## ✅ Recap

- The **DOM** is the page as a tree of objects; `document.getElementById('fajr')` grabs one.
- A **Promise** is a value that arrives later.
- **`fetch()`** sends an HTTP request and returns a Promise.
- **`await`** (inside an **`async`** function) pauses until a Promise resolves and gives you the value.
- **`await resp.json()`** parses the JSON body into a normal object you can read.
- Then you **update the DOM** so the human sees it — with a **toast** for quick feedback.

---

➡️ Next: [08 — Editable admin UI](../steps/08-editable-admin-ui.md) · 🧭 Go deeper: [How a web app works](./how-a-web-app-works.md) · ⬆️ [Foundations](./README.md) · [README](../../README.md)
