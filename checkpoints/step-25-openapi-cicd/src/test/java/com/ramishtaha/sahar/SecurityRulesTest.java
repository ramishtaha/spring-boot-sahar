package com.ramishtaha.sahar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pins the security policy (step 24) so it can't silently regress: reads are public, writes need the admin.
 * Unlike {@code RoutineApiIntegrationTest} (which runs as a mock admin to exercise the endpoints), this test
 * drives security from the OUTSIDE - no {@code @WithMockUser} - so the real filter chain decides each case.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityRulesTest {

    @Autowired
    MockMvc mvc;

    @Test
    void readsArePublic() throws Exception {
        mvc.perform(get("/api/config")).andExpect(status().isOk());
    }

    @Test
    void writesAreRejectedWithoutCredentials() throws Exception {
        mvc.perform(put("/api/month")
                        .contentType("application/json")
                        .content("{\"month\":\"July 2026\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void writesSucceedWithBasicAuth() throws Exception {
        mvc.perform(put("/api/month")
                        .with(httpBasic("admin", "sahar"))
                        .contentType("application/json")
                        .content("{\"month\":\"July 2026\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void writesAreRejectedWithWrongPassword() throws Exception {
        mvc.perform(put("/api/month")
                        .with(httpBasic("admin", "nope"))
                        .contentType("application/json")
                        .content("{\"month\":\"July 2026\"}"))
                .andExpect(status().isUnauthorized());
    }
}
