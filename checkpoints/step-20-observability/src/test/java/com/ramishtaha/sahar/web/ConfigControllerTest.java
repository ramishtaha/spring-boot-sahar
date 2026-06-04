package com.ramishtaha.sahar.web;

import com.ramishtaha.sahar.domain.RoutineConfig;
import com.ramishtaha.sahar.service.RoutineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A SLICE test (the middle of the testing pyramid). {@code @WebMvcTest} loads ONLY the web layer for one
 * controller - the JSON mapping, the {@code DispatcherServlet}, the advice - and nothing else. There is no
 * database and no real service; we hand the controller a fake {@link RoutineService} with
 * {@code @MockitoBean} and tell it exactly what to return. That makes the test fast and focused: it proves
 * the HTTP wiring (URL, status, JSON shape), not the business logic.
 *
 * <p>{@code @MockitoBean} is the Spring Boot 4 way to replace a bean with a Mockito mock (it superseded the
 * old {@code @MockBean}).
 */
@WebMvcTest(ConfigController.class)
class ConfigControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    RoutineService routine;

    @Test
    void getConfigReturnsJson() throws Exception {
        given(routine.getConfig()).willReturn(new RoutineConfig(
                "Sahar", "recover, build, fight", "June 2026",
                null, null, List.of(), List.of(), List.of(), List.of(),
                null, List.of(), null, List.of(), List.of(), null));

        mvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sahar"))
                .andExpect(jsonPath("$.month").value("June 2026"));
    }
}
