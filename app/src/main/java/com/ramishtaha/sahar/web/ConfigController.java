package com.ramishtaha.sahar.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ramishtaha.sahar.domain.RoutineConfig;
import com.ramishtaha.sahar.service.RoutineService;

/**
 * {@code GET /api/config} returns the current routine as JSON.
 *
 * <p>The data no longer comes straight from the seed. It comes from {@link RoutineService}, the
 * single bean that holds the editable copy. So if you PUT a new prayer time, the very next GET here
 * reflects it - they share the same service instance.
 *
 * <p>Note the constructor: Spring sees that {@code ConfigController} needs a {@code RoutineService}
 * and hands it the one it already created. That is <em>constructor injection</em> - the preferred
 * form of dependency injection. We never call {@code new RoutineService()} ourselves.
 */
@RestController
public class ConfigController {

    private final RoutineService routine;

    public ConfigController(RoutineService routine) {
        this.routine = routine;
    }

    @GetMapping("/api/config")
    public RoutineConfig config() {
        return routine.getConfig();
    }
}
