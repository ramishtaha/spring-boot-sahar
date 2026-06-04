package com.ramishtaha.sahar.journal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * The journal repository - and notice there is no implementation to write (step 23).
 *
 * <p>This is the Spring Data JPA headline: you declare an INTERFACE extending {@link JpaRepository}, and at
 * startup Spring generates a proxy that implements it. {@code JpaRepository<JournalEntry, Long>} already gives
 * you {@code save}, {@code findById}, {@code findAll}, {@code deleteById}, {@code count}, and more - no SQL,
 * no boilerplate.
 *
 * <p>{@code findAllByOrderByEntryDateDesc} is a <b>derived query</b>: Spring Data parses the METHOD NAME and
 * writes the query for you (here: "select all, order by entryDate descending"). Contrast that with
 * {@code PrayerTimesRepository}, where we hand-write every SQL string against a {@link org.springframework.jdbc.core.JdbcTemplate}.
 *
 * <p><b>When does each win?</b> JdbcTemplate keeps you close to the SQL - ideal for the bespoke reads and the
 * exact control the rest of Sahar wants. Spring Data JPA removes boilerplate for ordinary CRUD-shaped tables
 * like this journal, at the cost of a heavier abstraction (Hibernate, the persistence context, lazy loading).
 * A real codebase often uses both, deliberately - which is exactly what this project now demonstrates.
 */
public interface JournalRepository extends JpaRepository<JournalEntry, Long> {

    /** Newest first, by the day the entry is for. The query is derived entirely from this method's name. */
    List<JournalEntry> findAllByOrderByEntryDateDesc();
}
