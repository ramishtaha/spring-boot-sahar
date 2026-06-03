package com.ramishtaha.sahar.domain;

import java.util.List;

/**
 * The bullet-journal prompts the app displays, split into the morning (04:50) and
 * night (21:00) logs. These are reference content - shown, not edited through the app.
 */
public record JournalPrompts(
        List<String> morning,
        List<String> night
) {
}
