package com.ramishtaha.sahar.service;

import com.ramishtaha.sahar.domain.PrayerTimes;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Computes prayer times for a location and date using the standard astronomical method
 * (see <a href="http://praytimes.org/calculation">praytimes.org/calculation</a>).
 *
 * <p>Conventions baked in to match the Sahar seed (Thane, Hanafi):
 * <ul>
 *   <li><b>Karachi</b> twilight: Fajr and Isha at an 18° sun depression.</li>
 *   <li><b>Hanafi</b> Asr: shadow factor 2 (an object's shadow equals twice its height, plus noon shadow).</li>
 * </ul>
 *
 * <p>It is a pure, stateless {@code @Component} (a "domain service") - no database, no HTTP. That makes it
 * trivial to unit-test, which is exactly what {@code PrayerTimeCalculatorTest} does against the known
 * Thane values. The web layer ({@code PrayerTimesController}) calls it with coordinates the browser
 * obtained from the Geolocation API.
 *
 * <p>All trigonometry here works in DEGREES (via the small {@code dsin}/{@code dcos}/... helpers), because
 * the astronomical formulas are written in degrees. Times come out as fractional hours of local clock
 * time and are formatted to "HH:mm".
 *
 * <p><b>High-latitude caveat:</b> near the poles in summer the sun may never dip 18° below the horizon, so
 * Fajr/Isha have no exact solution and the result degenerates (the hour-angle is clamped). For Sahar's use
 * (Thane, ~19°N) this never happens; real apps add a "higher-latitude" rule (e.g. middle-of-night, or
 * 1/7th of the night). That is a deliberate non-goal here - see docs/steps/15-geolocation-prayer-times.md.
 */
@Component
public class PrayerTimeCalculator {

    private static final double FAJR_ANGLE = 18.0;   // Karachi
    private static final double ISHA_ANGLE = 18.0;   // Karachi
    private static final double ASR_FACTOR = 2.0;    // Hanafi
    private static final double SUNRISE_DIP = 0.833; // refraction + sun's radius

    /**
     * @param lat     latitude in degrees (north positive), -90..90
     * @param lng     longitude in degrees (east positive), -180..180
     * @param tzHours timezone offset from UTC in hours (e.g. IST = 5.5)
     * @param date    the local date to compute for
     */
    public PrayerTimes calculate(double lat, double lng, double tzHours, LocalDate date) {
        // --- Sun position for the day (low-precision but plenty for prayer times) ---
        double jd = julianDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        double d = jd - 2451545.0;                                   // days since J2000.0
        double g = fixAngle(357.529 + 0.98560028 * d);               // mean anomaly
        double q = fixAngle(280.459 + 0.98564736 * d);               // mean longitude
        double l = fixAngle(q + 1.915 * dsin(g) + 0.020 * dsin(2 * g)); // ecliptic longitude
        double e = 23.439 - 0.00000036 * d;                          // obliquity of the ecliptic
        double ra = fixHour(datan2(dcos(e) * dsin(l), dcos(l)) / 15.0); // right ascension (hours)
        double decl = dasin(dsin(e) * dsin(l));                      // sun declination
        double eqt = q / 15.0 - ra;                                  // equation of time (hours)

        // --- Local solar noon, then symmetric offsets for each prayer ---
        double dhuhr = 12.0 + tzHours - lng / 15.0 - eqt;

        double fajr = dhuhr - hourAngleBelowHorizon(FAJR_ANGLE, lat, decl);
        double sunrise = dhuhr - hourAngleBelowHorizon(SUNRISE_DIP, lat, decl);
        double maghrib = dhuhr + hourAngleBelowHorizon(SUNRISE_DIP, lat, decl);
        double isha = dhuhr + hourAngleBelowHorizon(ISHA_ANGLE, lat, decl);

        // Asr: the sun's altitude when an object's shadow = factor*height + noon shadow.
        double asrAltitude = dacot(ASR_FACTOR + dtan(Math.abs(lat - decl)));
        double asr = dhuhr + hourAngleAboveHorizon(asrAltitude, lat, decl);

        String note = String.format(Locale.US,
                "Auto-computed for %.3f°, %.3f° on %s (Karachi 18° Fajr/Isha, Hanafi Asr). "
                        + "Re-check if you travel; review monthly.", lat, lng, date);

        return new PrayerTimes(fmt(fajr), fmt(sunrise), fmt(dhuhr), fmt(asr), fmt(maghrib), fmt(isha), note);
    }

    /** Hours from noon to when the sun is {@code angle} degrees BELOW the horizon (Fajr/Isha/sunrise/sunset). */
    private static double hourAngleBelowHorizon(double angle, double lat, double decl) {
        double x = (-dsin(angle) - dsin(lat) * dsin(decl)) / (dcos(lat) * dcos(decl));
        return darccos(clamp(x)) / 15.0;
    }

    /** Hours from noon to when the sun is at altitude {@code alt} degrees ABOVE the horizon (Asr). */
    private static double hourAngleAboveHorizon(double alt, double lat, double decl) {
        double x = (dsin(alt) - dsin(lat) * dsin(decl)) / (dcos(lat) * dcos(decl));
        return darccos(clamp(x)) / 15.0;
    }

    private static double clamp(double x) {
        return Math.max(-1.0, Math.min(1.0, x));
    }

    /** Fractional hours -> "HH:mm", rounded to the nearest minute, wrapped into 0..24h. */
    private static String fmt(double hours) {
        double h = ((hours % 24) + 24) % 24;
        int totalMinutes = (int) Math.round(h * 60.0);
        int hh = (totalMinutes / 60) % 24;
        int mm = totalMinutes % 60;
        return String.format("%02d:%02d", hh, mm);
    }

    private static double julianDate(int year, int month, int day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double a = Math.floor(year / 100.0);
        double b = 2 - a + Math.floor(a / 4.0);
        return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + b - 1524.5;
    }

    // --- degree-based trig helpers ---
    private static double dsin(double d) { return Math.sin(Math.toRadians(d)); }
    private static double dcos(double d) { return Math.cos(Math.toRadians(d)); }
    private static double dtan(double d) { return Math.tan(Math.toRadians(d)); }
    private static double dasin(double x) { return Math.toDegrees(Math.asin(x)); }
    private static double darccos(double x) { return Math.toDegrees(Math.acos(x)); }
    private static double datan2(double y, double x) { return Math.toDegrees(Math.atan2(y, x)); }
    private static double dacot(double x) { return Math.toDegrees(Math.atan(1.0 / x)); }
    private static double fixAngle(double a) { a -= 360.0 * Math.floor(a / 360.0); return a < 0 ? a + 360.0 : a; }
    private static double fixHour(double h) { h -= 24.0 * Math.floor(h / 24.0); return h < 0 ? h + 24.0 : h; }
}
