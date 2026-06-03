package win.l0ve.sahar.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import win.l0ve.sahar.domain.ScheduleItem;
import win.l0ve.sahar.service.RoutineService;

import java.util.List;

/**
 * Full CRUD for the daily timeline - the classic five REST endpoints over a collection resource.
 *
 * <ul>
 *   <li>{@code GET    /api/schedule}      - list all slots (ordered by time).</li>
 *   <li>{@code POST   /api/schedule}      - create a slot; responds 201 Created with the new row (incl. id).</li>
 *   <li>{@code PUT    /api/schedule/{id}} - replace the slot at that id; 404 if it does not exist.</li>
 *   <li>{@code DELETE /api/schedule/{id}} - remove it; responds 204 No Content.</li>
 * </ul>
 *
 * <p>The status codes are deliberate: 201 says "created, here it is", 204 says "done, nothing to return".
 * Spring sets them from {@code @ResponseStatus}. On create the {@code id} in the body is null on the way
 * in and populated on the way out - the database assigned it.
 */
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final RoutineService routine;

    public ScheduleController(RoutineService routine) {
        this.routine = routine;
    }

    @GetMapping
    public List<ScheduleItem> list() {
        return routine.listSchedule();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleItem create(@Valid @RequestBody ScheduleItem item) {
        return routine.createScheduleItem(item);
    }

    @PutMapping("/{id}")
    public ScheduleItem update(@PathVariable Long id, @Valid @RequestBody ScheduleItem item) {
        return routine.updateScheduleItem(id, item);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        routine.deleteScheduleItem(id);
    }
}
