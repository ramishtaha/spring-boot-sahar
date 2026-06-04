package com.ramishtaha.sahar.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.ramishtaha.sahar.domain.DietSection;

import java.util.List;

/** Nutrition sections, ordered by their ordinal. Reference content: read and seeded only. */
@Repository
public class DietRepository {

    private static final RowMapper<DietSection> MAPPER = (rs, rowNum) -> new DietSection(
            rs.getInt("ordinal"),
            rs.getString("title"),
            rs.getString("body"));

    private final JdbcTemplate jdbc;

    public DietRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<DietSection> findAll() {
        return jdbc.query("SELECT * FROM diet_sections ORDER BY ordinal", MAPPER);
    }

    public void insertAll(List<DietSection> sections) {
        for (DietSection d : sections) {
            jdbc.update("INSERT INTO diet_sections (ordinal, title, body) VALUES (?, ?, ?)",
                    d.ordinal(), d.title(), d.body());
        }
    }
}
