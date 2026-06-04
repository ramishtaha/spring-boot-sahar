# Suggested diet plan

_A concrete week of meals derived from the Sahar nutrition rules and training grid. The app shows the
per-day lunch/dinner on the **This week** board; this doc is the full plan and the reasoning._

> [!IMPORTANT]
> Not medical advice — it's a structured starting point built from your own domain notes. Adjust to
> appetite, weight goals, and how training feels (autoregulate).

## 🔑 The principles it follows

- **Heavy lunch, light dinner.** Lunch is the big meal (rotating red meat and chicken with greens);
  dinner is light (fish/prawns/white fish), eaten ~2 h before sleep so it doesn't wreck deep sleep.
- **Breakfast is fixed:** 3–4 whole eggs in ghee + raw pumpkin seeds, with **B12** (~09:00). Quick,
  high-fat/protein, no decisions.
- **Organ meats, scheduled** so the weekly caps are automatic: **heart** freely (Wed), **liver ~100 g
  once/week** (Thu — vitamin-A cap), **kidney once/week** (Fri). Brain only occasionally (prion risk;
  get DHA from fish instead).
- **Omega-3 from food, 2–3×/week:** oily-fish dinners on **Mon / Thu / Sat** — bangda (mackerel) is
  cheapest and highest, then surmai and rawas.
- **Red meat ~2×/week** (Mon, Wed, Sat) and **chicken ~2–3×/week** (Tue, Thu, Sun); **greens daily**
  (hidden in stews; palak paneer — the paneer's calcium binds spinach's oxalate, lowering stone risk).
- **Batch-cook on Sunday:** the week's red-meat and chicken stews. **Fish and prawns are bought and
  cooked fresh** (they don't batch well).
- **Training-aligned:** Saturday is the hard-body spar day → eat well / refeed; the taper Friday keeps
  dinner light to protect sleep before Saturday.

## 🍽️ The week

> [!NOTE]
> Every morning: **3–4 eggs in ghee + raw pumpkin seeds** · B12 with breakfast.

| Day | Training | Lunch (heavy) | Dinner (light) | Cue |
|-----|----------|---------------|----------------|-----|
| **Mon** | Muay Thai (technical) | Beef/mutton stew (Sun batch) + leafy greens | Oily fish — bangda/mackerel | omega-3 |
| **Tue** | Boxing | Chicken (batch) + palak / greens | Prawns or white fish | — |
| **Wed** | Wrestling / BJJ (light) | Beef/mutton + **heart** + greens | Light chicken or surmai | heart (eat freely) |
| **Thu** | Muay Thai | Chicken + **liver (~100 g)** + greens | Oily fish — rawas | liver day · omega-3 |
| **Fri** | Padwork + taper | Mutton/chicken (lighter) + **kidney** + greens | White fish / prawns (light) | kidney day · protect sleep |
| **Sat** | **SPAR + rounds** | Bigger refeed — beef/mutton + greens (post-spar) | Oily fish + extra carbs (recovery) | hard body, eat well · omega-3 |
| **Sun** | Rest / mobility | Chicken / leftover stew + palak paneer | Fresh fish | **batch-cook** red meat + chicken · **D3 60K** with this fatty meal |

## 💊 Supplements (timing)

| When | What | Why |
|------|------|-----|
| 05:00 | Creatine 3–5 g + raw beetroot | brain ATP + nitrates before deep work / AM session |
| ~09:00 (breakfast) | Nurokind-OD B12 1500 mcg | myelin maintenance (not for cramps) |
| 20:50 | Mgmax 400 (magnesium bisglycinate) | pre-sleep CNS down-shift, ~30 min before lights-out |
| Sunday | Uprise-D3 60K | with a fatty meal (the Sunday fish/eggs). Repletion dose — retest at 8 weeks; past 50–60 ng/mL drop to 60K monthly |

Open adds (optional): K2 MK-7 to pair with the D3; collagen + vitamin C pre-training for joints. If you
hit oily fish 2–3×/week you can skip a fish-oil capsule.

## 🛒 Shopping & prep rhythm

- **Sunday batch-cook:** 2 red-meat stews (beef/mutton) + 2–3 chicken portions → covers Mon–Fri lunches.
  Buy the week's fish/prawns fresh (or freeze in single dinners).
- **Greens:** keep spinach/other leafy greens to stir into stews; one palak paneer in the week.
- **Organs:** one heart portion (Wed), ~100 g liver (Thu), one kidney portion (Fri) — buy with the
  Sunday meat run.

## 🗺️ How this maps to the app

The per-day **lunch / dinner / cue** live in `RoutineSeed.dietPlan()` and come back from
`GET /api/config` as `dietPlan` (reference content, like the journal prompts and the three rules). The
**This week** board on the home page renders them next to each day's training, with **today** highlighted.
To change the plan, edit `dietPlan()` in
[`app/src/main/java/com/ramishtaha/sahar/seed/RoutineSeed.java`](../app/src/main/java/com/ramishtaha/sahar/seed/RoutineSeed.java)
(it's not in the database — it's static guidance). The editable monthly fields (prayer times, the block,
the schedule) are still managed through `/admin.html`.

## 🔗 Related
- 📚 [Nutrition principles in the domain](./steps/03-model-the-domain.md) — where the diet sections are seeded
- 📄 [The README](../README.md) · ▶️ [Run the app](../README.md#running-it)
