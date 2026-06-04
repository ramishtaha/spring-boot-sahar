package com.ramishtaha.sahar.web;

import com.ramishtaha.sahar.error.BadRequestException;
import com.ramishtaha.sahar.error.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One place that turns every error into a consistent, standards-based response.
 *
 * <p>It returns {@link ProblemDetail} - Spring's implementation of <b>RFC 9457</b> ("Problem Details for
 * HTTP APIs"). Every error then has the same predictable shape and the {@code application/problem+json}
 * content type:
 * <pre>{ "type": "about:blank", "title": "...", "status": 400, "detail": "...", "instance": "/api/..." }</pre>
 * Validation failures add an {@code errors} array. A uniform error contract is a quiet but real piece of
 * good system design: every client (our admin page, a future mobile app, a test) parses errors the same way.
 *
 * <p>Note the division of labour: the service layer throws plain {@link NotFoundException} /
 * {@link BadRequestException} (no HTTP), and THIS class - the only HTTP-aware error code - maps each to a
 * status and a body. Spring sets the response status from the returned {@code ProblemDetail}.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Not found");
        return pd;
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Invalid request");
        return pd;
    }

    /** The request body was missing or not valid JSON (a parse error, before validation even runs). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request body is missing or not valid JSON");
        pd.setTitle("Malformed request");
        return pd;
    }

    /** Bean Validation failures on a {@code @Valid @RequestBody}: 400 with a list of field messages. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "One or more fields are invalid");
        pd.setTitle("Validation failed");
        List<String> errors = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.add(fe.getField() + ": " + fe.getDefaultMessage());
        }
        for (ObjectError oe : ex.getBindingResult().getGlobalErrors()) {
            errors.add(oe.getDefaultMessage());
        }
        Collections.sort(errors);
        pd.setProperty("errors", errors);
        return pd;
    }
}
