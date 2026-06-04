package com.ramishtaha.sahar.repo;

import com.ramishtaha.sahar.domain.MealDay;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** The editable weekly meal plan (Mon..Sun), kept in display order. */
@Repository
public class MealPlanRepository {

    private static final RowMapper<MealDay> MAPPER = (rs, rowNum) -> new MealDay(
            rs.getString("day_of_week"),
            rs.getString("lunch"),
            rs.getString("dinner"),
            rs.getString("note"));

    private final JdbcTemplate jdbc;

    public MealPlanRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<MealDay> findAll() {
        return jdbc.query("SELECT * FROM meal_plan ORDER BY sort_order", MAPPER);
    }

    public Optional<MealDay> findByDay(String day) {
        return jdbc.query("SELECT * FROM meal_plan WHERE LOWER(day_of_week) = LOWER(?)", MAPPER, day)
                .stream().findFirst();
    }

    /** Update one day's meals. Returns the number of rows changed (0 = no such day). */
    public int update(String day, String lunch, String dinner, String note) {
        return jdbc.update("""
                UPDATE meal_plan SET lunch = ?, dinner = ?, note = ?
                 WHERE LOWER(day_of_week) = LOWER(?)
                """, lunch, dinner, note, day);
    }
}
