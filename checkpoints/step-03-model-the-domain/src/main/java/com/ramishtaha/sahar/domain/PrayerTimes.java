package com.ramishtaha.sahar.domain;

/**
 * The five daily prayers (plus sunrise) for the month, and the calculation note.
 *
 * <p>This is a Java <em>record</em> - a compact, immutable data carrier introduced in Java 16.
 * Writing {@code record PrayerTimes(String fajr, ...)} generates, for free:
 * a private final field per component, a canonical constructor, public accessors
 * ({@code fajr()}, {@code isha()}, ...), and value-based {@code equals}/{@code hashCode}/{@code toString}.
 *
 * <p>Records are ideal for a domain model that is mostly "data with a shape": Jackson can serialize
 * them to JSON out of the box (it reads the component names), and immutability means once the server
 * hands one out, nobody can mutate it by accident.
 *
 * <p>Times are kept as {@code String} ("HH:mm") rather than {@code LocalTime} on purpose - they are
 * display values edited by hand, and step 05 adds validation that the strings are well-formed.
 */
public record PrayerTimes(
        String fajr,
        String sunrise,
        String dhuhr,
        String asr,
        String maghrib,
        String isha,
        String methodNote
) {
}
