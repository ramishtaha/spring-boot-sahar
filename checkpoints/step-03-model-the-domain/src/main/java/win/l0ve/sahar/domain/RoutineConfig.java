package win.l0ve.sahar.domain;

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
}
