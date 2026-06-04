package com.ramishtaha.sahar.event;

import com.ramishtaha.sahar.domain.Location;
import com.ramishtaha.sahar.domain.PrayerTimes;
import com.ramishtaha.sahar.repo.PrayerTimesRepository;
import com.ramishtaha.sahar.service.PrayerTimeCalculator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;

/**
 * Recomputes and saves the month's prayer times whenever the location changes (step 22).
 *
 * <p>This is the "reaction" half of the event pattern. It listens for {@link LocationChangedEvent} and,
 * using the same pure {@link PrayerTimeCalculator} the API exposes, writes fresh times for the new
 * coordinates. {@code RoutineService.updateLocation} stays blissfully unaware that this happens - decoupling
 * a write from its consequences.
 *
 * <p><b>Why {@code @TransactionalEventListener(AFTER_COMMIT)} and not a plain {@code @EventListener}?</b> A
 * plain listener runs the instant the event is published - which is still <em>inside</em> the location's
 * transaction. If that transaction then rolled back, we would have recomputed prayer times for a location
 * that was never actually saved. {@code AFTER_COMMIT} runs only once the change is durably committed, so the
 * reaction can never get ahead of the fact it reacts to. (If no transaction is active, an AFTER_COMMIT
 * listener simply doesn't fire - which is why {@code updateLocation} is {@code @Transactional}.)
 */
@Component
public class PrayerTimesRecalculator {

    private final PrayerTimeCalculator calculator;
    private final PrayerTimesRepository prayerTimes;

    public PrayerTimesRecalculator(PrayerTimeCalculator calculator, PrayerTimesRepository prayerTimes) {
        this.calculator = calculator;
        this.prayerTimes = prayerTimes;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLocationChanged(LocationChangedEvent event) {
        Location loc = event.location();
        // tzOffset is minutes east of UTC; the calculator wants hours (IST 330 -> 5.5).
        PrayerTimes recomputed = calculator.calculate(loc.lat(), loc.lng(), loc.tzOffset() / 60.0, LocalDate.now());
        prayerTimes.update(recomputed);
    }
}
