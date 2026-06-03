package com.ramishtaha.sahar.seed;

import com.ramishtaha.sahar.domain.*;

import java.util.List;

/**
 * The single source of seed data: the real Sahar routine, built as a {@link RoutineConfig}.
 *
 * <p>For now this is the app's only data. Steps 04-05 will let a service hold and edit a copy of it
 * in memory; step 06 moves the structured, editable parts into a database and this class becomes the
 * "first run" seed; step 10 moves that seed into a Flyway migration.
 *
 * <p>It is a {@code final} class with a private constructor and one static factory - a plain
 * "no instances, just a factory" utility. {@code defaultConfig()} returns a fresh tree every call.
 */
public final class RoutineSeed {

    private RoutineSeed() {
    }

    public static RoutineConfig defaultConfig() {
        return new RoutineConfig(
                "Sahar",
                "recover, build, fight",
                "June 2026",
                prayerTimes(),
                block(),
                weeklyGrid(),
                schedule(),
                supplements(),
                diet(),
                journal(),
                threeRules(),
                weekend(),
                notes(),
                dietPlan()
        );
    }

    private static PrayerTimes prayerTimes() {
        return new PrayerTimes(
                "04:37", "05:59", "12:37", "17:12", "19:13", "20:36",
                "Karachi 18° Fajr/Isha, Hanafi Asr, computed for Thane (19.22N, 72.98E). "
                        + "Recheck monthly; drifts under ~10 min across a month.");
    }

    private static BlockPlan block() {
        return new BlockPlan(
                "4-week block · 8 Jun – 5 Jul (Foundation · Build · Peak · Deload)",
                List.of(
                        new Week(1, "Foundation", "8 Jun", "14 Jun",
                                "Moderate, no PM sessions; lock the schedule and the journaling habit.",
                                "Spring Boot core: setup, dependency injection, controllers, a CRUD REST API.",
                                false),
                        new Week(2, "Build", "15 Jun", "21 Jun",
                                "Add PM strength Mon and Thu; deeper deep-work; volume climbs.",
                                "Persistence: Spring Data JPA, Postgres, repositories, validation.",
                                false),
                        new Week(3, "Peak", "22 Jun", "28 Jun",
                                "Full volume, sharpest spar, deepest learning.",
                                "Docker and DevOps: Dockerfile, compose, env config, basic CI.",
                                false),
                        new Week(4, "Deload", "29 Jun", "5 Jul",
                                "MMA down ~40%, light or skipped spar, weekday mains drilling only, more sleep.",
                                "Ship it: deploy the container, refactor, docs.",
                                true)
                ));
    }

    private static List<GridDay> weeklyGrid() {
        return List.of(
                new GridDay("Mon", "Muay Thai (technical)", null),
                new GridDay("Tue", "Boxing", null),
                new GridDay("Wed", "Wrestling / BJJ (light)", null),
                new GridDay("Thu", "Muay Thai", null),
                new GridDay("Fri", "Padwork + light drills (taper)", null),
                new GridDay("Sat", "SPAR + BJJ / MMA rounds", null),
                new GridDay("Sun", "Rest / mobility walk", null)
        );
    }

    private static List<ScheduleItem> schedule() {
        return List.of(
                item("04:15", "Wake, water, wudu, Tahajjud, Fajr", "500ml water · 20-min Tahajjud", "pray"),
                item("04:50", "Morning bullet-journal log", "5–10 min", "journal"),
                item("05:00", "Deep work — backend", "120 min · backend focus per the current week", "work"),
                item("07:30", "MMA AM session", "discipline per the weekly grid", "train"),
                item("08:45", "Shower, breakfast, B12", "eggs in ghee + pumpkin seeds · Nurokind-OD B12", "meal"),
                item("09:30", "Buffer — mobility & admin", null, "admin"),
                item("11:15", "Commute", null, "admin"),
                item("11:30", "Work block 1", "Dhuhr enters 12:37", "work"),
                item("13:30", "Home, lunch, Qailulah nap", "20–30 min nap · load-bearing", "meal"),
                item("14:20", "Work block 2", "Asr 17:12", "work"),
                item("18:20", "Optional PM session", "Mon & Thu, Build/Peak weeks only · first to cut when tired", "train"),
                item("19:13", "Maghrib", null, "pray"),
                item("19:30", "Light dinner", "fish / prawns / light · ~2h before sleep", "meal"),
                item("20:36", "Isha", null, "pray"),
                item("20:50", "Magnesium — Mgmax 400", "~30 min pre-sleep", "supp"),
                item("21:00", "Night bullet-journal log", "5–10 min", "journal"),
                item("21:20", "Lights out", "~7h sleep", "sleep")
        );
    }

    private static ScheduleItem item(String time, String title, String detail, String category) {
        return new ScheduleItem(null, time, title, detail, category, "weekday");
    }

    private static List<Supplement> supplements() {
        return List.of(
                new Supplement("05:00", "Creatine + raw beetroot", "3–5 g",
                        "brain ATP + nitrates", null),
                new Supplement("~09:00 with breakfast", "Nurokind-OD B12", "1500 mcg",
                        "myelin maintenance", "not for cramps"),
                new Supplement("20:50", "Mgmax 400 (magnesium bisglycinate)", "400 mg",
                        "pre-sleep CNS down-shift", "~30 min before lights-out"),
                new Supplement("Sunday", "Uprise-D3 60K", "60,000 IU",
                        "vitamin D repletion", "With a fatty meal. 60K weekly is a repletion dose, not "
                        + "maintenance; retest at 8 weeks; past 50–60 ng/mL drop to 60K monthly. Open adds: "
                        + "omega-3 (or oily fish 2–3x/week), K2 MK-7 to pair with D3, collagen + vitamin C pre-training.")
        );
    }

