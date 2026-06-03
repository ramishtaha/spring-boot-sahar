package win.l0ve.sahar.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * The five daily prayers (plus sunrise) for the month, and the calculation note.
 *
 * <p>The constraint annotations on the components are <em>Bean Validation</em> (the {@code jakarta.validation}
 * standard, implemented by Hibernate Validator, which {@code spring-boot-starter-validation} brings in).
 * They do nothing on their own - they describe rules. They are <em>enforced</em> when something annotated
 * {@code @Valid} is validated, which in our app happens when the controller receives one of these as a
 * request body (see {@code PrayerTimesController}).
 *
 * <ul>
 *   <li>{@code @NotBlank} - not null, and not just whitespace.</li>
 *   <li>{@code @Pattern} - must match the 24-hour {@code HH:mm} regex (00:00 .. 23:59).</li>
 * </ul>
 *
 * <p>{@code methodNote} is free text, so it carries no constraints.
 */
public record PrayerTimes(
        @NotBlank @Pattern(regexp = TIME, message = "Fajr must be a 24-hour time like 04:37") String fajr,
        @NotBlank @Pattern(regexp = TIME, message = "Sunrise must be a 24-hour time like 05:59") String sunrise,
        @NotBlank @Pattern(regexp = TIME, message = "Dhuhr must be a 24-hour time like 12:37") String dhuhr,
        @NotBlank @Pattern(regexp = TIME, message = "Asr must be a 24-hour time like 17:12") String asr,
        @NotBlank @Pattern(regexp = TIME, message = "Maghrib must be a 24-hour time like 19:13") String maghrib,
        @NotBlank @Pattern(regexp = TIME, message = "Isha must be a 24-hour time like 20:36") String isha,
        String methodNote
) {
    /** 24-hour HH:mm, leading zero required: 00:00 through 23:59. */
    public static final String TIME = "^([01]\\d|2[0-3]):[0-5]\\d$";
}
