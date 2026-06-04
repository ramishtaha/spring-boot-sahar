package com.ramishtaha.sahar.error;

/**
 * Thrown when a request is well-formed JSON but breaks a business rule (a 6-week block, dropping the
 * deload, lat/lng out of range, ...). Maps to HTTP 400 in {@code ApiExceptionHandler}.
 *
 * <p>Like {@link NotFoundException}, it is HTTP-agnostic on purpose: the service expresses *what is wrong*
 * in domain terms; the web layer translates it to a status code and a {@code ProblemDetail} body.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
