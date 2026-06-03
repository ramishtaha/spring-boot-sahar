package com.ramishtaha.sahar.domain;

import java.util.List;

/**
 * The aggregate root of the whole app: everything {@code GET /api/config} returns.
 *
 * <p>This single record composes all the smaller ones. Because it (and everything it holds)
 * is a record, Jackson serializes the entire tree to JSON automatically, and the shape is
 * guaranteed by the compiler - the exact opposite of the untyped {@code Map} we started with
 * in step 02.
 *
 * <p>Some fields are <em>editable monthly</em> (month, prayerTimes, block, schedule); the rest
 * (weeklyGrid, supplements, diet, journal, threeRules, weekend, notes) are reference content the
 * app displays. Later steps persist the editable parts in a database.
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
        List<String> notes
) {
    // Records are immutable: you cannot change a field in place. The idiomatic way to "edit" one is
    // to build a NEW instance that copies every field except the one you are changing. These small
    // "wither" helpers keep that copy-and-replace in one place so the service stays readable.
    // (Java may add automatic "withers" for records in a future release; until then, we write them.)

    public RoutineConfig withMonth(String newMonth) {
        return new RoutineConfig(title, tagline, newMonth, prayerTimes, block, weeklyGrid, schedule,
                supplements, diet, journal, threeRules, weekend, notes);
    }

    public RoutineConfig withPrayerTimes(PrayerTimes newPrayerTimes) {
        return new RoutineConfig(title, tagline, month, newPrayerTimes, block, weeklyGrid, schedule,
                supplements, diet, journal, threeRules, weekend, notes);
    }

    public RoutineConfig withBlock(BlockPlan newBlock) {
        return new RoutineConfig(title, tagline, month, prayerTimes, newBlock, weeklyGrid, schedule,
                supplements, diet, journal, threeRules, weekend, notes);
    }

    public RoutineConfig withSchedule(List<ScheduleItem> newSchedule) {
        return new RoutineConfig(title, tagline, month, prayerTimes, block, weeklyGrid, newSchedule,
                supplements, diet, journal, threeRules, weekend, notes);
    }
}
