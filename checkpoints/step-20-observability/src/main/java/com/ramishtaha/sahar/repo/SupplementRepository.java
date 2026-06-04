package com.ramishtaha.sahar.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.ramishtaha.sahar.domain.Supplement;

import java.util.List;

/** Supplements, in display order. Reference content: read and seeded only. */
@Repository
public class SupplementRepository {

    private static final RowMapper<Supplement> MAPPER = (rs, rowNum) -> new Supplement(
            rs.getString("time_label"),
            rs.getString("name"),
            rs.getString("dose"),
            rs.getString("purpose"),
            rs.getString("note"));

    private final JdbcTemplate jdbc;

    public SupplementRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Supplement> findAll() {
        return jdbc.query("SELECT * FROM supplements ORDER BY sort_order", MAPPER);
    }

    public void insertAll(List<Supplement> supplements) {
        int order = 0;
        for (Supplement s : supplements) {
            jdbc.update("""
                    INSERT INTO supplements (time_label, name, dose, purpose, note, sort_order)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    s.timeLabel(), s.name(), s.dose(), s.purpose(), s.note(), order++);
        }
    }
}
