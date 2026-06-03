package win.l0ve.sahar.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * One week of the monthly training block.
 *
 * <p>Each week carries its own field-level constraints. When a {@code BlockPlan} is validated, the
 * {@code @Valid} on its {@code weeks} list cascades down so every {@code Week} here is checked too.
 *
 * @param ordinal       1-based position in the block (must be >= 1)
 * @param name          phase name (Foundation, Build, Peak, Deload, ...) - required
 * @param startDate     human date, e.g. "8 Jun" - required
 * @param endDate       human date, e.g. "14 Jun" - required
 * @param trainingFocus what the body does this week - required
 * @param backendFocus  what the brain does this week - required
 * @param deload        true only for the final, recovery week
 */
public record Week(
        @Min(value = 1, message = "week ordinal starts at 1") int ordinal,
        @NotBlank(message = "week name is required") String name,
        @NotBlank(message = "start date is required") String startDate,
        @NotBlank(message = "end date is required") String endDate,
        @NotBlank(message = "training focus is required") String trainingFocus,
        @NotBlank(message = "backend focus is required") String backendFocus,
        boolean deload
) {
}
