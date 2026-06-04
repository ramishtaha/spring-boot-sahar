package com.ramishtaha.sahar.web;

import com.ramishtaha.sahar.domain.Location;
import com.ramishtaha.sahar.service.RoutineService;
import com.ramishtaha.sahar.error.BadRequestException;
import org.springframework.web.bind.annotation.*;

/**
 * Read and set the location prayer times are computed for.
 *
 * <ul>
 *   <li>{@code GET /api/location} - the current location.</li>
 *   <li>{@code PUT /api/location} - set it (from geolocation, search, or a map pin).</li>
 * </ul>
 *
 * The location also rides along in {@code GET /api/config} as {@code location}, so the home page shows it
 * without a second request.
 */
@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final RoutineService routine;

    public LocationController(RoutineService routine) {
        this.routine = routine;
    }

    @GetMapping
    public Location get() {
        return routine.getLocation();
    }

    @PutMapping
    public Location update(@RequestBody Location location) {
        if (location.lat() < -90 || location.lat() > 90 || location.lng() < -180 || location.lng() > 180) {
            throw new BadRequestException("lat must be -90..90 and lng -180..180");
        }
        if (location.tzOffset() < -720 || location.tzOffset() > 840) {
            throw new BadRequestException("tzOffset (minutes from UTC) is out of range");
        }
        return routine.updateLocation(location);
    }
}
