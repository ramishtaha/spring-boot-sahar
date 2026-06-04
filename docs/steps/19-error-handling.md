# 19 - A clean error model (RFC 9457 ProblemDetail)

_Beyond the core course: stop inventing a new error shape per endpoint — let the service throw plain domain
exceptions and translate them all in one place into a standard, machine-readable error body._

> [!IMPORTANT]
> **Checkpoint:** [`step-19-error-handling`](../../checkpoints/step-19-error-handling/) — the full app, now
> with one error contract instead of ad-hoc shapes scattered across controllers. Package is
> `com.ramishtaha.sahar`.

> [!TIP]
> The whole HTTP-aware error story lives in a single file:
> [`ApiExceptionHandler`](../../checkpoints/step-19-error-handling/src/main/java/com/ramishtaha/sahar/web/ApiExceptionHandler.java).
> If you only read one thing this step, read that.

## 🎯 Why this matters

Every API fails sometimes — a missing id, a bad date, malformed JSON. The question is *how* it fails. If
each endpoint returns its own little JSON blob (`{"message": ...}` here, `{"error": ...}` there, a raw stack
trace somewhere else), then every client — your admin page, a future mobile app, a test — has to special-case
each one. That is a tax you pay forever.

This step pays it down once. We make two decisions and the rest follows:

1. **The service layer throws *meaning*, not HTTP.** It says "this thing doesn't exist" or "this breaks a
   rule" — in plain Java exceptions that know nothing about status codes.
2. **One web-layer class assigns the HTTP status and shapes the body.** Every error comes out in the *same*
   standard format, with the *same* content type. One contract, parsed one way, forever.

That's not just tidiness — "uniform error contract" and "separation of concerns" are exactly the phrases
interviewers listen for. By the end you'll be able to say them *and* point at the code.

## 🧠 Theory

### What is RFC 9457 / ProblemDetail?

**RFC 9457** ("Problem Details for HTTP APIs") is an internet standard that defines a *standard JSON body for
errors*, so clients don't have to guess your shape. It looks like this:

```json
{
  "type": "about:blank",
  "title": "Not found",
  "status": 404,
  "detail": "no schedule item 999999",
  "instance": "/api/schedule/999999"
}
```

The members are fixed by the spec:
- **`type`** — a URI naming the *kind* of problem (defaults to `about:blank` when you don't define your own).
- **`title`** — a short, human-readable label for that type ("Not found").
- **`status`** — the HTTP status code, mirrored into the body.
- **`detail`** — a human-readable explanation *of this specific occurrence* (our exception message goes here).
- **`instance`** — a URI for this specific occurrence (Spring fills it with the request path).

You may also add **extension members** — your own extra fields. We add an `errors` array for validation.

Spring Framework 6/7 (and so Spring Boot 4) ships a first-class type for this: **`ProblemDetail`**. You don't
add a library — `org.springframework.http.ProblemDetail` is already on the classpath via
[`spring-boot-starter-webmvc`](../../checkpoints/step-19-error-handling/pom.xml). When you return one, Spring
serializes it as JSON *and* sets the response `Content-Type` to **`application/problem+json`** — the media
type the RFC reserves for these bodies, so clients can recognize "this is a problem document" before parsing.

> [!NOTE]
> A **media type** (a.k.a. content type) is the label in the `Content-Type` header that tells the client what
> kind of payload it's getting. `application/json` means "some JSON"; `application/problem+json` means "JSON,
> and specifically a Problem Details document." The `+json` suffix says "it's JSON underneath."

### Where each piece lives

The design is a clean split. The service expresses *what went wrong* in domain terms; exactly one web-layer
class decides *which HTTP status* that maps to and *what the body looks like*.

```mermaid
flowchart LR
  Ctrl[Controller<br/>thin] --> Svc[RoutineService<br/>throws NotFoundException / BadRequestException]
  Svc -- bubbles up --> Adv[ApiExceptionHandler<br/>@RestControllerAdvice]
  Adv --> PD[ProblemDetail<br/>application/problem+json]
  PD --> Client[admin.js / app / tests]
```

The arrow that matters: the exception **bubbles up** past the controller untouched, and `ApiExceptionHandler`
catches it. The controller doesn't try/catch anything — it stays a one-liner.

### Why the service must NOT import HttpStatus

If `RoutineService` returned `404`s itself, it would be welded to HTTP. You could never call it from a batch
job, a CLI, or a message listener without dragging the servlet stack along. By throwing
`NotFoundException` instead, the service stays a *pure domain* component — and the knowledge "not-found means
404" lives in one HTTP-aware file. That is the separation-of-concerns lesson of this whole step.

> [!NOTE]
> **Beyond the core course:** earlier the project leaned on Spring's `ResponseStatusException` (an exception
> that carries an HTTP status). It's convenient, but it puts HTTP knowledge *inside* the service. This step
> deliberately refactors that out — domain exceptions in, `ResponseStatusException` out.

