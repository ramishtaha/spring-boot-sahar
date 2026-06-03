package com.ramishtaha.sahar.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.ramishtaha.sahar.domain.BlockPlan;
import com.ramishtaha.sahar.service.RoutineService;

/**
 * Read, replace, and operate on the monthly training block.
 *
 * <ul>
 *   <li>{@code GET    /api/block}                 - the current block.</li>
 *   <li>{@code PUT    /api/block}                 - replace it wholesale (validated: 4-5 weeks, deload last).</li>
 *   <li>{@code POST   /api/block/roll-forward}    - start next month's block from the template.</li>
 *   <li>{@code POST   /api/block/weeks}           - add an accumulation week before the deload.</li>
 *   <li>{@code PUT    /api/block/weeks/{ordinal}} - edit one week's name, dates, foci.</li>
 *   <li>{@code DELETE /api/block/weeks/{ordinal}} - drop a week (not the deload; min 4).</li>
 * </ul>
 *
 * <p>Note the verbs: replacing the resource is PUT (idempotent); roll-forward and add-week create/run
 * something new, so they are POST; editing one named week is PUT to that week's URL; removing one is
 * DELETE. {@code @PathVariable} pulls the {@code {ordinal}} out of the URL.
 */
@RestController
@RequestMapping("/api/block")
public class BlockController {

    private final RoutineService routine;

    public BlockController(RoutineService routine) {
        this.routine = routine;
    }

    @GetMapping
    public BlockPlan get() {
        return routine.getBlock();
    }

    @PutMapping
    public BlockPlan replace(@Valid @RequestBody BlockPlan block) {
        return routine.replaceBlock(block);
    }

    @PostMapping("/roll-forward")
    public BlockPlan rollForward() {
        return routine.rollForward();
    }

    @PostMapping("/weeks")
    public BlockPlan addWeek() {
        return routine.addWeek();
    }

    @PutMapping("/weeks/{ordinal}")
    public BlockPlan updateWeek(@PathVariable int ordinal, @Valid @RequestBody WeekUpdate update) {
        return routine.updateWeek(ordinal, update);
    }

    @DeleteMapping("/weeks/{ordinal}")
    public BlockPlan dropWeek(@PathVariable int ordinal) {
        return routine.dropWeek(ordinal);
    }
}
