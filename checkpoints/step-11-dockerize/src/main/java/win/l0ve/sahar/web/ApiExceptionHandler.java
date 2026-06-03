package win.l0ve.sahar.web;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Turns validation failures into one clean, predictable 400 response for the whole API.
 *
 * <p>{@code @RestControllerAdvice} is a global interceptor for every {@code @RestController}. When a
 * {@code @Valid @RequestBody} fails, Spring throws {@link MethodArgumentNotValidException}; this handler
 * catches it, flattens every field error and class-level (global) error into a list of plain messages,
 * and returns HTTP 400 with that list. Without it you would still get a 400, but with Spring's default
 * verbose body - this gives the frontend something tidy to show the user.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** The body shape of a validation failure. */
    public record ApiError(int status, String error, List<String> messages) {
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex) {
        List<String> messages = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            messages.add(fe.getField() + ": " + fe.getDefaultMessage());
        }
        for (ObjectError oe : ex.getBindingResult().getGlobalErrors()) {
            messages.add(oe.getDefaultMessage());
        }
        Collections.sort(messages); // stable, readable ordering
        return new ApiError(400, "validation failed", messages);
    }
}