## 🚦 Start from

[`step-18-testing`](../../checkpoints/step-18-testing/). The app is fully working and well-tested; this step
changes only *how errors are produced and shaped* — the happy paths are untouched.

## 🛠️ Build it

### 1. Two plain domain exceptions

Create an `error` package with two `RuntimeException`s. They carry only a message — no status, no HTTP import.
From [`NotFoundException`](../../checkpoints/step-19-error-handling/src/main/java/com/ramishtaha/sahar/error/NotFoundException.java):

```java
package com.ramishtaha.sahar.error;

/** Thrown by the service when something the caller asked for does not exist. HTTP-agnostic on purpose. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
```

[`BadRequestException`](../../checkpoints/step-19-error-handling/src/main/java/com/ramishtaha/sahar/error/BadRequestException.java)
is its twin, for "well-formed but breaks a business rule." They extend `RuntimeException` so callers aren't
forced to declare or catch them — they're meant to bubble all the way to the advice.

### 2. The service throws meaning, not status

[`RoutineService`](../../checkpoints/step-19-error-handling/src/main/java/com/ramishtaha/sahar/service/RoutineService.java)
imports the two domain exceptions and nothing from `org.springframework.http`. A missing row is a
`NotFoundException`; a broken rule is a `BadRequestException`:

```java
public void deleteScheduleItem(Long id) {
    if (schedule.delete(id) == 0) {
        throw new NotFoundException("no schedule item " + id);
    }
}

public BlockPlan addWeek() {
    ...
    if (weeks.size() >= 5) {
        throw new BadRequestException("a block is at most 5 weeks");
    }
    ...
}
```

Notice the messages are about the *domain* ("a block is at most 5 weeks"), not about HTTP.

### 3. One @RestControllerAdvice maps everything

This is the only HTTP-aware error file. `@RestControllerAdvice` is a class whose `@ExceptionHandler` methods
apply to *every* controller in the app — a global "catch block." Each handler builds a `ProblemDetail` and
returns it; Spring reads the status *out of* the ProblemDetail and sets the response status to match. From
[`ApiExceptionHandler`](../../checkpoints/step-19-error-handling/src/main/java/com/ramishtaha/sahar/web/ApiExceptionHandler.java):

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Not found");
        return pd;
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Invalid request");
        return pd;
    }
}
```

`ProblemDetail.forStatusAndDetail(status, detail)` is the standard factory: it fills `status` and `detail` for
you; `setTitle(...)` sets the human label.

> [!NOTE]
> `@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`. The `@ResponseBody` part is what makes the
> returned `ProblemDetail` get serialized to the response body (as JSON) instead of being treated as a view
> name. Plain `@ControllerAdvice` alone would not do that.

### 4. Validation errors → a 400 with an `errors` array

When a request hits `@Valid @RequestBody` and a field is invalid, Spring throws
`MethodArgumentNotValidException` *before your controller method even runs*. We catch it, collect each broken
field as `"field: message"`, and attach the list as a custom member with `setProperty`:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "One or more fields are invalid");
    pd.setTitle("Validation failed");
    List<String> errors = new ArrayList<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
        errors.add(fe.getField() + ": " + fe.getDefaultMessage());
    }
    for (ObjectError oe : ex.getBindingResult().getGlobalErrors()) {
        errors.add(oe.getDefaultMessage());
    }
    Collections.sort(errors);
    pd.setProperty("errors", errors);   // a custom extension member on the problem body
    return pd;
}
```

