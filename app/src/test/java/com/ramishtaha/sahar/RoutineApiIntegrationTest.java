package com.ramishtaha.sahar;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * An INTEGRATION test (the top of the pyramid). {@code @SpringBootTest} starts the WHOLE application
 * context exactly as {@code main} would - real controllers, the real service, the real repositories, and
 * the in-memory H2 database with Flyway migrations applied and seeded (see src/test/resources/
 * application.properties). {@code @AutoConfigureMockMvc} gives us a {@link MockMvc} to drive HTTP without
 * binding a network port.
 *
 * <p>Slower than a slice test, but it proves the pieces work TOGETHER: a request really flows web -> service
 * -> repository -> database and back. A healthy suite has many unit/slice tests and a few of these.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RoutineApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void configIsSeeded() throws Exception {
        mvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sahar"))
                .andExpect(jsonPath("$.prayerTimes.fajr").value("04:37"))
                .andExpect(jsonPath("$.block.weeks.length()").value(4))
                .andExpect(jsonPath("$.dietPlan.length()").value(7))
                .andExpect(jsonPath("$.location.placeName").value("Thane, Maharashtra, India"));
    }

    @Test
    void editingTheMonthRoundTrips() throws Exception {
        mvc.perform(put("/api/month")
                        .with(httpBasic("admin", "sahar"))
                        .contentType("application/json")
                        .content("{\"month\":\"August 2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("August 2026"));

        mvc.perform(get("/api/config"))
                .andExpect(jsonPath("$.month").value("August 2026"));
    }

    @Test
    void changingLocationRecalculatesPrayerTimes() throws Exception {
        // Capture the seeded state so we can restore it - other tests assert the seeded values, and JUnit's
        // method order is not guaranteed, so this test must leave the database exactly as it found it.
        String seedLocation = mvc.perform(get("/api/location")).andReturn().getResponse().getContentAsString();
        String seedPrayerTimes = mvc.perform(get("/api/prayer-times")).andReturn().getResponse().getContentAsString();
        try {
            // Move to Null Island (0,0, UTC). Its prayer times cannot equal Thane's seeded 04:37 fajr, so if
            // the AFTER_COMMIT event fired and recomputed, the saved fajr must have changed.
            mvc.perform(put("/api/location")
                            .with(httpBasic("admin", "sahar"))
                            .contentType("application/json")
                            .content("{\"placeName\":\"Null Island\",\"lat\":0.0,\"lng\":0.0,\"tzOffset\":0}"))
                    .andExpect(status().isOk());

            mvc.perform(get("/api/prayer-times"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fajr").value(Matchers.not("04:37")));
        } finally {
            // Restore the location first (this fires the recalc again), THEN overwrite with the seeded times.
            mvc.perform(put("/api/location").with(httpBasic("admin", "sahar"))
                    .contentType("application/json").content(seedLocation));
            mvc.perform(put("/api/prayer-times").with(httpBasic("admin", "sahar"))
                    .contentType("application/json").content(seedPrayerTimes));
        }
    }

    @Test
    void aBadPrayerTimeIsRejected() throws Exception {
        mvc.perform(put("/api/prayer-times")
                        .with(httpBasic("admin", "sahar"))
                        .contentType("application/json")
                        .content("{\"fajr\":\"99:99\",\"sunrise\":\"05:59\",\"dhuhr\":\"12:37\",\"asr\":\"17:12\",\"maghrib\":\"19:13\",\"isha\":\"20:36\"}"))
                .andExpect(status().isBadRequest());
    }
}
