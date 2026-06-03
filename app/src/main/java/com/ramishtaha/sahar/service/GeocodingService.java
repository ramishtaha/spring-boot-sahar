package com.ramishtaha.sahar.service;

import com.ramishtaha.sahar.domain.GeoResult;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Turns place names into coordinates (search) and coordinates into place names (reverse), by calling
 * OpenStreetMap's free <a href="https://nominatim.org/">Nominatim</a> service.
 *
 * <p>Why server-side and not straight from the browser? Two good reasons, both worth learning:
 * <ol>
 *   <li>Nominatim's usage policy asks callers to identify themselves with a {@code User-Agent}; a server
 *       can set that reliably (and could add caching / rate-limiting). We do that here.</li>
 *   <li>It introduces Spring Boot 4's {@link RestClient} - the modern, fluent HTTP client for calling other
 *       services. Spring auto-configures a {@code RestClient.Builder} bean we just customise.</li>
 * </ol>
 *
 * <p>If Nominatim is unreachable (offline, rate-limited), the methods degrade gracefully rather than
 * throwing: search returns an empty list, reverse falls back to the raw coordinates as the name.
 *
 * <p>Note: Nominatim allows ~1 request/second for light use. For anything heavier you would cache results
 * or run your own instance.
 */
@Service
public class GeocodingService {

    private final RestClient client;

    public GeocodingService() {
        // RestClient.builder() is the static factory - no bean wiring needed. (Spring Boot also offers an
        // auto-configured RestClient.Builder bean when the right module is present; the static factory keeps
        // this service self-contained.)
        this.client = RestClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader("User-Agent", "Sahar/1.0 (https://github.com/ramishtaha/spring-boot-sahar)")
                .build();
    }

    /** Search by free text, e.g. "Thane" or "Mecca". Returns up to a handful of matches. */
    public List<GeoResult> search(String query) {
        try {
            List<Map<String, Object>> raw = client.get()
                    .uri(b -> b.path("/search")
                            .queryParam("q", query)
                            .queryParam("format", "jsonv2")
                            .queryParam("limit", 6)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            if (raw == null) return List.of();
            return raw.stream().map(GeocodingService::toResult).filter(Objects::nonNull).toList();
        } catch (RuntimeException ex) {
            return List.of(); // offline / rate-limited / bad response -> no suggestions
        }
    }

    /** Look up a human label for a pinned point. Falls back to the coordinates if the lookup fails. */
    public GeoResult reverse(double lat, double lng) {
        try {
            Map<String, Object> m = client.get()
                    .uri(b -> b.path("/reverse")
                            .queryParam("lat", lat)
                            .queryParam("lon", lng)
                            .queryParam("format", "jsonv2")
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            GeoResult r = toResult(m);
            if (r != null) return r;
        } catch (RuntimeException ignored) {
            // fall through to the coordinate fallback
        }
        return new GeoResult(round(lat) + ", " + round(lng), lat, lng);
    }

    private static GeoResult toResult(Map<String, Object> m) {
        if (m == null || m.get("lat") == null || m.get("lon") == null) return null;
        try {
            double lat = Double.parseDouble(m.get("lat").toString());
            double lng = Double.parseDouble(m.get("lon").toString());
            Object name = m.get("display_name");
            return new GeoResult(name != null ? name.toString() : (round(lat) + ", " + round(lng)), lat, lng);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static double round(double d) {
        return Math.round(d * 1000.0) / 1000.0;
    }
}
