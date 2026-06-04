package com.ramishtaha.sahar.repo;

import com.ramishtaha.sahar.domain.MealDay;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A repository test against the real (in-memory) database. The Flyway migrations run and seed the
 * `meal_plan` table, so we can assert on actual rows and prove a real UPDATE round-trips through
 * {@link MealPlanRepository}. This is the kind of test that protects your SQL.
 */
@SpringBootTest
class MealPlanRepositoryTest {

    @Autowired
    MealPlanRepository mealPlan;

    @Test
    void seedHasSevenDays() {
        assertThat(mealPlan.findAll()).hasSize(7);
        assertThat(mealPlan.findByDay("Mon")).isPresent();
    }

    @Test
    void updateRoundTrips() {
        int changed = mealPlan.update("Tue", "Test lunch", "Test dinner", "test note");
        assertThat(changed).isEqualTo(1);
        assertThat(mealPlan.findByDay("Tue"))
                .get()
                .extracting(MealDay::lunch)
                .isEqualTo("Test lunch");
    }
}
