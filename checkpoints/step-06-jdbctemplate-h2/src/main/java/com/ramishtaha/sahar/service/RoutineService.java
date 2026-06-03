package com.ramishtaha.sahar.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ramishtaha.sahar.domain.BlockPlan;
import com.ramishtaha.sahar.domain.PrayerTimes;
import com.ramishtaha.sahar.domain.RoutineConfig;
import com.ramishtaha.sahar.repo.*;
import com.ramishtaha.sahar.seed.RoutineSeed;

/**
 * The service layer - unchanged in PURPOSE from step 04, but its guts now read and write a database
 * instead of holding a field in memory.
 *
 * <p>This is the payoff of layering: the controllers did not change at all. They still call
 * {@code getConfig()}, {@code updatePrayerTimes(...)}, and so on. We swapped the implementation behind
 * those methods from "a mutable field" to "rows in H2", and the web layer never noticed. That is the
 * whole reason we separated web / service / data.
 *
 * <p>{@code getConfig()} assembles a {@link RoutineConfig} by reading each table through its repository,
 * then bolting on the reference content (journal prompts, three rules, weekend, notes) that is NOT stored
 * in the database - it is static text, so it stays in {@link RoutineSeed}. Not everything has to live in
 * a table.
 */
@Service
public class RoutineService {

    private final MetaRepository meta;
    private final PrayerTimesRepository prayerTimes;
    private final BlockRepository blocks;
    private final ScheduleRepository schedule;
    private final GridRepository grid;
    private final SupplementRepository supplements;
    private final DietRepository diet;

    public RoutineService(MetaRepository meta, PrayerTimesRepository prayerTimes, BlockRepository blocks,
                          ScheduleRepository schedule, GridRepository grid,
                          SupplementRepository supplements, DietRepository diet) {
        this.meta = meta;
        this.prayerTimes = prayerTimes;
        this.blocks = blocks;
        this.schedule = schedule;
        this.grid = grid;
        this.supplements = supplements;
        this.diet = diet;
    }

    public RoutineConfig getConfig() {
        MetaRepository.Meta m = meta.find().orElseThrow(() -> new IllegalStateException("app_meta not seeded"));
        PrayerTimes pt = prayerTimes.find().orElseThrow(() -> new IllegalStateException("prayer_times not seeded"));
        BlockPlan block = blocks.find();

        // Reference content is not in the database - read it straight from the seed.
        RoutineConfig reference = RoutineSeed.defaultConfig();

        return new RoutineConfig(
                m.title(), m.tagline(), m.month(),
                pt,
                block,
                grid.findAll(),
                schedule.findAll(),
                supplements.findAll(),
                diet.findAll(),
                reference.journal(),
                reference.threeRules(),
                reference.weekend(),
                reference.notes());
    }

    public RoutineConfig updatePrayerTimes(PrayerTimes prayerTimes) {
        this.prayerTimes.update(prayerTimes);
        return getConfig();
    }

    public RoutineConfig updateMonth(String month) {
        meta.updateMonth(month);
        return getConfig();
    }

    /**
     * Replacing the block touches two tables (blocks + weeks). {@code @Transactional} makes the whole
     * operation atomic: if anything throws halfway, the delete-and-reinsert is rolled back, so a reader
     * can never catch the block with half its weeks missing.
     */
    @Transactional
    public RoutineConfig updateBlock(BlockPlan block) {
        blocks.replace(block);
        return getConfig();
    }
}
