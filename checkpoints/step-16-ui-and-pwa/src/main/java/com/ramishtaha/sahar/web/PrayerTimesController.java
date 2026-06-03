package com.ramishtaha.sahar.web;

import com.ramishtaha.sahar.domain.PrayerTimes;
import com.ramishtaha.sahar.service.PrayerTimeCalculator;
import com.ramishtaha.sahar.service.RoutineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Read, replace, and CALCULATE the month's prayer times.
 *
 * <ul>
 *   <li>{@code GET /api/prayer-times} - the current (saved) times.</li>
 *   <li>{@code PUT /api/prayer-times} - replace them wholesale (idempotent).</li>
 *   <li>{@code GET /api/prayer-times/calculate?lat=&lng=&tz=&date=} - compute times for a location WITHOUT
 *       saving them. The browser gets {@code lat}/{@code lng} from the Geolocation API and {@code tz}
 *       (minutes east of UTC) from the device; the user reviews the result and can then PUT it to save.</li>
 * </ul>
 *
 * <p>Why PUT for the update and GET for calculate? PUT replaces the resource and is idempotent. Calculate
 * is a pure, read-only computation with no side effects, so GET with query params is the natural fit.
 *
 * <p>{@link PrayerTimeCalculator} is a stateless domain-service bean injected alongside the
 * {@link RoutineService}; the controller just validates inputs and delegates.
 */
@RestController
public class PrayerTimesController {

    private final RoutineService routine;
    private final PrayerTimeCalculator calculator;

    public PrayerTimesController(RoutineService routine, PrayerTimeCalculator calculator) {
        this.routine = routine;
        this.calculator = calculator;
    }

    @GetMapping("/api/prayer-times")
    public PrayerTimes get() {
        return routine.getPrayerTimes();
    }

    @PutMapping("/api/prayer-times")
    public PrayerTimes update(@Valid @RequestBody PrayerTimes prayerTimes) {
        return routine.updatePrayerTimes(prayerTimes);
    }

    @GetMapping("/api/prayer-times/calculate")
    public PrayerTimes calculate(@RequestParam double lat,
                                 @RequestParam double lng,
                                 @RequestParam(defaultValue = "0") int tz,
                                 @RequestParam(required = false) String date) {
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lat must be -90..90 and lng -180..180");
        }
        if (tz < -720 || tz > 840) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tz (minutes from UTC) is out of range");
        }
        LocalDate day;
        try {
            day = (date == null || date.isBlank()) ? LocalDate.now() : LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date must be yyyy-MM-dd");
        }
        return calculator.calculate(lat, lng, tz / 60.0, day);
    }
}
