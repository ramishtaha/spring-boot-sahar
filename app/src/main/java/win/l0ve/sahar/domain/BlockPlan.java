package win.l0ve.sahar.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import win.l0ve.sahar.validation.DeloadLast;

import java.util.List;

/**
 * The training block for the month: a label and an ordered list of weeks.
 *
 * <p>Three rules are enforced:
 * <ul>
 *   <li>{@code @Size(min = 4, max = 5)} - a block is 4 or 5 weeks (a built-in constraint).</li>
 *   <li>{@code @Valid} on the list - cascade validation into each {@link Week}.</li>
 *   <li>{@code @DeloadLast} - our own, custom, cross-field rule: the deload is the last week and only
 *       the last week. A single field constraint cannot express that, so step 05 writes a small
 *       {@code ConstraintValidator} for it. The annotation sits on the type because the rule looks at
 *       the whole record, not one component.</li>
 * </ul>
 */
@DeloadLast
public record BlockPlan(
        String label,

        @NotEmpty(message = "a block needs weeks")
        @Size(min = 4, max = 5, message = "a block is 4 or 5 weeks long")
        @Valid
        List<Week> weeks
) {
    public int length() {
        return weeks == null ? 0 : weeks.size();
    }
}
