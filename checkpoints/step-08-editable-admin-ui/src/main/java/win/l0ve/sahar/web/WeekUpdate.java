package win.l0ve.sahar.web;

import jakarta.validation.constraints.NotBlank;

/**
 * The request body for editing one week: {@code PUT /api/block/weeks/{ordinal}}.
 *
 * <p>It deliberately does NOT carry {@code ordinal} or {@code deload}. The ordinal comes from the URL,
 * and whether a week is the deload is decided by its POSITION in the block, not by the client - so those
 * are off-limits here. This is a small but real API-design habit: only accept the fields you actually
 * allow the caller to change.
 */
public record WeekUpdate(
        @NotBlank(message = "week name is required") String name,
        @NotBlank(message = "start date is required") String startDate,
        @NotBlank(message = "end date is required") String endDate,
        @NotBlank(message = "training focus is required") String trainingFocus,
        @NotBlank(message = "backend focus is required") String backendFocus
) {
}
