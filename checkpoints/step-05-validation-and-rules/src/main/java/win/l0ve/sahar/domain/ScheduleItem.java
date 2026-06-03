package win.l0ve.sahar.domain;

/**
 * A single entry in the daily timeline.
 *
 * @param time     start time, "HH:mm"
 * @param title    the headline of the slot
 * @param detail   optional secondary text (may be null)
 * @param category one of: pray, journal, work, train, meal, admin, supp, sleep
 *                 (the frontend colour-codes the timeline by this)
 * @param dayType  "weekday" or "weekend"
 */
public record ScheduleItem(
        String time,
        String title,
        String detail,
        String category,
        String dayType
) {
}
