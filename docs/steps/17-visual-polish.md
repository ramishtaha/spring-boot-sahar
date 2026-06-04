# 17 - Visual polish: a proper design system

_Beyond the core course: a focused redesign pass to make Sahar genuinely good-looking — better type,
colour, depth, spacing and motion — with no change to behaviour._

> **Checkpoint:** [`step-17-visual-polish`](../../checkpoints/step-17-visual-polish/) — identical in
> behaviour to [step 16](./16-ui-and-pwa.md)/[`app/`](../../app/); only the look changed. It is almost
> entirely a [`styles.css`](../../checkpoints/step-17-visual-polish/src/main/resources/static/styles.css)
> rewrite, so the markup and JavaScript were left alone.

## Why this matters

"Make it look good" sounds vague, but it's mostly engineering: a small set of **design tokens** applied
consistently. Doing it as a CSS-only pass — reusing every existing class — is also a lesson in why
separating structure (HTML), behaviour (JS) and presentation (CSS) pays off: you can completely restyle an
app without touching the other two.

## What changed (and the ideas)

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

## How a CSS-only restyle stays safe

Because the redesign only changes `styles.css` (plus a font `<link>` and a bumped service-worker cache so
the new shell ships), there is zero risk to the API, the location picker, the countdown, or the PWA. The
JS still finds the same class names; it just looks better. That's the payoff of keeping presentation in CSS.

> The service-worker cache key was bumped to `sahar-v2` so returning visitors get the new stylesheet
> instead of the cached old one — the same "bump the cache when the shell changes" rule from
> [step 16](./16-ui-and-pwa.md).

## Common mistakes and how to debug them

- **New styles don't appear after deploy/refresh.** The old service-worker cache is serving the old CSS —
  bump `CACHE` in `service-worker.js` (done here) or unregister the worker in dev tools.
- **Font flashes / wrong font offline.** Expected: the web font loads when online and falls back to the
  system stack otherwise (that's why a real fallback matters).
- **A component looks unstyled.** A class name drifted out of sync between the HTML/JS and the CSS — the
  whole point of a CSS-only pass is to keep those identical.

## Check yourself

1. Why can the entire app be restyled without touching the HTML or JavaScript?
2. How do the light and dark themes share one set of rules?
3. Why bump the service-worker cache name in this step?
4. Why load the web font with a system-font fallback?

---
Prev: [16 - Location picker, UI & PWA](./16-ui-and-pwa.md) | Next: [99 - Roadmap](./99-roadmap.md) | Checkpoint: [step-17](../../checkpoints/step-17-visual-polish/)
