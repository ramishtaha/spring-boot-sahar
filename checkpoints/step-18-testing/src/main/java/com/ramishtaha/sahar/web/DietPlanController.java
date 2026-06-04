package com.ramishtaha.sahar.web;

import com.ramishtaha.sahar.domain.MealDay;
import com.ramishtaha.sahar.service.RoutineService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Read and edit the weekly meal plan.
 *
 * <ul>
 *   <li>{@code GET /api/diet-plan}        - the seven days.</li>
 *   <li>{@code PUT /api/diet-plan/{day}}  - replace one day's lunch/dinner/note ({@code day} = Mon..Sun).</li>
 * </ul>
 *
 * The whole plan also rides along inside {@code GET /api/config} as {@code dietPlan}, so the home page can
 * render it next to the training grid without a second request.
 */
@RestController
@RequestMapping("/api/diet-plan")
public class DietPlanController {

    private final RoutineService routine;

    public DietPlanController(RoutineService routine) {
        this.routine = routine;
    }

    @GetMapping
    public List<MealDay> list() {
        return routine.listMealPlan();
    }

    @PutMapping("/{day}")
    public MealDay update(@PathVariable String day, @Valid @RequestBody MealDayUpdate body) {
        return routine.updateMealDay(day, body);
    }
}
