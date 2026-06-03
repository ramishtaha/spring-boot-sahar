package win.l0ve.sahar.service;

import org.springframework.stereotype.Service;
import win.l0ve.sahar.domain.BlockPlan;
import win.l0ve.sahar.domain.PrayerTimes;
import win.l0ve.sahar.domain.RoutineConfig;
import win.l0ve.sahar.seed.RoutineSeed;

/**
 * Holds the current routine and applies edits. This is the "service layer" - the place where
 * business logic lives, sitting between the web layer (controllers) and, later, the data layer
 * (repositories in step 06).
 *
 * <p>{@code @Service} marks it as a Spring bean. Spring creates exactly one instance (a singleton)
 * at startup and injects it wherever it is needed. That single instance is why an edit made through
 * one HTTP request is visible to the next request - they share this object.
 *
 * <p>The state is just a field holding the current {@link RoutineConfig}. It lives in memory only,
 * so it survives requests but NOT a restart: stop the app and the next start reads a fresh copy from
 * {@link RoutineSeed} again. That is the limitation step 06 fixes by moving state into a database.
 *
 * <p>The methods are {@code synchronized} so two concurrent requests cannot interleave a read and a
 * write of {@code config} and lose an update. (One user editing their own routine will never hit
 * this, but it costs nothing and is the correct habit.)
 */
@Service
public class RoutineService {

    private RoutineConfig config = RoutineSeed.defaultConfig();

    public synchronized RoutineConfig getConfig() {
        return config;
    }

    /** Replace the whole prayer-times block. Returns the updated config. */
    public synchronized RoutineConfig updatePrayerTimes(PrayerTimes prayerTimes) {
        config = config.withPrayerTimes(prayerTimes);
        return config;
    }

    /** Replace just the month label. Returns the updated config. */
    public synchronized RoutineConfig updateMonth(String month) {
        config = config.withMonth(month);
        return config;
    }

    /** Replace the whole training block. Returns the updated config. */
    public synchronized RoutineConfig updateBlock(BlockPlan block) {
        config = config.withBlock(block);
        return config;
    }
}
