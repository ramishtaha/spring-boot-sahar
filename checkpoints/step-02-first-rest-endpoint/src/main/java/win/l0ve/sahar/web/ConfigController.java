package win.l0ve.sahar.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The first REST endpoint. {@code GET /api/config} returns the whole routine as JSON.
 *
 * <p>How the magic happens:
 * <ul>
 *   <li>{@code @RestController} = {@code @Controller} + {@code @ResponseBody}. It tells Spring MVC
 *       that every method's return value IS the response body (not a view name to render).</li>
 *   <li>{@code @GetMapping("/api/config")} maps HTTP GET requests for that path to this method.</li>
 *   <li>Returning a Java object (here a {@code Map}) triggers Jackson, which serializes it to JSON
 *       and sets {@code Content-Type: application/json}. No manual string building.</li>
 * </ul>
 *
 * <p>NOTE: the data is hardcoded and modelled as an untyped {@code Map<String, Object>} on purpose.
 * It works, but look how fragile it is - a typo in a key is invisible to the compiler, the shape is
 * not guaranteed, and nesting maps-of-maps gets ugly fast. Step 03 replaces all of this with proper
 * Java records (the domain model), and the JSON shape stays identical.
 */
@RestController
public class ConfigController {

    @GetMapping("/api/config")
    public Map<String, Object> config() {
        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("title", "Sahar");
        cfg.put("tagline", "recover, build, fight");
        cfg.put("month", "June 2026");
        cfg.put("prayerTimes", prayerTimes());
        cfg.put("block", block());
        cfg.put("weeklyGrid", weeklyGrid());
        return cfg;
    }

    private Map<String, Object> prayerTimes() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("fajr", "04:37");
        p.put("sunrise", "05:59");
        p.put("dhuhr", "12:37");
        p.put("asr", "17:12");
        p.put("maghrib", "19:13");
        p.put("isha", "20:36");
        p.put("methodNote", "Karachi 18° Fajr/Isha, Hanafi Asr, computed for Thane (19.22N, 72.98E). "
                + "Recheck monthly; drifts under ~10 min across a month.");
        return p;
    }

    private Map<String, Object> block() {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("label", "4-week block · 8 Jun – 5 Jul (Foundation · Build · Peak · Deload)");
        b.put("weeks", List.of(
                week(1, "Foundation", "8 Jun", "14 Jun", false,
                        "Moderate, no PM sessions; lock the schedule and the journaling habit.",
                        "Spring Boot core: setup, dependency injection, controllers, a CRUD REST API."),
                week(2, "Build", "15 Jun", "21 Jun", false,
                        "Add PM strength Mon and Thu; deeper deep-work; volume climbs.",
                        "Persistence: Spring Data JPA, Postgres, repositories, validation."),
                week(3, "Peak", "22 Jun", "28 Jun", false,
                        "Full volume, sharpest spar, deepest learning.",
                        "Docker and DevOps: Dockerfile, compose, env config, basic CI."),
                week(4, "Deload", "29 Jun", "5 Jul", true,
                        "MMA down ~40%, light or skipped spar, weekday mains drilling only, more sleep.",
                        "Ship it: deploy the container, refactor, docs.")
        ));
        return b;
    }

    private Map<String, Object> week(int ordinal, String name, String start, String end,
                                      boolean deload, String training, String backend) {
        Map<String, Object> w = new LinkedHashMap<>();
        w.put("ordinal", ordinal);
        w.put("name", name);
        w.put("startDate", start);
        w.put("endDate", end);
        w.put("deload", deload);
        w.put("trainingFocus", training);
        w.put("backendFocus", backend);
        return w;
    }

    private List<Map<String, Object>> weeklyGrid() {
        return List.of(
                gridDay("Mon", "Muay Thai (technical)"),
                gridDay("Tue", "Boxing"),
                gridDay("Wed", "Wrestling / BJJ (light)"),
                gridDay("Thu", "Muay Thai"),
                gridDay("Fri", "Padwork + light drills (taper)"),
                gridDay("Sat", "SPAR + BJJ / MMA rounds"),
                gridDay("Sun", "Rest / mobility walk")
        );
    }

    private Map<String, Object> gridDay(String day, String discipline) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("day", day);
        d.put("discipline", discipline);
        return d;
    }
}
