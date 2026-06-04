package com.ramishtaha.sahar.web;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for editing one day of the meal plan: {@code PUT /api/diet-plan/{day}}.
 * The day comes from the URL; only the editable fields are accepted here.
 */
public record MealDayUpdate(
        @NotBlank(message = "lunch is required") String lunch,
        @NotBlank(message = "dinner is required") String dinner,
        String note
) {
}
