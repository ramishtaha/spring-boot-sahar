package com.ramishtaha.sahar.domain;

/**
 * The location prayer times are computed for. The user controls it: refresh from the device,
 * search a place name, or pinpoint it on a map. It is persisted (in app_meta) so the app remembers it
 * and the home page can show it.
 *
 * @param placeName    a human label, e.g. "Thane, Maharashtra, India" (from reverse geocoding)
 * @param lat          latitude, degrees north
 * @param lng          longitude, degrees east
 * @param tzOffset     timezone offset from UTC, in minutes east (IST = 330)
 */
public record Location(
        String placeName,
        double lat,
        double lng,
        int tzOffset
) {
}
