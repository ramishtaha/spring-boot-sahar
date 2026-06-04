package com.ramishtaha.sahar.error;

/**
 * Thrown by the service layer when something the caller asked for does not exist (a missing schedule id,
 * an unknown week, a bad meal-plan day, ...).
 *
 * <p>It is a plain DOMAIN exception - it knows nothing about HTTP. The web layer's {@code ApiExceptionHandler}
 * is the single place that decides "a NotFoundException becomes an HTTP 404". That separation keeps the
 * service reusable and HTTP-agnostic, and keeps the error contract in one place.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
