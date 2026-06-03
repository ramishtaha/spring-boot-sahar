# 05 - Validation and business rules

*Reject bad edits at the door: well-formed prayer times, a 4-or-5-week block, and a deload that is always the last week - turned into clean HTTP 400s.*

## Why this matters

In [step 04](./04-in-memory-edit.md) you made the routine editable: `PUT /api/prayer-times` and `PUT /api/block` accept a JSON body and replace the in-memory state. But they accepted *anything*. Send `"fajr": "25:99"` and the app shrugged and stored it. Send a 3-week block, or a block whose deload sits in the middle, and the routine quietly went wrong.

That is the gap this step closes. The point of Sahar is to be edited once a month and then trusted for the rest of the month. If a typo can corrupt the prayer schedule or break the training block, the trust is gone. So before any edit is allowed to mutate state, it has to pass a gate.

The lesson underneath the app: **validate at the boundary, in one place, with declarative rules.** You will see three layers of constraints - simple field rules (`@NotBlank`, `@Pattern`), a built-in collection rule (`@Size`), and a custom cross-field rule you write yourself (`@DeloadLast`) - all enforced automatically by Spring when the request body arrives, and all failures funnelled into one tidy `400` response shape. That is the standard, idiomatic way to do input validation in a Spring application, and you will reuse exactly this pattern in every controller from here on.

## Theory

### What Bean Validation is

Bean Validation is a Java standard (the `jakarta.validation` API, the Jakarta EE 11 namespace - see the [glossary](../../reference/glossary.md)). You *describe* rules by putting annotations on fields, and a separate engine *enforces* them. The reference implementation - the engine that actually runs the checks - is **Hibernate Validator**, which `spring-boot-starter-validation` pulls onto the classpath. You added that starter back in the [baseline pom](../../checkpoints/step-00-baseline/); step 05 is where it finally earns its keep.

Two halves, kept separate on purpose:

- The **annotation** (e.g. `@Pattern(regexp = ...)`) is just metadata. On its own it does nothing - it is a sticky note on a field.
- The **validation engine** reads those notes and runs them, but only when something is *asked* to be validated. In a web app, that trigger is `@Valid` on a controller's `@RequestBody`.

### Where validation sits in a request

Validation is not something you call by hand. It is wired into the request lifecycle (the broader picture is in [HTTP and REST](../theory/http-and-rest.md)):

```mermaid
flowchart TD
    A[PUT /api/block with JSON body] --> B[Jackson deserializes JSON -> BlockPlan record]
    B --> C{@Valid on the @RequestBody param?}
    C -- no --> H[handler runs with whatever was sent]
    C -- yes --> D[Hibernate Validator runs every constraint]
    D -- all pass --> E[controller method runs, state updates]
    D -- any fail --> F[Spring throws MethodArgumentNotValidException]
    F --> G[@RestControllerAdvice: ApiExceptionHandler]
    G --> I[HTTP 400 + JSON list of messages]
```

The order matters: Jackson builds the object *first* (the field types must parse), then the validator checks the *values*. So `"isha": "20:36"` becomes a valid `String` either way - it is `@Pattern` that decides `20:36` is acceptable and `99:99` is not.

### Field rules vs cross-field rules

Most rules are about one field: "this must not be blank", "this must look like a time". Those are simple field annotations.

But some rules span the whole object. "The deload is the *last* week" is not a fact about any single `Week` - week 4 being a deload is fine in a 4-week block and wrong in a 5-week block. A field annotation literally cannot see the other fields. For that you write a **custom class-level constraint**: an annotation on the *type* plus a `ConstraintValidator` that receives the whole object. That is `@DeloadLast`.

### Cascading with `@Valid`

A `BlockPlan` holds a `List<Week>`. Validating the list's size does not validate the weeks inside it. Putting `@Valid` on the `weeks` field tells the engine to **cascade** - descend into every `Week` and run its field rules too. Without that, a `Week` with a blank `name` would slip through.

## Start from

