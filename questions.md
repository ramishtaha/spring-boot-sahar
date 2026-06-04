# Questions for my mentor

Keep this open while you build. When something is fuzzy — a concept, a decision, an error you don't
fully understand — log it here instead of breaking flow. Bring the list to your mentor session.

> [!TIP]
> **A good entry has:** the step, the exact question, what you *think* the answer is (commit to a guess —
> it sharpens the discussion), and any code/error. Resolve entries inline so the list doubles as a study log.

---

## 📝 Template

```
### [Step NN] One-line question
- Context: what I was doing / which file.
- My current guess:
- What I'd want to confirm:
- (Resolved): <answer in my own words, once discussed>
```

---

## ❓ Open

### [Step 04] Why constructor injection instead of field injection (`@Autowired` on a field)?
- Context: the controllers take `RoutineService` as a constructor argument.
- My current guess: it makes dependencies explicit and the object testable without Spring.
- What I'd want to confirm: when, if ever, field/setter injection is acceptable.

### [Step 06] When should a method be `@Transactional`, and what exactly is rolled back?
- Context: `RoutineService.replaceBlock` is `@Transactional`; it deletes weeks then re-inserts them.
- My current guess: it groups the deletes+inserts so a failure halfway leaves the old block intact.
- What I'd want to confirm: default propagation/rollback rules; does it roll back on checked exceptions?

### [Step 07] Is a service throwing `ResponseStatusException` a smell?
- Context: block operations throw it for 400/404.
- My current guess: it's a pragmatic shortcut; "purer" code throws a domain exception and translates it in `@RestControllerAdvice`.
- What I'd want to confirm: where the line is for a small app vs a larger one.

### [Step 10] Why prefer Flyway over `schema.sql`/`data.sql`?
- Context: step 10 moved schema + seed into versioned migrations.
- My current guess: versioning + history make it safe across teams, environments, and an evolving schema.
- What I'd want to confirm: how to handle a *change* to an already-applied migration (new V-file vs editing).

---

## ✅ Resolved

_(move entries here once you've discussed and written the answer in your own words)_
