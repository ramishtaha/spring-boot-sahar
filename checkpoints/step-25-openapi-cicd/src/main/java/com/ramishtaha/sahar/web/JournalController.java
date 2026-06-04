package com.ramishtaha.sahar.web;

import com.ramishtaha.sahar.journal.JournalEntry;
import com.ramishtaha.sahar.journal.JournalForm;
import com.ramishtaha.sahar.journal.JournalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The journal's REST endpoints (step 23):
 * <ul>
 *   <li>{@code GET    /api/journal}        - list entries, newest day first.</li>
 *   <li>{@code POST   /api/journal}        - add one (validated by {@link JournalForm}); returns 201.</li>
 *   <li>{@code DELETE /api/journal/{id}}   - remove one; returns 204, or 404 if it doesn't exist.</li>
 * </ul>
 *
 * The controller stays the same thin shape as the rest of the app - validate inputs, delegate to the service,
 * let {@code ApiExceptionHandler} turn any thrown domain exception into a {@code ProblemDetail}. Only the
 * persistence mechanism behind {@link JournalService} differs (Spring Data JPA instead of JdbcTemplate).
 */
@RestController
@RequestMapping("/api/journal")
public class JournalController {

    private final JournalService journal;

    public JournalController(JournalService journal) {
        this.journal = journal;
    }

    @GetMapping
    public List<JournalEntry> list() {
        return journal.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JournalEntry create(@Valid @RequestBody JournalForm form) {
        return journal.add(form);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        journal.delete(id);
    }
}