Continue from the [step 04 checkpoint](../../checkpoints/step-04-in-memory-edit/). At that point the domain records (`PrayerTimes`, `Week`, `BlockPlan`) had no annotations, and the controllers took a bare `@RequestBody`:

```java
// step 04 PrayerTimesController - note: NO @Valid
@PutMapping("/api/prayer-times")
public PrayerTimes update(@RequestBody PrayerTimes prayerTimes) {
    return routine.updatePrayerTimes(prayerTimes).prayerTimes();
}
```

The `spring-boot-starter-validation` dependency is already in the [pom](../../checkpoints/step-05-validation-and-rules/) from the baseline, so there is no Maven change in this step - only annotations and two new classes.

## Build it

### 1. Annotate the prayer times

Open `domain/PrayerTimes.java` and put field constraints on each time component. A record's constructor parameters *are* its fields, so the annotations go right on the parameters:

```java
package com.ramishtaha.sahar.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PrayerTimes(
        @NotBlank @Pattern(regexp = TIME, message = "Fajr must be a 24-hour time like 04:37") String fajr,
        @NotBlank @Pattern(regexp = TIME, message = "Sunrise must be a 24-hour time like 05:59") String sunrise,
        @NotBlank @Pattern(regexp = TIME, message = "Dhuhr must be a 24-hour time like 12:37") String dhuhr,
        @NotBlank @Pattern(regexp = TIME, message = "Asr must be a 24-hour time like 17:12") String asr,
        @NotBlank @Pattern(regexp = TIME, message = "Maghrib must be a 24-hour time like 19:13") String maghrib,
        @NotBlank @Pattern(regexp = TIME, message = "Isha must be a 24-hour time like 20:36") String isha,
        String methodNote
) {
    /** 24-hour HH:mm, leading zero required: 00:00 through 23:59. */
    public static final String TIME = "^([01]\\d|2[0-3]):[0-5]\\d$";
}
```

Line by line of *why*:

- `@NotBlank` means **not null, not empty, and not only whitespace** - stronger than `@NotNull` (which allows `""`) and stronger than `@NotEmpty` (which allows `"   "`). The right choice for a human-typed string.
- `@Pattern(regexp = TIME, ...)` checks the value against a regex. Note `@Pattern` does **not** complain about `null` - that is `@NotBlank`'s job. Using both means "must be present AND well-formed", with separate messages.
- `TIME = "^([01]\\d|2[0-3]):[0-5]\\d$"` is the 24-hour rule: hours `00-19` via `[01]\d` or `20-23` via `2[0-3]`, a literal `:`, then minutes `00-59` via `[0-5]\d`. The leading zero is required, so `4:37` is rejected but `04:37` passes - that keeps every stored time the same fixed width.
- `methodNote` is free text (the "Thane, Hanafi" calculation note) so it carries **no** constraint. Validate only what has a rule; do not annotate for the sake of it.

The `message` is what the user eventually sees, so it is concrete ("a 24-hour time like 04:37"), not "invalid format".

### 2. Annotate a single Week

Open `domain/Week.java`. These are the per-week field rules that will be reached by cascading from the block:

```java
package com.ramishtaha.sahar.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record Week(
        @Min(value = 1, message = "week ordinal starts at 1") int ordinal,
        @NotBlank(message = "week name is required") String name,
        @NotBlank(message = "start date is required") String startDate,
        @NotBlank(message = "end date is required") String endDate,
        @NotBlank(message = "training focus is required") String trainingFocus,
        @NotBlank(message = "backend focus is required") String backendFocus,
        boolean deload
) {
}
```

- `@Min(1)` on `ordinal`: the position is 1-based, so 0 or a negative is nonsense. `@Min` works on the primitive `int`; there is no "null" to worry about.
- `@NotBlank` on every human-typed string field. Each gets its own message so a 400 points straight at the offending field.
- `deload` is a `boolean` with no annotation - whether it is allowed to be `true` depends on its *position*, which a single field cannot judge. That is the next step's job.

### 3. Annotate the block (size + cascade + custom rule)

Open `domain/BlockPlan.java`. This is where the three layers come together:

