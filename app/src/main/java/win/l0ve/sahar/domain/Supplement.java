package win.l0ve.sahar.domain;

/**
 * A supplement and when/why it is taken.
 *
 * @param timeLabel when to take it ("05:00", "~09:00 with breakfast", "Sunday")
 * @param name      the product/compound
 * @param dose      dose string ("3-5 g", "1500 mcg")
 * @param purpose   the short "why"
 * @param note      optional caveat or protocol note (may be null)
 */
public record Supplement(
        String timeLabel,
        String name,
        String dose,
        String purpose,
        String note
) {
}
