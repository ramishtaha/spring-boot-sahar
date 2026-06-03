package com.ramishtaha.sahar.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.ramishtaha.sahar.domain.BlockPlan;
import com.ramishtaha.sahar.domain.Week;

import java.util.List;

/**
 * The block (id = 1) and its ordered weeks.
 *
 * <p>This is the first repository that spans TWO tables (blocks + weeks). It shows a common pattern:
 * an aggregate ({@link BlockPlan}) is stored across a parent row and child rows, so reading it is two
 * queries and replacing it is "update the parent, delete the old children, insert the new ones".
 */
@Repository
public class BlockRepository {

    private static final RowMapper<Week> WEEK_MAPPER = (rs, rowNum) -> new Week(
            rs.getInt("ordinal"),
            rs.getString("name"),
            rs.getString("start_date"),
            rs.getString("end_date"),
            rs.getString("training_focus"),
            rs.getString("backend_focus"),
            rs.getBoolean("deload"));

    private final JdbcTemplate jdbc;

    public BlockRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public BlockPlan find() {
        String label = jdbc.queryForObject("SELECT label FROM blocks WHERE id = 1", String.class);
        List<Week> weeks = jdbc.query(
                "SELECT * FROM weeks WHERE block_id = 1 ORDER BY ordinal", WEEK_MAPPER);
        return new BlockPlan(label, weeks);
    }

    public boolean exists() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM blocks", Integer.class);
        return count != null && count > 0;
    }

    /**
     * Replace the whole block: set the label, wipe the weeks, re-insert them in order.
     * The caller (the service) wraps this in a transaction so a half-written block can never be seen.
     */
    public void replace(BlockPlan block) {
        jdbc.update("UPDATE blocks SET label = ? WHERE id = 1", block.label());
        jdbc.update("DELETE FROM weeks WHERE block_id = 1");
        insertWeeks(block);
    }

    /** Used by the seeder on a fresh database. */
    public void insert(BlockPlan block) {
        jdbc.update("INSERT INTO blocks (id, label) VALUES (1, ?)", block.label());
        insertWeeks(block);
    }

    private void insertWeeks(BlockPlan block) {
        for (Week w : block.weeks()) {
            jdbc.update("""
                    INSERT INTO weeks
                        (block_id, ordinal, name, start_date, end_date, training_focus, backend_focus, deload)
                    VALUES (1, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    w.ordinal(), w.name(), w.startDate(), w.endDate(),
                    w.trainingFocus(), w.backendFocus(), w.deload());
        }
    }
}
