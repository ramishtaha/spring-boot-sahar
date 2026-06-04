# 17 - Visual polish: a proper design system

_Beyond the core course: a focused redesign pass to make Sahar genuinely good-looking — better type,
colour, depth, spacing and motion — with no change to behaviour._

> [!IMPORTANT]
> **Checkpoint:** [`step-17-visual-polish`](../../checkpoints/step-17-visual-polish/) — identical in
> behaviour to [step 16](./16-ui-and-pwa.md)/[`app/`](../../app/); only the look changed. It is almost
> entirely a [`styles.css`](../../checkpoints/step-17-visual-polish/src/main/resources/static/styles.css)
> rewrite, so the markup and JavaScript were left alone. Package is `com.ramishtaha.sahar`.

## 🎯 Why this matters

"Make it look good" sounds vague, but it's mostly engineering: a small set of **design tokens** (named,
reusable values like a colour or spacing unit, stored once and referenced everywhere) applied
consistently. Doing it as a CSS-only pass — reusing every existing class — is also a lesson in why
separating structure (HTML), behaviour (JS) and presentation (CSS) pays off: you can completely restyle an
app without touching the other two.

> [!NOTE]
> **What changed from Spring Boot 3.x.** Nothing in this step touches Java or Spring — it's pure
> front-end CSS, so the framework version is irrelevant here. The one version-aware detail is the **service
> worker** (a script the browser keeps running in the background to cache the app and serve it offline): bumping
> its cache key is the same browser-platform rule from step 16, unrelated to the Boot 3.x→4 / Java 17→25 moves.
> For the consolidated older-vs-newer story across the whole app, see the
> [Version deltas](../../reference/cheatsheet-version-deltas.md) cheatsheet.

## 🎨 What changed (and the ideas)

- **Tokens first.** Everything is a CSS custom property: colours, a spacing/`--pad` scale, radii
  (`--r-sm/--r/--r-lg/--r-pill`), and a 3-tier shadow scale (`--shadow-sm/--shadow/--shadow-lg`). Both the
  dark and light themes are just different *values* for the same tokens, so the ☀/☾ toggle still swaps the
  entire look by flipping `data-theme`.
- **Type.** A real UI typeface (Plus Jakarta Sans, loaded with `display=swap` and a full system-font
  fallback so it still looks right offline), a tighter heading scale, uppercase tracked section labels, and
  **tabular numerals** for all the times so digits line up.
- **Depth & colour.** Deeper dark background with a soft "dawn glow" radial behind the header, cards with a
  subtle border + layered shadow (and a faint top-sheen in dark mode), and a warmer, higher-contrast accent.
- **A real prayer hero.** The next-prayer banner is now a gradient panel with a pulsing dot and a big
  tabular countdown; the strip highlights the next prayer (accent ring + lift) and dims the ones that passed.
- **The weekly board** reads as a board: a bold day header, training shown as a coloured chip, compact
  L/D meal lines with little badges, hover-lift, and a clear "today" treatment.
- **Polish everywhere:** numbered rule badges, refined foldables with a rotating chevron, nicer buttons
  (gradient primary, ghost secondary) with focus-visible rings, a blurred modal, and gentle staggered
  fade-in — all switched off under `prefers-reduced-motion`.

## 🔒 How a CSS-only restyle stays safe

Because the redesign only changes `styles.css` (plus a font `<link>` and a bumped service-worker cache so
the new shell ships), there is zero risk to the API, the location picker, the countdown, or the
PWA (Progressive Web App — a website that can be installed and run offline like a native app; see
[PWA & service workers](../theory/pwa-and-service-workers.md)). The
JS still finds the same class names; it just looks better. That's the payoff of keeping presentation in CSS.

> [!NOTE]
> The service-worker cache key was bumped to `sahar-v2` so returning visitors get the new stylesheet
> instead of the cached old one — the same "bump the cache when the shell changes" rule from
> [step 16](./16-ui-and-pwa.md).

## 💼 Interview angle

**Q: What are CSS design tokens, and why use them?**
A: Named, reusable values (colours, spacing, radii, shadows) defined once as custom properties and
referenced everywhere. They give you a single source of truth, so changing one variable updates the whole
UI consistently — no hunting for hard-coded hex values scattered across the stylesheet.

**Q: How would you support a dark and a light theme without duplicating your rules?**
A: Keep the same tokens but give them different *values* under a theme selector — here, `:root` for dark and
`:root[data-theme="light"]` for light. Components reference the tokens, not raw colours, so flipping
`data-theme` re-skins the entire app with zero changes to component CSS.

**Q: Why separate structure (HTML), behaviour (JS) and presentation (CSS)?**
A: Separation of concerns lets each change in isolation. This step proves it: a full visual redesign touched
only `styles.css` (plus a font link), with no risk to the API, the countdown, or the PWA, because the JS
still finds the same class names.

**Q: You shipped new CSS but returning users still see the old look — why, and how do you fix it?**
A: A service worker is serving the old stylesheet from its cache. Bump the cache key (here `sahar-v2`) so the
worker discards the stale shell on activation and fetches the new assets — the standard "version the cache
when the shell changes" rule.

**Q: How do you keep a web font from breaking the page when it's slow or offline?**
A: Load it with `display=swap` so text renders immediately in a fallback, and declare a full system-font
stack after the web font in `font-family`. Online you get the designed typeface; offline or during load you
get a near-identical system font instead of invisible or unstyled text.

## 🐞 Common mistakes and how to debug them

- **New styles don't appear after deploy/refresh.** The old service-worker cache is serving the old CSS —
  bump `CACHE` in `service-worker.js` (done here) or unregister the worker in dev tools.
- **Font flashes / wrong font offline.** Expected: the web font loads when online and falls back to the
  system stack otherwise (that's why a real fallback matters).
- **A component looks unstyled.** A class name drifted out of sync between the HTML/JS and the CSS — the
  whole point of a CSS-only pass is to keep those identical.

## ❓ Check yourself

1. Why can the entire app be restyled without touching the HTML or JavaScript?
2. How do the light and dark themes share one set of rules?
3. Why bump the service-worker cache name in this step?
4. Why load the web font with a system-font fallback?

---
⬅️ Prev: [16 - Location picker, UI & PWA](./16-ui-and-pwa.md) · ➡️ Next: [18 - Testing the pyramid](./18-testing.md) · 📍 Checkpoint: [step-17](../../checkpoints/step-17-visual-polish/) · 🔗 See also: [PWA & service workers](../theory/pwa-and-service-workers.md) · [Version deltas](../../reference/cheatsheet-version-deltas.md) · [Interview-prep](../../reference/interview-prep.md)
