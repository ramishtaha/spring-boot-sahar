package win.l0ve.sahar.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import win.l0ve.sahar.domain.RoutineConfig;
import win.l0ve.sahar.repo.*;

/**
 * Fills a fresh database with the real routine the first time the app starts.
 *
 * <p>{@code ApplicationRunner} is a Spring Boot hook: its {@code run} method executes once, right after
 * the context has fully started (and after {@code schema.sql} has created the tables). It is the
 * idiomatic place for "do this on boot" jobs like seeding.
 *
 * <p>The guard is the whole trick behind "edit a value, restart, it persists": we only seed when the
 * database is empty. On the first run the tables are blank, so we copy {@link RoutineSeed} into them.
 * On every later run the data is already there (H2 file mode kept it), so we skip - and your edits,
 * which are now rows in the database, are still there.
 *
 * <p>Step 10 retires this class: Flyway will seed the data as a versioned migration instead.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private final MetaRepository meta;
    private final PrayerTimesRepository prayerTimes;
    private final BlockRepository block;
    private final ScheduleRepository schedule;
    private final GridRepository grid;
    private final SupplementRepository supplements;
    private final DietRepository diet;

    public DataSeeder(MetaRepository meta, PrayerTimesRepository prayerTimes, BlockRepository block,
                      ScheduleRepository schedule, GridRepository grid,
                      SupplementRepository supplements, DietRepository diet) {
        this.meta = meta;
        this.prayerTimes = prayerTimes;
        this.block = block;
        this.schedule = schedule;
        this.grid = grid;
        this.supplements = supplements;
        this.diet = diet;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (meta.find().isPresent()) {
            return; // already seeded - leave existing data (and any edits) untouched
        }

        RoutineConfig c = RoutineSeed.defaultConfig();
        meta.insert(c.title(), c.tagline(), c.month());
        prayerTimes.insert(c.prayerTimes());
        block.insert(c.block());
        grid.insertAll(c.weeklyGrid());
        schedule.insertAll(c.schedule());
        supplements.insertAll(c.supplements());
        diet.insertAll(c.diet());
    }
}
