package win.l0ve.sahar.domain;

import java.util.List;

/**
 * The training block for the month: a label and an ordered list of weeks.
 *
 * <p>The rule (enforced by validation in step 05): a block is 4 or 5 weeks long, and the
 * <em>last</em> week is always the deload. A 4-week block is Foundation/Build/Peak/Deload;
 * a 5-week block inserts an extra accumulation week before the deload.
 *
 * <p>{@code length()} is a derived/computed accessor - records can have extra methods, they
 * just cannot add mutable state. It is handy in the UI and in the rules.
 */
public record BlockPlan(
        String label,
        List<Week> weeks
) {
    public int length() {
        return weeks == null ? 0 : weeks.size();
    }
}
