package com.ramishtaha.sahar.journal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * The request body for creating a journal entry (step 23).
 *
 * <p>A separate input record - not the {@link JournalEntry} entity - is the right call: the entity carries a
 * database id and a server-set timestamp the client must not supply, and we want Bean Validation to reject a
 * bad payload before it ever reaches Hibernate. Jackson parses {@code "2026-06-04"} straight into a
 * {@link LocalDate}.
 */
public record JournalForm(
        @NotNull LocalDate date,
        @Min(value = 1, message = "energy is 1..5") @Max(value = 5, message = "energy is 1..5") int energy,
        @Size(max = 1000, message = "note is at most 1000 characters") String note) {
}
