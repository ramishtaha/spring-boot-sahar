package win.l0ve.sahar.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import win.l0ve.sahar.domain.PrayerTimes;
import win.l0ve.sahar.service.RoutineService;

/**
 * Read and replace the month's prayer times.
 *
 * <ul>
 *   <li>{@code GET  /api/prayer-times} - return the current times.</li>
 *   <li>{@code PUT  /api/prayer-times} - replace them wholesale with the JSON body.</li>
 * </ul>
 *
 * <p>Why PUT, not POST? PUT means "make the resource at this URL equal to what I am sending" - a full
 * replacement that is <em>idempotent</em> (sending it twice leaves the same result). POST means
 * "create a new sub-resource / run a process" and is not idempotent. Updating the single prayer-times
 * resource is a textbook PUT.
 *
 * <p>{@code @RequestBody} tells Spring to take the JSON request body and deserialize it (via Jackson)
 * into a {@link PrayerTimes} record - the mirror image of what {@code @RestController} does on the way out.
 */
@RestController
public class PrayerTimesController {

    private final RoutineService routine;

    public PrayerTimesController(RoutineService routine) {
        this.routine = routine;
    }

    @GetMapping("/api/prayer-times")
    public PrayerTimes get() {
        return routine.getConfig().prayerTimes();
    }

    @PutMapping("/api/prayer-times")
    public PrayerTimes update(@RequestBody PrayerTimes prayerTimes) {
        return routine.updatePrayerTimes(prayerTimes).prayerTimes();
    }
}
