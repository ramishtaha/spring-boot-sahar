package com.ramishtaha.sahar.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.ramishtaha.sahar.domain.BlockPlan;
import com.ramishtaha.sahar.service.RoutineService;

/**
 * Read and replace the monthly training block.
 *
 * <ul>
 *   <li>{@code GET /api/block} - return the current block (label + weeks).</li>
 *   <li>{@code PUT /api/block} - replace the whole block with the JSON body.</li>
 * </ul>
 *
 * <p>For now any block is accepted. Step 05 adds the rules (4 or 5 weeks, deload last) so a malformed
 * block is rejected with a 400 instead of silently corrupting the routine. Step 07 adds finer-grained
 * operations (roll forward, add/drop a week) on top of this.
 */
@RestController
public class BlockController {

    private final RoutineService routine;

    public BlockController(RoutineService routine) {
        this.routine = routine;
    }

    @GetMapping("/api/block")
    public BlockPlan get() {
        return routine.getConfig().block();
    }

    @PutMapping("/api/block")
    public BlockPlan update(@Valid @RequestBody BlockPlan block) {
        return routine.updateBlock(block).block();
    }
}