```java
package com.ramishtaha.sahar.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import com.ramishtaha.sahar.validation.DeloadLast;

import java.util.List;

@DeloadLast
public record BlockPlan(
        String label,

        @NotEmpty(message = "a block needs weeks")
        @Size(min = 4, max = 5, message = "a block is 4 or 5 weeks long")
        @Valid
        List<Week> weeks
) {
    public int length() {
        return weeks == null ? 0 : weeks.size();
    }
}
```

Four annotations, four distinct jobs:

- `@DeloadLast` sits on the **type** (the record itself), because the rule looks at the whole `BlockPlan`, not one component. This is the custom constraint you write in steps 4-5 below.
- `@NotEmpty` on `weeks`: not null and not an empty list. If this fails, the deload rule never gets a chance to complain (see step 5).
- `@Size(min = 4, max = 5)`: the business rule that **a block is 4 or 5 weeks** (4-week = Foundation/Build/Peak/Deload; the 5-week variant adds a second build phase). This is a built-in constraint - you do not write any code for it.
- `@Valid` on the list: **cascade** into each `Week` so its `@NotBlank`/`@Min` rules run too. Drop this and a week with a blank `name` would pass.

The `length()` helper is just convenience for the service layer; it carries no validation.

### 4. Write the custom annotation `@DeloadLast`

A custom constraint needs two files. First the annotation, in the new `com.ramishtaha.sahar.validation` package, in `DeloadLast.java`:

```java
package com.ramishtaha.sahar.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = DeloadLastValidator.class)
@Target(TYPE)
@Retention(RUNTIME)
public @interface DeloadLast {

    String message() default "the deload must be the last week (and only the last week)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

What each part does:

- `@Constraint(validatedBy = DeloadLastValidator.class)` is the meta-annotation that *makes this a validation constraint* and points at the class holding the logic.
- `@Target(TYPE)` - this annotation may only be placed on a type (a class or record), which is why `@DeloadLast` goes on `BlockPlan` itself and not on a field.
- `@Retention(RUNTIME)` - the annotation must survive into the running program; the validator reads it via reflection at request time, so without `RUNTIME` it would be invisible.
- The three members `message()`, `groups()`, `payload()` are **mandatory** by the Bean Validation spec - every constraint annotation must declare exactly these, with these signatures. `message()` is the default error text; `groups` and `payload` are advanced features Sahar does not use, but they must still be present.

### 5. Write the validator `DeloadLastValidator`

The logic lives in `validation/DeloadLastValidator.java`:

```java
package com.ramishtaha.sahar.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.ramishtaha.sahar.domain.BlockPlan;
import com.ramishtaha.sahar.domain.Week;

import java.util.List;

public class DeloadLastValidator implements ConstraintValidator<DeloadLast, BlockPlan> {

    @Override
    public boolean isValid(BlockPlan block, ConstraintValidatorContext context) {
        if (block == null) {
            return true;
        }
        List<Week> weeks = block.weeks();
        if (weeks == null || weeks.isEmpty()) {
            return true; // not our concern - @NotEmpty/@Size handle the size
        }

        int lastIndex = weeks.size() - 1;
        for (int i = 0; i < weeks.size(); i++) {
            boolean isDeload = weeks.get(i).deload();
            boolean isLast = (i == lastIndex);
            if (isDeload != isLast) {
                // Either an earlier week is a deload (isDeload && !isLast),
                // or the last week is NOT a deload (!isDeload && isLast). Both are invalid.
                return false;
            }
        }
        return true;
    }
}
```

The key ideas:

- `implements ConstraintValidator<DeloadLast, BlockPlan>` ties the annotation type (`DeloadLast`) to the type it validates (`BlockPlan`). The engine instantiates this class and calls `isValid` whenever it meets a `@DeloadLast`-annotated object.
- `isValid` returns `true` for valid. The whole rule is one elegant line: for every week, `isDeload` must equal `isLast`. Walk the list; the *last* week must be a deload and *no other* week may be. The instant `isDeload != isLast`, the block is wrong.
- It deliberately returns `true` for a null or empty list. **Each validator should do exactly one thing.** Size is `@NotEmpty`/`@Size`'s job; if this validator also complained about size, a 3-week block would produce two confusing errors instead of one clear one. Let each rule own its message.

### 6. Add `@Valid` to the controller request bodies

The annotations describe rules but nothing runs them yet. Add `@Valid` to the `@RequestBody` parameter in each editing controller. In `web/PrayerTimesController.java`:

```java
import jakarta.validation.Valid;

