package win.l0ve.sahar.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import win.l0ve.sahar.domain.RoutineConfig;
import win.l0ve.sahar.seed.RoutineSeed;

/**
 * {@code GET /api/config} returns the whole routine as JSON.
 *
 * <p>Compare this to step 02: the giant, error-prone {@code Map<String, Object>} is gone. The method
 * now returns a {@link RoutineConfig} record - a typed tree the compiler checks for us. Jackson still
 * does the JSON serialization automatically, walking the record components, so the response shape is
 * identical to before. This is the payoff of domain modelling: same JSON, far safer code.
 *
 * <p>The data still comes straight from {@link RoutineSeed}. It is read-only and resets on every
 * request because there is nothing holding state yet - step 04 introduces a service that keeps an
 * editable copy in memory.
 */
@RestController
public class ConfigController {

    @GetMapping("/api/config")
    public RoutineConfig config() {
        return RoutineSeed.defaultConfig();
    }
}
