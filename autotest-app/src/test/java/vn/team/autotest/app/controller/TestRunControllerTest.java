package vn.team.autotest.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.team.autotest.app.service.TestExecutionService;
import vn.team.autotest.core.model.RunSummary;
import vn.team.autotest.core.model.TestStatus;

class TestRunControllerTest {
    @Test
    void bindsRunIdWhenDashboardPollsExecutionStatus() throws Exception {
        RunSummary summary = new RunSummary(
                "run-qa", TestStatus.RUNNING, Instant.now(), null, 0, 0, 0, 0, 0, List.of());
        TestExecutionService service = new TestExecutionService(Runnable::run) {
            @Override public RunSummary find(String runId) {
                assertEquals("run-qa", runId);
                return summary;
            }
        };
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new TestRunController(service)).build();

        mvc.perform(get("/api/runs/{runId}", "run-qa"))
                .andExpect(status().isOk());
    }
}