@PutMapping("/api/prayer-times")
public PrayerTimes update(@Valid @RequestBody PrayerTimes prayerTimes) {
    return routine.updatePrayerTimes(prayerTimes).prayerTimes();
}
```

And the same in `web/BlockController.java`:

```java
@PutMapping("/api/block")
public BlockPlan update(@Valid @RequestBody BlockPlan block) {
    return routine.updateBlock(block).block();
}
```

`@Valid` is the trigger. With it, Spring validates the deserialized object *before* the method body runs; if anything fails it throws `MethodArgumentNotValidException` and your method never executes - the bad data never reaches the service or the in-memory state.

### 7. Turn failures into a clean 400 with `@RestControllerAdvice`

Without a handler you would still get a `400`, but with Spring's verbose default body. Add one global handler so every controller gives the frontend the same tidy shape. Create `web/ApiExceptionHandler.java`:

```java
package com.ramishtaha.sahar.web;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    /** The body shape of a validation failure. */
    public record ApiError(int status, String error, List<String> messages) {
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex) {
        List<String> messages = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            messages.add(fe.getField() + ": " + fe.getDefaultMessage());
        }
        for (ObjectError oe : ex.getBindingResult().getGlobalErrors()) {
            messages.add(oe.getDefaultMessage());
        }
        Collections.sort(messages); // stable, readable ordering
        return new ApiError(400, "validation failed", messages);
    }
}
```

What it does:

- `@RestControllerAdvice` is a global interceptor across *every* `@RestController` in the app. Declare the handler once here, and it covers prayer times, block, and every future endpoint.
- `@ExceptionHandler(MethodArgumentNotValidException.class)` says "when this specific exception escapes any controller, run this method instead of letting it 500".
- `@ResponseStatus(HttpStatus.BAD_REQUEST)` sets the status code to `400` - the correct code for "your input was malformed" (see the [HTTP cheatsheet](../../reference/cheatsheet-http-rest.md)).
- It pulls **field errors** (e.g. `fajr: Fajr must be a 24-hour time...`) and **global errors** (the class-level `@DeloadLast` message has no field, so it lands here) out of the binding result, flattens them to plain strings, and sorts them so the output order is stable and testable.
- Returning the `ApiError` record means Jackson 3 (`tools.jackson` - transparent here, see [persistence/JSON notes](../theory/http-and-rest.md)) serializes it to JSON automatically, exactly like any other response body.

## End state

The two editing endpoints now reject bad input with a clear `400` before any state changes; valid edits still succeed exactly as in step 04.

A bad prayer time:

```bash
curl -i -X PUT http://localhost:8080/api/prayer-times \
  -H "Content-Type: application/json" \
  -d '{"fajr":"25:99","sunrise":"05:59","dhuhr":"12:37","asr":"17:12","maghrib":"19:13","isha":"20:36","methodNote":"Thane, Hanafi"}'
```

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{"status":400,"error":"validation failed","messages":["fajr: Fajr must be a 24-hour time like 04:37"]}
```

A block whose deload is *not* the last week (here a 4-week block with week 1 flagged deload and week 4 not):

