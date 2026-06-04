package com.ramishtaha.sahar.domain;

/**
 * One day of the weekly training grid.
 *
 * @param day        "Mon" .. "Sun"
 * @param discipline what is trained that day
 * @param note       optional extra note (may be null)
 */
public record GridDay(
        String day,
        String discipline,
        String note
) {
}
