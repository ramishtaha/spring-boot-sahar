package win.l0ve.sahar.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * A single entry in the daily timeline.
 *
 * <p>Step 07 adds an {@code id}: to update or delete one specific slot, the client needs to name it,
 * and the database row's primary key is that name. It is {@code null} on the way in when CREATING a new
 * item (the database assigns it), and populated on the way out.
 *
 * <p>The same field constraints from step 05 apply here, so creating/updating a slot with a bad time or
 * a blank title is rejected with a 400.
 *
 * @param id       database id; null when creating, set when reading
 * @param time     start time, "HH:mm"
 * @param title    the headline of the slot
 * @param detail   optional secondary text (may be null)
 * @param category one of: pray, journal, work, train, meal, admin, supp, sleep
 * @param dayType  "weekday" or "weekend"
 */
public record ScheduleItem(
        Long id,
        @NotBlank @Pattern(regexp = PrayerTimes.TIME, message = "time must be like 06:30") String time,
        @NotBlank(message = "title is required") String title,
        String detail,
        @NotBlank(message = "category is required") String category,
        String dayType
) {
}
