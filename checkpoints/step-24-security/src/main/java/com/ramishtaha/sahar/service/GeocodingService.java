package com.ramishtaha.sahar.service;

import com.ramishtaha.sahar.config.GeocodingProperties;
import com.ramishtaha.sahar.domain.GeoResult;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
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
 *       can set that reliably AND cache results so we stay well under the rate limit. We do both here.</li>
 *   <li>It introduces Spring Boot 4's {@link RestClient} - the modern, fluent HTTP client for calling other
 *       services.</li>
 * </ol>
 *
 * <p><b>Hardened in step 21</b> with three production habits for any outbound call:
 * <ul>
 *   <li><b>Type-safe config</b> - base URL, user-agent, timeouts and retry count come from
 *       {@link GeocodingProperties} ({@code sahar.geocoding.*}), not hard-coded literals.</li>
 *   <li><b>Timeouts</b> - a slow third party must never hang our request thread forever, so we set a
 *       connect and a read timeout on the HTTP client.</li>
 *   <li><b>Retry + caching</b> - we retry a failed call a couple of times, and {@code @Cacheable} memoises
 *       successful answers (Caffeine), so repeat lookups never touch the network.</li>
 * </ul>
 *
 * <p>If Nominatim is still unreachable after retries, the methods degrade gracefully rather than throwing:
 * search returns an empty list, reverse falls back to the raw coordinates as the name.
 */
@Service
public class GeocodingService {

    private final RestClient client;
    private final int maxRetries;

    public GeocodingService(GeocodingProperties props) {
        // A slow or hung third party must not pin our request thread. SimpleClientHttpRequestFactory lets us
        // cap how long we wait to connect and to read the response.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(props.connectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(props.readTimeoutMs()));

        this.client = RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader("User-Agent", props.userAgent())
                .requestFactory(factory)
                .build();
        this.maxRetries = props.maxRetries();
    }

    /**
     * Search by free text, e.g. "Thane" or "Mecca". Returns up to a handful of matches.
     *
     * <p>{@code @Cacheable} memoises the answer per query in the "geocode" cache; {@code unless} keeps us from
     * caching a failure (an empty list), so a transient outage can't poison the cache.
     */
    @Cacheable(cacheNames = "geocode", key = "#query", unless = "#result.isEmpty()")
    public List<GeoResult> search(String query) {
        List<Map<String, Object>> raw = withRetry(() -> client.get()
                .uri(b -> b.path("/search")
                        .queryParam("q", query)
                        .queryParam("format", "jsonv2")
                        .queryParam("limit", 6)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {}));
        if (raw == null) return List.of(); // offline / rate-limited / bad response -> no suggestions
        return raw.stream().map(GeocodingService::toResult).filter(Objects::nonNull).toList();
    }

    /** Look up a human label for a pinned point. Falls back to the coordinates if the lookup fails. */
    @Cacheable(cacheNames = "reverse", key = "#lat + ',' + #lng")
    public GeoResult reverse(double lat, double lng) {
        Map<String, Object> m = withRetry(() -> client.get()
                .uri(b -> b.path("/reverse")
                        .queryParam("lat", lat)
                        .queryParam("lon", lng)
                        .queryParam("format", "jsonv2")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {}));
        GeoResult r = toResult(m);
        if (r != null) return r;
        return new GeoResult(round(lat) + ", " + round(lng), lat, lng);
    }

    /**
     * Run a call, retrying a few times on failure. Returns {@code null} once all attempts are exhausted so the
     * callers can apply their graceful fallback. A real system would also back off between attempts; we keep it
     * to a tight loop because Nominatim is best-effort and the request is interactive.
     */
    private <T> T withRetry(java.util.function.Supplier<T> call) {
        RuntimeException last = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return call.get();
            } catch (RuntimeException ex) {
                last = ex; // timeout, 5xx, connection refused - try again
            }
        }
        return null; // all attempts failed (last holds the cause if we ever want to log it)
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
