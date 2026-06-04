package com.ramishtaha.sahar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

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
                        .contentType("application/json")
                        .content("{\"month\":\"August 2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("August 2026"));

        mvc.perform(get("/api/config"))
                .andExpect(jsonPath("$.month").value("August 2026"));
    }

    @Test
    void aBadPrayerTimeIsRejected() throws Exception {
        mvc.perform(put("/api/prayer-times")
                        .contentType("application/json")
                        .content("{\"fajr\":\"99:99\",\"sunrise\":\"05:59\",\"dhuhr\":\"12:37\",\"asr\":\"17:12\",\"maghrib\":\"19:13\",\"isha\":\"20:36\"}"))
                .andExpect(status().isBadRequest());
    }
}
