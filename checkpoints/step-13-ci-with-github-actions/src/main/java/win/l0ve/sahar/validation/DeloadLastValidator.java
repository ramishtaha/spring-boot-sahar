package win.l0ve.sahar.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import win.l0ve.sahar.domain.BlockPlan;
import win.l0ve.sahar.domain.Week;

import java.util.List;

/**
 * The logic behind {@link DeloadLast}. {@code ConstraintValidator<A, T>} ties the annotation {@code A}
 * to the type {@code T} it validates - here {@code BlockPlan}.
 *
 * <p>{@code isValid} returns true when the block is valid. We deliberately return {@code true} for a
 * null or empty list: that is the job of {@code @NotEmpty}/{@code @Size}, and a good validator does ONE
 * thing so error messages stay precise (you do not want "deload" errors piling on top of "too short").
 */
public class DeloadLastValidator implements ConstraintValidator<DeloadLast, BlockPlan> {

    @Override
    public boolean isValid(BlockPlan block, ConstraintValidatorContext context) {
        if (block == null) {
            return true;
        }
        List<Week> weeks = block.weeks();
        if (weeks == null || weeks.isEmpty()) {
            return true; // not our concern - @NotEmpty/@Size handle the size
        }

        int lastIndex = weeks.size() - 1;
        for (int i = 0; i < weeks.size(); i++) {
            boolean isDeload = weeks.get(i).deload();
            boolean isLast = (i == lastIndex);
            if (isDeload != isLast) {
                // Either an earlier week is a deload (isDeload && !isLast),
                // or the last week is NOT a deload (!isDeload && isLast). Both are invalid.
                return false;
            }
        }
        return true;
    }
}
