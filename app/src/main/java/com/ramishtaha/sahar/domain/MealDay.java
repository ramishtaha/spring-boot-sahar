package com.ramishtaha.sahar.domain;

/**
 * One day of the suggested weekly meal plan, aligned to the training grid.
 *
 * <p>Breakfast is the same every day (eggs in ghee + pumpkin seeds), so it is shown once in the UI
 * rather than repeated here. This record carries the parts that vary by day.
 *
 * @param day    "Mon" .. "Sun" (matches the weekly training grid, so the UI can show them side by side)
 * @param lunch  the heavy meal
 * @param dinner the light meal
 * @param note   a short cue (organ meat of the day, omega-3, batch-cook, etc.)
 */
public record MealDay(
        String day,
        String lunch,
        String dinner,
        String note
) {
}
