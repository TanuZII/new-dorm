package th.ac.dusit.dorm.reporting;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import th.ac.dusit.dorm.identity.SecurityConfig;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void anonymousUserCannotReadOperationalDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authorizedStaffReceivesDormitoryMetrics() throws Exception {
        when(dashboardService.summary()).thenReturn(
                new DashboardSummary(120, 18, 7, 3, 24500));

        mockMvc.perform(get("/api/v1/dashboard")
                        .with(user("staff").roles("DORM_STAFF")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRooms").value(120))
                .andExpect(jsonPath("$.availableBeds").value(18))
                .andExpect(jsonPath("$.overdueInvoices").value(7))
                .andExpect(jsonPath("$.openMaintenanceRequests").value(3))
                .andExpect(jsonPath("$.outstandingAmount").value(24500));
    }
}
