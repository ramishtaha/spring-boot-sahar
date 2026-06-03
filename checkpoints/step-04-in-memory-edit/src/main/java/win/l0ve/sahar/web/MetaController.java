package win.l0ve.sahar.web;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import win.l0ve.sahar.service.RoutineService;

import java.util.Map;

/**
 * Edits the small "meta" fields. For now that is just the month label.
 *
 * <ul>
 *   <li>{@code PUT /api/month} with body {@code { "month": "July 2026" }} - change the month label.</li>
 * </ul>
 *
 * <p>It returns a tiny JSON object echoing the new value. Returning the updated state from a PUT is a
 * common, friendly convention: the client does not have to issue a follow-up GET to see the result.
 */
@RestController
public class MetaController {

    private final RoutineService routine;

    public MetaController(RoutineService routine) {
        this.routine = routine;
    }

    @PutMapping("/api/month")
    public Map<String, String> updateMonth(@RequestBody MonthUpdate body) {
        routine.updateMonth(body.month());
        return Map.of("month", routine.getConfig().month());
    }
}
