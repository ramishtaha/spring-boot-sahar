package com.ramishtaha.sahar.service;

import com.ramishtaha.sahar.domain.PrayerTimes;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A plain unit test (no Spring context) for the prayer-time math. It pins the algorithm to the known
 * Thane values from the seed - the same numbers the routine was originally computed with - so an
 * accidental change to the formulas is caught immediately.
 *
 * <p>This is the kind of fast, focused test the roadmap (step 99) points you toward.
 */
class PrayerTimeCalculatorTest {

    private final PrayerTimeCalculator calc = new PrayerTimeCalculator();

    @Test
    void thaneMidJuneIsCloseToTheSeededTimes() {
        // Thane: 19.22N, 72.98E, India Standard Time = UTC+5:30.
        PrayerTimes pt = calc.calculate(19.22, 72.98, 5.5, LocalDate.of(2026, 6, 15));

        // Seeded reference times for June 2026 (note drifts <~10 min across the month).
        assertWithin(pt.fajr(), "04:37", 10);
        assertWithin(pt.sunrise(), "05:59", 10);
        assertWithin(pt.dhuhr(), "12:37", 10);
        assertWithin(pt.asr(), "17:12", 10);
        assertWithin(pt.maghrib(), "19:13", 10);
        assertWithin(pt.isha(), "20:36", 10);
    }

    @Test
    void timesAreInChronologicalOrder() {
        PrayerTimes pt = calc.calculate(19.22, 72.98, 5.5, LocalDate.of(2026, 6, 15));
        assertThat(minutes(pt.fajr()))
                .isLessThan(minutes(pt.sunrise()));
        assertThat(minutes(pt.sunrise()))
                .isLessThan(minutes(pt.dhuhr()));
        assertThat(minutes(pt.dhuhr()))
                .isLessThan(minutes(pt.asr()));
        assertThat(minutes(pt.asr()))
                .isLessThan(minutes(pt.maghrib()));
        assertThat(minutes(pt.maghrib()))
                .isLessThan(minutes(pt.isha()));
    }

    private static void assertWithin(String actual, String expected, int toleranceMinutes) {
        int diff = Math.abs(minutes(actual) - minutes(expected));
        assertThat(diff)
                .as("%s should be within %d min of %s (was %s)", actual, toleranceMinutes, expected, actual)
                .isLessThanOrEqualTo(toleranceMinutes);
    }

    private static int minutes(String hhmm) {
        String[] parts = hhmm.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
}
