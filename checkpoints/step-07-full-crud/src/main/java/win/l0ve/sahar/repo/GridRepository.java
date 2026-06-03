package win.l0ve.sahar.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import win.l0ve.sahar.domain.GridDay;

import java.util.List;

/** The weekly training grid (Mon..Sun), in display order. Reference content: read and seeded only. */
@Repository
public class GridRepository {

    private static final RowMapper<GridDay> MAPPER = (rs, rowNum) -> new GridDay(
            rs.getString("day_of_week"),
            rs.getString("discipline"),
            rs.getString("note"));

    private final JdbcTemplate jdbc;

    public GridRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<GridDay> findAll() {
        return jdbc.query("SELECT * FROM weekly_grid ORDER BY sort_order", MAPPER);
    }

    public void insertAll(List<GridDay> days) {
        int order = 0;
        for (GridDay d : days) {
            jdbc.update(
                    "INSERT INTO weekly_grid (day_of_week, discipline, note, sort_order) VALUES (?, ?, ?, ?)",
                    d.day(), d.discipline(), d.note(), order++);
        }
    }
}
