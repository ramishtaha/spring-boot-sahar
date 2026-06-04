package com.ramishtaha.sahar.repo;

import com.ramishtaha.sahar.domain.Location;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * The single app_meta row: the title, tagline, the editable month label, and (from V4) the location.
 */
@Repository
public class MetaRepository {

    /** A small carrier for the three meta fields. Lives here because only this layer needs it. */
    public record Meta(String title, String tagline, String month) {
    }

    private static final RowMapper<Meta> MAPPER = (rs, rowNum) ->
            new Meta(rs.getString("title"), rs.getString("tagline"), rs.getString("month_label"));

    private static final RowMapper<Location> LOCATION_MAPPER = (rs, rowNum) ->
            new Location(rs.getString("place_name"), rs.getDouble("lat"), rs.getDouble("lng"), rs.getInt("tz_offset"));

    private final JdbcTemplate jdbc;

    public MetaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Meta> find() {
        return jdbc.query("SELECT * FROM app_meta WHERE id = 1", MAPPER).stream().findFirst();
    }

    public Optional<Location> findLocation() {
        return jdbc.query("SELECT * FROM app_meta WHERE id = 1", LOCATION_MAPPER).stream().findFirst();
    }

    public void updateLocation(Location loc) {
        jdbc.update("UPDATE app_meta SET place_name = ?, lat = ?, lng = ?, tz_offset = ? WHERE id = 1",
                loc.placeName(), loc.lat(), loc.lng(), loc.tzOffset());
    }

    public void updateMonth(String month) {
        jdbc.update("UPDATE app_meta SET month_label = ? WHERE id = 1", month);
    }

    public void insert(String title, String tagline, String month) {
        jdbc.update("INSERT INTO app_meta (id, title, tagline, month_label) VALUES (1, ?, ?, ?)",
                title, tagline, month);
    }
}
