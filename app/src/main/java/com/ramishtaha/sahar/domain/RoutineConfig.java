package com.ramishtaha.sahar.domain;

import java.util.List;

/**
 * The aggregate root of the whole app: everything {@code GET /api/config} returns.
 *
 * <p>This single record composes all the smaller ones. Because it (and everything it holds)
 * is a record, Jackson serializes the entire tree to JSON automatically, and the shape is
 * guaranteed by the compiler.
 *
 * <p>Some fields are <em>editable monthly</em> (month, prayerTimes, block, schedule); the rest
 * (weeklyGrid, supplements, diet, journal, threeRules, weekend, notes, dietPlan) are reference content
 * the app displays. The editable parts live in the database; the reference content comes from the seed.
 */
public record RoutineConfig(
        String title,
        String tagline,
        String month,
        PrayerTimes prayerTimes,
        BlockPlan block,
        List<GridDay> weeklyGrid,
        List<ScheduleItem> schedule,
        List<Supplement> supplements,
        List<DietSection> diet,
        JournalPrompts journal,
        List<String> threeRules,
        WeekendProtocol weekend,
        List<String> notes,
        List<MealDay> dietPlan
) {
}
