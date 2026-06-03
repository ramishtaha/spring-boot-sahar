package win.l0ve.sahar.web;

/**
 * The request body for changing the month label: {@code { "month": "July 2026" }}.
 *
 * <p>This is a small "DTO" (data transfer object) that exists only to give the PUT a typed shape.
 * We could reuse a domain type, but the month label is a lone string and a tiny request record keeps
 * the API self-documenting without dragging the whole config into the request.
 */
public record MonthUpdate(String month) {
}
