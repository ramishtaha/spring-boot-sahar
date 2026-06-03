package win.l0ve.sahar.domain;

/**
 * A titled paragraph of nutrition guidance (Breakfast, Lunch, Organ meats, ...).
 *
 * @param ordinal display order
 * @param title   the heading
 * @param body    the guidance text
 */
public record DietSection(
        int ordinal,
        String title,
        String body
) {
}
