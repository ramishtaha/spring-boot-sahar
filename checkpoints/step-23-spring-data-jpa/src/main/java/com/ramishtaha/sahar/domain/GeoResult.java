package com.ramishtaha.sahar.domain;

/**
 * A geocoding result: a place name and its coordinates. Returned by the search and reverse-lookup
 * endpoints so the UI can let the user pick a place or label a pinned point.
 */
public record GeoResult(
        String name,
        double lat,
        double lng
) {
}