`setProperty("errors", list)` adds an **extension member** to the JSON — a field beyond the RFC's standard
ones. Sorting makes the output deterministic (handy for tests and for humans). Full method in
[`ApiExceptionHandler`](../../checkpoints/step-19-error-handling/src/main/java/com/ramishtaha/sahar/web/ApiExceptionHandler.java#L57).

### 5. Malformed JSON → a clean 400

If the body isn't valid JSON at all, parsing fails *before* validation with `HttpMessageNotReadableException`.
Without a handler, Spring's default 400 leaks internals; we give it the same ProblemDetail treatment:

```java
@ExceptionHandler(HttpMessageNotReadableException.class)
public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request body is missing or not valid JSON");
    pd.setTitle("Malformed request");
    return pd;
}
```

### 6. The browser reads the standard shape

Because the body is now predictable, the admin UI's error reader gets simpler and more robust. From
[`admin.js`](../../checkpoints/step-19-error-handling/src/main/resources/static/admin.js#L29):

```javascript
// Pull a human message out of an RFC 9457 ProblemDetail response (or fall back).
function errorText(res) {
    const b = res.body || {};
    if (Array.isArray(b.errors)) return b.errors.join('; '); // ProblemDetail validation: our "errors" array
    if (b.detail) return b.detail;                           // ProblemDetail "detail"
    ...
    return 'HTTP ' + res.status;
}
```

It checks `errors` first (the validation list), then falls back to `detail`. Every save button across the page
funnels through this one function.

## ▶️ Try it

Start the app (`./mvnw spring-boot:run`), then poke the error paths. Note the `-i` flag — it prints the
response *headers* so you can see the `application/problem+json` content type.

```bash
# 404 — a missing schedule item flows through NotFoundException -> ProblemDetail
curl -i -X DELETE http://localhost:8080/api/schedule/999999
# HTTP/1.1 404
# Content-Type: application/problem+json
# {"type":"about:blank","title":"Not found","status":404,"detail":"no schedule item 999999","instance":"/api/schedule/999999"}

# 400 with errors[] — invalid field value trips Bean Validation
curl -i -X PUT http://localhost:8080/api/prayer-times \
  -H "Content-Type: application/json" -d '{"fajr":"99:99"}'
# HTTP/1.1 400 ... "title":"Validation failed", "errors":["fajr: ...", "..."]

# 400 malformed — not even JSON, so parsing fails before validation
curl -i -X PUT http://localhost:8080/api/prayer-times \
  -H "Content-Type: application/json" -d 'not json'
# HTTP/1.1 400 ... "title":"Malformed request","detail":"Request body is missing or not valid JSON"
```

HTTPie shows the body even more readably:

```bash
http DELETE :8080/api/schedule/999999
http PUT :8080/api/prayer-times fajr=99:99
```

> [!TIP]
> On Windows `cmd`/PowerShell, swap the single quotes for double quotes and escape the inner ones:
> `curl -i -X PUT http://localhost:8080/api/prayer-times -H "Content-Type: application/json" -d "{\"fajr\":\"99:99\"}"`.

## ✅ End state

- An `error` package with two HTTP-agnostic domain exceptions:
  [`NotFoundException`](../../checkpoints/step-19-error-handling/src/main/java/com/ramishtaha/sahar/error/NotFoundException.java)
  and [`BadRequestException`](../../checkpoints/step-19-error-handling/src/main/java/com/ramishtaha/sahar/error/BadRequestException.java).
- [`RoutineService`](../../checkpoints/step-19-error-handling/src/main/java/com/ramishtaha/sahar/service/RoutineService.java)
  and the controllers no longer use `ResponseStatusException`; the service imports nothing from
  `org.springframework.http`.
- One [`ApiExceptionHandler`](../../checkpoints/step-19-error-handling/src/main/java/com/ramishtaha/sahar/web/ApiExceptionHandler.java)
  maps every error to a `ProblemDetail`: 404, 400 (rule), 400 (validation, with `errors[]`), 400 (malformed JSON).
- Every error response carries `Content-Type: application/problem+json` and the same predictable shape.
- [`admin.js`](../../checkpoints/step-19-error-handling/src/main/resources/static/admin.js)'s `errorText()`
  reads `errors` / `detail` from the standard body.

## 💼 Interview angle

**Q: What is RFC 9457 / ProblemDetail, and why prefer it over a custom error body?**
RFC 9457 ("Problem Details for HTTP APIs") standardizes the JSON shape of an error — `type`, `title`,
`status`, `detail`, `instance`, plus optional extension members — served as `application/problem+json`. A
standard body beats a bespoke one because every client parses errors the same way; you avoid bikeshedding the
shape per service; and tooling/clients can recognize it by media type. Spring Boot 4 implements it as
`org.springframework.http.ProblemDetail`, so there's nothing extra to add.

**Q: Explain the separation of concerns in your error handling.**
The service layer throws *domain* exceptions (`NotFoundException`, `BadRequestException`) that express
*meaning* and know nothing about HTTP. Exactly one web-layer class — the `@RestControllerAdvice` — is
HTTP-aware: it maps each exception to a status and a `ProblemDetail` body. So the *what's wrong* and the
*how to report it over HTTP* live in different places, and the rule "not-found → 404" exists in one file.

**Q: What do `@RestControllerAdvice` and `@ExceptionHandler` do?**
`@ExceptionHandler(SomeException.class)` marks a method that handles that exception type. `@ControllerAdvice`
makes such handlers *global* — they apply across all controllers. `@RestControllerAdvice` is
`@ControllerAdvice` + `@ResponseBody`, so the returned object (here, a `ProblemDetail`) is serialized straight
into the response body as JSON rather than resolved as a view name.

**Q: How does the response get the right status code and content type?**
Spring inspects the returned `ProblemDetail`, reads its `status` field, and sets the HTTP response status to
match — you don't set it separately. It also serializes ProblemDetail with the `application/problem+json`
media type automatically.

**Q: Why shouldn't the service import `HttpStatus` or throw `ResponseStatusException`?**
Because that couples a pure domain component to the web layer. A service that throws HTTP statuses can't be
reused from a batch job, scheduler, CLI, or message listener without dragging servlet concerns along. Throwing
plain domain exceptions keeps the service portable and concentrates HTTP knowledge in one advice class.

**Q: How do you surface field-level validation errors?**
A `@Valid @RequestBody` failure raises `MethodArgumentNotValidException` before the controller body runs. The
advice catches it, walks `getBindingResult().getFieldErrors()`, builds `"field: message"` strings, and attaches
them to the ProblemDetail as a custom extension member via `setProperty("errors", list)` — so the standard
body carries the list of what's wrong.

## 🐞 Common mistakes and how to debug them

- **Handler never fires / you still see the default error page.** The exception isn't reaching the advice —
  usually because something *caught* it lower down (a stray try/catch in a controller), or the exception type
  doesn't match an `@ExceptionHandler`. Search for leftover `ResponseStatusException` and remove it.
- **You set `setStatus` but the HTTP status is still 200/500.** Don't call `setStatus` by hand — use
  `ProblemDetail.forStatusAndDetail(status, detail)`; Spring derives the response status from that. Returning a
  ProblemDetail you built with a `200` status will produce a 200 with an error-looking body.
- **Content-Type comes back `application/json`, not `application/problem+json`.** You're probably returning a
  hand-rolled DTO or a `Map`, not a `ProblemDetail`. Return the `ProblemDetail` type itself.
- **`errors` array missing on validation failures.** Either you forgot `setProperty("errors", list)`, or the
  field passed validation and the real failure was a parse error (`HttpMessageNotReadableException`) — those
  carry `detail`, not `errors`.
- **`GET /api/schedule/999999` returns 405/404 from Spring, not your ProblemDetail.** There's no `GET /{id}`
  route; the not-found *domain* path is the `DELETE`/`PUT` on a missing id. Use those to exercise the handler.
- **A new exception type returns a raw 500 stack trace.** There's no `@ExceptionHandler` for it yet. Add one,
  or a fallback `@ExceptionHandler(Exception.class)` that returns a generic 500 ProblemDetail (without leaking
  the message).

## ❓ Check yourself

1. Name the five standard members of a ProblemDetail body and say what each is for.
2. Why does `RoutineService` import `NotFoundException` instead of `HttpStatus`?
3. What's the difference between `@ControllerAdvice` and `@RestControllerAdvice`, and why does it matter here?
4. Which exception fires for an invalid *field value* versus *unparseable JSON*, and how does the body differ?
5. How does Spring decide the HTTP status code and `Content-Type` of the response when you return a `ProblemDetail`?

---
⬅️ Prev: [18 - Testing the pyramid](./18-testing.md) · ➡️ Next: [20 - Observability with Actuator](./20-observability.md) · 📍 Checkpoint: [step-19-error-handling](../../checkpoints/step-19-error-handling/) · 🔗 See also: [HTTP & REST](../theory/http-and-rest.md) · [interview-prep](../../reference/interview-prep.md)
