package com.ramishtaha.sahar.web;

import com.ramishtaha.sahar.domain.GeoResult;
import com.ramishtaha.sahar.service.GeocodingService;
import com.ramishtaha.sahar.error.BadRequestException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Geocoding for the location picker.
 *
 * <ul>
 *   <li>{@code GET /api/geocode?q=thane} - search a place name, returns candidate {@link GeoResult}s.</li>
 *   <li>{@code GET /api/geocode/reverse?lat=&lng=} - the place name for a pinned point.</li>
 * </ul>
 *
 * Both are thin wrappers over {@link GeocodingService}; the actual call to OpenStreetMap happens there.
 */
@RestController
@RequestMapping("/api/geocode")
public class GeocodeController {

    private final GeocodingService geocoding;

    public GeocodeController(GeocodingService geocoding) {
        this.geocoding = geocoding;
    }

    @GetMapping
    public List<GeoResult> search(@RequestParam String q) {
        if (q == null || q.isBlank()) {
            throw new BadRequestException("q (the place to search) is required");
        }
        return geocoding.search(q);
    }

    @GetMapping("/reverse")
    public GeoResult reverse(@RequestParam double lat, @RequestParam double lng) {
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new BadRequestException("lat must be -90..90 and lng -180..180");
        }
        return geocoding.reverse(lat, lng);
    }
}