    private static List<DietSection> diet() {
        return List.of(
                new DietSection(1, "Breakfast", "3–4 whole eggs in ghee + raw pumpkin seeds."),
                new DietSection(2, "Lunch (heavy)", "Rotate beef/mutton (~2x/week) and chicken (~2–3x) "
                        + "with leafy greens; batch-cooks well."),
                new DietSection(3, "Dinner (light)", "Oily fish, prawns, white fish, or light chicken; "
                        + "digests faster than red meat, protects deep sleep."),
                new DietSection(4, "Organ meats", "Heart: eat freely (CoQ10, taurine, B12, lean). "
                        + "Liver: cap at ~100g once a week (vitamin A). Kidney: once a week (selenium, B12). "
                        + "Brain: small prion risk and you guard your CNS, so get DHA from fish; bheja occasionally only."),
                new DietSection(5, "Vegetables (the minimum)", "Organs cover most micronutrients, so veg is for "
                        + "fibre and vitamin C. Hide it in stews; palak paneer (the paneer's calcium binds spinach's "
                        + "oxalate, lower stone risk); rotate greens."),
                new DietSection(6, "Omega-3 from food", "Oily fish 2–3x/week; bangda (mackerel) is cheapest and "
                        + "highest, then surmai and rawas. Batch rule: batch red meat and chicken; cook fish and prawns fresh.")
        );
    }

    private static JournalPrompts journal() {
        return new JournalPrompts(
                List.of(
                        "Sleep last night (1–5)",
                        "Resting HR",
                        "Top 3 priorities",
                        "Backend focus for the 5am sprint",
                        "Training intent",
                        "Intention / niyyah"
                ),
                List.of(
                        "Deep work done — what I built or learned",
                        "How body/CNS felt (1–5)",
                        "Wins today",
                        "One thing to improve tomorrow",
                        "Tomorrow's number one",
                        "Were the 5 prayers on time"
                ));
    }

    private static List<String> threeRules() {
        return List.of(
                "One maxed system per day. Spar is Saturday, so Saturday is hard-body and easy-brain; Sunday flips it.",
                "Wave the load: Foundation, Build, Peak, Deload.",
                "The real CNS threat is head impact, not a missing supplement. Weekly spar is low concussive load; keep it there."
        );
    }

    private static WeekendProtocol weekend() {
        return new WeekendProtocol(
                "Hard body, easy brain — spar plus rounds in the morning; study is applied only "
                        + "(build, debug, ship), no new material.",
                "Deep brain, rested body — 4–5 hours of real focus then build a project; full physical rest, "
                        + "mobility walk; Uprise-D3 60K with a fatty meal; batch-cook the week's red meat and chicken stews, "
                        + "buy fish fresh.");
    }

    private static List<String> notes() {
        return List.of(
                "Sleep ceiling ~7h, fixed by Fajr 04:37 and Isha 20:36; the midday Qailulah is load-bearing; defend lights-out.",
                "Deload every 4th week: the body deloads, not the brain — MMA volume down ~40%, drop PM sessions, "
                        + "mains drilling only, spar light or skipped, add sleep.",
                "Autoregulation: if resting HR is up 5–7 bpm over baseline, or grip/strength is down, Wednesday becomes full rest."
        );
    }

    /**
     * A suggested week of meals derived from the nutrition rules: heavy lunch / light dinner, organ meats
     * scheduled (heart freely Wed, liver ~100g Thu, kidney Fri), oily fish dinners for omega-3 (Mon/Thu/Sat),
     * red meat lunches ~2x and chicken ~2-3x, greens daily, and the Sunday batch-cook + D3. See
     * docs/diet-plan.md for the full plan and the reasoning.
     */
    private static List<MealDay> dietPlan() {
        return List.of(
                new MealDay("Mon", "Beef/mutton stew (Sun batch) + leafy greens",
                        "Oily fish — bangda/mackerel", "Muay Thai · omega-3"),
                new MealDay("Tue", "Chicken (batch) + palak / greens",
                        "Prawns or white fish", "Boxing"),
                new MealDay("Wed", "Beef/mutton + heart + greens",
                        "Light chicken or surmai", "Wrestling/BJJ · heart (eat freely)"),
                new MealDay("Thu", "Chicken + liver (~100g, weekly) + greens",
                        "Oily fish — rawas", "Muay Thai · liver day · omega-3"),
                new MealDay("Fri", "Mutton/chicken (lighter) + kidney (weekly) + greens",
                        "White fish / prawns (light)", "Padwork taper · kidney day · protect sleep"),
                new MealDay("Sat", "Bigger refeed — beef/mutton + greens (post-spar)",
                        "Oily fish + extra carbs (recovery)", "SPAR · hard body, eat well · omega-3"),
                new MealDay("Sun", "Chicken / leftover stew + palak paneer",
                        "Fresh fish", "Rest · BATCH-COOK red meat + chicken · D3 60K with this fatty meal")
        );
    }
}
