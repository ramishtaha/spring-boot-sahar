package com.ramishtaha.sahar.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ramishtaha.sahar.error.BadRequestException;
import com.ramishtaha.sahar.error.NotFoundException;
import com.ramishtaha.sahar.domain.*;
import com.ramishtaha.sahar.repo.*;
import com.ramishtaha.sahar.seed.RoutineSeed;
import com.ramishtaha.sahar.web.MealDayUpdate;
import com.ramishtaha.sahar.web.WeekUpdate;

import java.util.ArrayList;
import java.util.List;

/**
 * The service layer. It assembles the full config for reads, and is the single home for the routine's
 * business rules on writes - especially the block operations, where "4 or 5 weeks, deload last" must
 * hold no matter which operation (replace, roll-forward, add, drop, edit) got us there.
 *
 * <p>On errors it throws plain DOMAIN exceptions - {@code NotFoundException} / {@code BadRequestException} -
 * which know nothing about HTTP. The web layer's {@code ApiExceptionHandler} is the one place that maps them
 * to HTTP statuses and a {@code ProblemDetail} body (see step 19). That keeps this service HTTP-agnostic and
 * reusable, and keeps the whole error contract in a single file.
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
    private final MealPlanRepository mealPlan;

    public RoutineService(MetaRepository meta, PrayerTimesRepository prayerTimes, BlockRepository blocks,
                          ScheduleRepository schedule, GridRepository grid,
                          SupplementRepository supplements, DietRepository diet,
                          MealPlanRepository mealPlan) {
        this.meta = meta;
        this.prayerTimes = prayerTimes;
        this.blocks = blocks;
        this.schedule = schedule;
        this.grid = grid;
        this.supplements = supplements;
        this.diet = diet;
        this.mealPlan = mealPlan;
    }

    // ---- reads -------------------------------------------------------------

    public RoutineConfig getConfig() {
        MetaRepository.Meta m = meta.find().orElseThrow(() -> new IllegalStateException("app_meta not seeded"));
        PrayerTimes pt = prayerTimes.find().orElseThrow(() -> new IllegalStateException("prayer_times not seeded"));
        BlockPlan block = blocks.find();

        RoutineConfig reference = RoutineSeed.defaultConfig(); // reference content is not in the DB

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
                reference.notes(),
                mealPlan.findAll(),
                meta.findLocation().orElseGet(reference::location));
    }

    public Location getLocation() {
        return meta.findLocation().orElseThrow();
    }

    /** Save the user-chosen location (from geolocation, search, or the map). */
    public Location updateLocation(Location location) {
        meta.updateLocation(location);
        return meta.findLocation().orElseThrow();
    }

    public List<MealDay> listMealPlan() {
        return mealPlan.findAll();
    }

    public MealDay updateMealDay(String day, MealDayUpdate u) {
        if (mealPlan.update(day, u.lunch(), u.dinner(), u.note()) == 0) {
            throw new NotFoundException("no meal-plan day '" + day + "' (use Mon..Sun)");
        }
        return mealPlan.findByDay(day).orElseThrow();
    }

    public PrayerTimes getPrayerTimes() {
        return prayerTimes.find().orElseThrow();
    }

    public BlockPlan getBlock() {
        return blocks.find();
    }

    public List<ScheduleItem> listSchedule() {
        return schedule.findAll();
    }

    // ---- simple edits ------------------------------------------------------

    public PrayerTimes updatePrayerTimes(PrayerTimes prayerTimes) {
        this.prayerTimes.update(prayerTimes);
        return this.prayerTimes.find().orElseThrow();
    }

    public String updateMonth(String month) {
        meta.updateMonth(month);
        return meta.find().orElseThrow().month();
    }

    // ---- block: replace + operations --------------------------------------

    @Transactional
    public BlockPlan replaceBlock(BlockPlan block) {
        requireValidBlock(block);
        blocks.replace(block);
        return blocks.find();
    }

    /** Start next month's block from the canonical template of the same length (dates/foci to fill in). */
    @Transactional
    public BlockPlan rollForward() {
        int length = blocks.find().length();
        BlockPlan next = freshTemplate(length);
        blocks.replace(next);
        return blocks.find();
    }

    /** Add an accumulation (Build) week immediately before the deload, keeping the deload last. */
    @Transactional
    public BlockPlan addWeek() {
        BlockPlan current = blocks.find();
        List<Week> weeks = new ArrayList<>(current.weeks());
        if (weeks.size() >= 5) {
            throw new BadRequestException("a block is at most 5 weeks");
        }
        int beforeDeload = weeks.size() - 1; // insert just before the last (deload) week
        weeks.add(beforeDeload, new Week(0, "Build", "TBD", "TBD",
                "(added accumulation week - set the training focus)",
                "(set the backend focus)", false));
        BlockPlan updated = renumber(current.label(), weeks);
        requireValidBlock(updated);
        blocks.replace(updated);
        return blocks.find();
    }

    /** Drop a week by its ordinal. The deload cannot be dropped, and a block stays at least 4 weeks. */
    @Transactional
    public BlockPlan dropWeek(int ordinal) {
        BlockPlan current = blocks.find();
        List<Week> weeks = new ArrayList<>(current.weeks());
        if (weeks.size() <= 4) {
            throw new BadRequestException("a block is at least 4 weeks");
        }
        Week target = weeks.stream().filter(w -> w.ordinal() == ordinal).findFirst()
                .orElseThrow(() -> new NotFoundException("no week " + ordinal));
        if (target.deload()) {
            throw new BadRequestException("cannot drop the deload week");
        }
        weeks.removeIf(w -> w.ordinal() == ordinal);
        BlockPlan updated = renumber(current.label(), weeks);
        requireValidBlock(updated);
        blocks.replace(updated);
        return blocks.find();
    }

    /** Edit one week's name, dates, and foci. Its deload flag stays as its position dictates. */
    @Transactional
    public BlockPlan updateWeek(int ordinal, WeekUpdate u) {
        List<Week> current = blocks.find().weeks();
        List<Week> weeks = new ArrayList<>();
        boolean found = false;
        for (Week w : current) {
            if (w.ordinal() == ordinal) {
                found = true;
                weeks.add(new Week(w.ordinal(), u.name(), u.startDate(), u.endDate(),
                        u.trainingFocus(), u.backendFocus(), w.deload()));
            } else {
                weeks.add(w);
            }
        }
        if (!found) {
            throw new NotFoundException("no week " + ordinal);
        }
        BlockPlan updated = new BlockPlan(blocks.find().label(), weeks);
        requireValidBlock(updated);
        blocks.replace(updated);
        return blocks.find();
    }

    // ---- schedule CRUD -----------------------------------------------------

    public ScheduleItem createScheduleItem(ScheduleItem item) {
        return schedule.create(item);
    }

    public ScheduleItem updateScheduleItem(Long id, ScheduleItem item) {
        if (schedule.update(id, item) == 0) {
            throw new NotFoundException("no schedule item " + id);
        }
        return schedule.findById(id).orElseThrow();
    }

    public void deleteScheduleItem(Long id) {
        if (schedule.delete(id) == 0) {
            throw new NotFoundException("no schedule item " + id);
        }
    }

    // ---- helpers -----------------------------------------------------------

    /** The same rules as the @DeloadLast / @Size constraints, applied to a server-built block. */
    private void requireValidBlock(BlockPlan block) {
        int n = block.length();
        if (n < 4 || n > 5) {
            throw new BadRequestException("a block is 4 or 5 weeks long");
        }
        List<Week> weeks = block.weeks();
        for (int i = 0; i < n; i++) {
            boolean shouldBeDeload = (i == n - 1);
            if (weeks.get(i).deload() != shouldBeDeload) {
                throw new BadRequestException(
                        "the deload must be the last week (and only the last week)");
            }
        }
    }

    private List<Week> phaseNames(int length) {
        return length == 5
                ? List.of("Foundation", "Build", "Build/Peak", "Peak", "Deload").stream()
                        .map(n -> new Week(0, n, "TBD", "TBD", "(set focus)", "(set focus)", n.equals("Deload"))).toList()
                : List.of("Foundation", "Build", "Peak", "Deload").stream()
                        .map(n -> new Week(0, n, "TBD", "TBD", "(set focus)", "(set focus)", n.equals("Deload"))).toList();
    }

    private BlockPlan freshTemplate(int length) {
        return renumber(length + "-week block (rolled forward - fill in dates)",
                new ArrayList<>(phaseNames(length)));
    }

    /** Renumber ordinals 1..n in list order, keeping the given label. */
    private BlockPlan renumber(String label, List<Week> weeks) {
        List<Week> out = new ArrayList<>(weeks.size());
        for (int i = 0; i < weeks.size(); i++) {
            Week w = weeks.get(i);
            out.add(new Week(i + 1, w.name(), w.startDate(), w.endDate(),
                    w.trainingFocus(), w.backendFocus(), w.deload()));
        }
        return new BlockPlan(label, out);
    }
}
