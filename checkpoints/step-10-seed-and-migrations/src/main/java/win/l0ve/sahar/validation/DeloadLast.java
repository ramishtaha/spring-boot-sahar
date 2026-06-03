package win.l0ve.sahar.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom Bean Validation constraint: in a {@code BlockPlan}, the deload week must be the LAST week,
 * and no earlier week may be a deload.
 *
 * <p>An annotation only becomes a validation rule via two pieces:
 * <ol>
 *   <li>this {@code @Constraint}-meta-annotated annotation (the marker the developer writes), and</li>
 *   <li>the {@link DeloadLastValidator} class named in {@code validatedBy}, which holds the logic.</li>
 * </ol>
 *
 * <p>The three members {@code message}, {@code groups}, {@code payload} are required by the Bean
 * Validation spec - every constraint annotation must declare them.
 */
@Documented
@Constraint(validatedBy = DeloadLastValidator.class)
@Target(TYPE)
@Retention(RUNTIME)
public @interface DeloadLast {

    String message() default "the deload must be the last week (and only the last week)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
