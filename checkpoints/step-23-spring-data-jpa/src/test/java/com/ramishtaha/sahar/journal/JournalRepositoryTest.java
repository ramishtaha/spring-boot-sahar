package com.ramishtaha.sahar.journal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the Spring Data JPA module works end to end against the real (in-memory) schema: Hibernate
 * VALIDATES the {@link JournalEntry} entity against the Flyway-built {@code journal_entry} table at startup,
 * then a {@code save} assigns an id and stamps {@code createdAt}, and the derived query returns rows
 * newest-day-first.
 *
 * <p>{@code @Transactional} on the test rolls everything back afterwards, so these inserts never leak into the
 * other tests sharing the in-memory database.
 */
@SpringBootTest
@Transactional
class JournalRepositoryTest {

    @Autowired
    JournalRepository journal;

    @Test
    void savesAndAssignsIdAndTimestamp() {
        JournalEntry saved = journal.save(new JournalEntry(LocalDate.of(2026, 6, 1), 4, "good session"));
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(journal.findById(saved.getId())).isPresent();
    }

    @Test
    void derivedQueryReturnsNewestDayFirst() {
        journal.save(new JournalEntry(LocalDate.of(2026, 6, 1), 3, "older"));
        journal.save(new JournalEntry(LocalDate.of(2026, 6, 3), 5, "newer"));

        var entries = journal.findAllByOrderByEntryDateDesc();
        assertThat(entries).hasSizeGreaterThanOrEqualTo(2);
        assertThat(entries.get(0).getEntryDate()).isAfterOrEqualTo(entries.get(1).getEntryDate());
    }
}
