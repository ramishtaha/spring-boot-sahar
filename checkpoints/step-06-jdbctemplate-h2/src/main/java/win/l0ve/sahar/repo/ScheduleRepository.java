package win.l0ve.sahar.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import win.l0ve.sahar.domain.ScheduleItem;

import java.util.List;

/**
 * The daily timeline rows, kept in display order by {@code sort_order}.
 *
 * <p>In step 06 this is read-only (plus an insert for seeding). Step 07 adds full create/update/delete
 * so individual timeline slots can be edited.
 */
@Repository
public class ScheduleRepository {

    private static final RowMapper<ScheduleItem> MAPPER = (rs, rowNum) -> new ScheduleItem(
            rs.getString("slot_time"),
            rs.getString("title"),
            rs.getString("detail"),
            rs.getString("category"),
            rs.getString("day_type"));

    private final JdbcTemplate jdbc;

    public ScheduleRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ScheduleItem> findAll() {
        return jdbc.query("SELECT * FROM schedule_items ORDER BY sort_order, slot_time", MAPPER);
    }

    /** Seed all rows, numbering sort_order by position so the seed order is preserved. */
    public void insertAll(List<ScheduleItem> items) {
        int order = 0;
        for (ScheduleItem it : items) {
            jdbc.update("""
                    INSERT INTO schedule_items (slot_time, title, detail, category, day_type, sort_order)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    it.time(), it.title(), it.detail(), it.category(), it.dayType(), order++);
        }
    }
}