```bash
curl -i -X PUT http://localhost:8080/api/block \
  -H "Content-Type: application/json" \
  -d '{"label":"Jun 2026","weeks":[
        {"ordinal":1,"name":"Foundation","startDate":"8 Jun","endDate":"14 Jun","trainingFocus":"base","backendFocus":"setup","deload":true},
        {"ordinal":2,"name":"Build","startDate":"15 Jun","endDate":"21 Jun","trainingFocus":"volume","backendFocus":"web","deload":false},
        {"ordinal":3,"name":"Peak","startDate":"22 Jun","endDate":"28 Jun","trainingFocus":"intensity","backendFocus":"data","deload":false},
        {"ordinal":4,"name":"Deload","startDate":"29 Jun","endDate":"5 Jul","trainingFocus":"recover","backendFocus":"polish","deload":false}
      ]}'
```

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{"status":400,"error":"validation failed","messages":["the deload must be the last week (and only the last week)"]}
```

A 3-week block fails on size instead (`weeks: a block is 4 or 5 weeks long`), and a valid 4-week block returns `200` with the stored block - the deload correctly on week 4.

Files changed / added in this step (browse the full [step 05 checkpoint](../../checkpoints/step-05-validation-and-rules/)):

- `domain/PrayerTimes.java` - added `@NotBlank` + `@Pattern` and the `TIME` regex.
- `domain/Week.java` - added `@Min` and `@NotBlank` field rules.
- `domain/BlockPlan.java` - added `@DeloadLast`, `@NotEmpty`, `@Size`, `@Valid`.
- `validation/DeloadLast.java` - **new** custom constraint annotation.
- `validation/DeloadLastValidator.java` - **new** `ConstraintValidator` logic.
- `web/ApiExceptionHandler.java` - **new** global `@RestControllerAdvice`.
- `web/PrayerTimesController.java` and `web/BlockController.java` - added `@Valid` to the `@RequestBody`.

No pom change: `spring-boot-starter-validation` was already present from the baseline.

## Common mistakes and how to debug them

- **Forgetting `@Valid` on the parameter.** The annotations are present but nothing rejects bad input - the symptom is "my `@Pattern` does nothing". Fix: every `@RequestBody` you want validated needs `@Valid` right before it.
- **Putting `@DeloadLast` on a field instead of the type.** It is `@Target(TYPE)`, so the compiler rejects it on a field. It belongs on the `BlockPlan` record declaration, because the rule needs the whole object.
- **Missing `@Valid` on the `weeks` list, so cascade never happens.** A `Week` with a blank `name` slips through even though `Week` is fully annotated. Fix: keep `@Valid` on the `List<Week>` field, distinct from the `@Valid` on the controller param.
- **The validator throwing a `NullPointerException`.** Jackson can hand you a `null` list, and `@NotEmpty` runs independently. Always null-check inside `isValid` and return `true` for null/empty - let the size constraints own that case, as the real validator does.
- **Expecting `@Pattern` to reject `null`.** It does not - `@Pattern` passes on `null`. You need `@NotBlank` (or `@NotNull`) alongside it for "must be present and well-formed".
- **Forgetting the leading zero in a time.** `"4:37"` fails the regex; `"04:37"` passes. If a real time is being rejected, check the width before suspecting the pattern.
- **A `415 Unsupported Media Type` instead of `400`.** That is not validation - you forgot `-H "Content-Type: application/json"` on the request, so Jackson never even built the object.
- **Two errors when you expected one.** Usually a validator doing more than one job. Keep each rule single-purpose so messages stay precise.

## Check yourself

1. Why does `PrayerTimes.fajr` need *both* `@NotBlank` and `@Pattern`, rather than just one of them?
2. What is the exact trigger that makes Hibernate Validator run on an incoming `PUT /api/block` body? What happens if it is missing?
3. Why is `@DeloadLast` a class-level constraint and not a field constraint on `Week.deload`?
4. Why does `DeloadLastValidator.isValid` return `true` for a null or empty `weeks` list instead of failing it?
5. What is the difference between a *field error* and a *global error* in `MethodArgumentNotValidException`, and which one carries the `@DeloadLast` message?
6. What would break if you removed `@Valid` from the `List<Week> weeks` field but left it on the controller parameter?

## ---

Prev: [04 - In-memory edit](./04-in-memory-edit.md) | Next: [06 - JdbcTemplate and H2](./06-jdbctemplate-h2.md) | Checkpoint: [step-05-validation-and-rules](../../checkpoints/step-05-validation-and-rules/)
