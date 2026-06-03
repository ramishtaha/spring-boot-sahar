package com.ramishtaha.sahar.domain;

/**
 * One week of the monthly training block.
 *
 * @param ordinal       1-based position in the block (1, 2, 3, ...)
 * @param name          the phase name: Foundation, Build, Build/Peak, Peak, or Deload
 * @param startDate     human date, e.g. "8 Jun"
 * @param endDate       human date, e.g. "14 Jun"
 * @param trainingFocus what the body does this week
 * @param backendFocus  what the brain (backend learning) does this week
 * @param deload        true only for the final, recovery week of the block
 */
public record Week(
        int ordinal,
        String name,
        String startDate,
        String endDate,
        String trainingFocus,
        String backendFocus,
        boolean deload
) {
}
