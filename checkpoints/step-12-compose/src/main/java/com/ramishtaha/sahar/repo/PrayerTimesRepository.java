package com.ramishtaha.sahar.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.ramishtaha.sahar.domain.PrayerTimes;

import java.util.Optional;

/**
 * Reads and writes the single prayer-times row (id = 1).
 *
 * <p>This is the Repository layer - the only place that knows SQL. The service talks to it in terms
 * of {@link PrayerTimes} objects and never sees a {@code ResultSet}. That separation is the point:
 * swap H2 for Postgres (step 09) and only this layer's wiring changes, not the service or controllers.
 *
 * <p>{@link JdbcTemplate} is auto-configured by {@code spring-boot-starter-jdbc} from the
 * {@code spring.datasource.*} properties. It handles the tedious, error-prone JDBC ceremony
 * (open connection, create statement, set parameters, iterate results, close everything) and lets us
 * write just the SQL and a {@link RowMapper}.
 *
 * <ul>
 *   <li>A {@code RowMapper<T>} turns ONE result row into ONE object.</li>
 *   <li>{@code query(sql, mapper, args...)} runs a SELECT and maps every row.</li>
 *   <li>{@code update(sql, args...)} runs INSERT/UPDATE/DELETE and returns the affected row count.</li>
 *   <li>The {@code ?} placeholders are <em>bind parameters</em> - never string-concatenate user input
 *       into SQL, or you invite SQL injection. JdbcTemplate binds them safely.</li>
 * </ul>
 */
@Repository
public class PrayerTimesRepository {

    private static final RowMapper<PrayerTimes> MAPPER = (rs, rowNum) -> new PrayerTimes(
            rs.getString("fajr"),
            rs.getString("sunrise"),
            rs.getString("dhuhr"),
            rs.getString("asr"),
            rs.getString("maghrib"),
            rs.getString("isha"),
            rs.getString("method_note"));

    private final JdbcTemplate jdbc;

    public PrayerTimesRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<PrayerTimes> find() {
        return jdbc.query("SELECT * FROM prayer_times WHERE id = 1", MAPPER).stream().findFirst();
    }

    public void update(PrayerTimes p) {
        jdbc.update("""
                UPDATE prayer_times
                   SET fajr = ?, sunrise = ?, dhuhr = ?, asr = ?, maghrib = ?, isha = ?, method_note = ?
                 WHERE id = 1
                """,
                p.fajr(), p.sunrise(), p.dhuhr(), p.asr(), p.maghrib(), p.isha(), p.methodNote());
    }

    /** Used once, by the seeder, to create the single row on a fresh database. */
    public void insert(PrayerTimes p) {
        jdbc.update("""
                INSERT INTO prayer_times (id, fajr, sunrise, dhuhr, asr, maghrib, isha, method_note)
                VALUES (1, ?, ?, ?, ?, ?, ?, ?)
                """,
                p.fajr(), p.sunrise(), p.dhuhr(), p.asr(), p.maghrib(), p.isha(), p.methodNote());
    }
}
