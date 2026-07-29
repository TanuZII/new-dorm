package th.ac.dusit.dorm.tenant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import th.ac.dusit.dorm.common.DomainConflictException;

@SpringBootTest
@AutoConfigureMockMvc
class TenantControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private TenantService service;

    @Test
    void financeCanSearchButCannotCreate() throws Exception {
        when(service.search(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response())));

        mockMvc.perform(get("/api/v1/tenants")
                        .param("type", "STUDENT")
                        .with(user("finance").roles("FINANCE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tenantCode").value("TEN-000001"));

        mockMvc.perform(post("/api/v1/tenants")
                        .with(user("finance").roles("FINANCE"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validStudentJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void dormStaffCanCreateAndTenantCodeIsServerGenerated() throws Exception {
        when(service.create(any(CreateTenantRequest.class), eq("staff"), anyString()))
                .thenReturn(response());

        mockMvc.perform(post("/api/v1/tenants")
                        .with(user("staff").roles("DORM_STAFF"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validStudentJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/tenants/1"))
                .andExpect(jsonPath("$.tenantCode").value("TEN-000001"));
    }

    @Test
    void duplicateIdentifierUsesStableConflictEnvelope() throws Exception {
        when(service.create(any(CreateTenantRequest.class), eq("staff"), anyString()))
                .thenThrow(new DomainConflictException(
                        "TENANT_IDENTIFIER_DUPLICATE", "Institutional identifier already exists"));

        mockMvc.perform(post("/api/v1/tenants")
                        .with(user("staff").roles("DORM_STAFF"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validStudentJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TENANT_IDENTIFIER_DUPLICATE"));
    }

    @Test
    void invalidNestedContactIsRejectedBeforeService() throws Exception {
        mockMvc.perform(post("/api/v1/tenants")
                        .with(user("staff").roles("DORM_STAFF"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validStudentJson().replace("0899999999", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    private TenantResponse response() {
        return new TenantResponse(
                1L, "TEN-000001", TenantType.STUDENT, "68001", null,
                "สมชาย", "ใจดี", "สมชาย ใจดี", "somchai@example.com", "0812345678",
                true, 0L, List.of(), List.of());
    }

    private String validStudentJson() {
        return """
                {
                  "tenantType": "STUDENT",
                  "institutionalId": "68001",
                  "firstName": "สมชาย",
                  "lastName": "ใจดี",
                  "email": "somchai@example.com",
                  "phone": "0812345678",
                  "contacts": [{
                    "contactType": "EMERGENCY",
                    "fullName": "มานี ใจดี",
                    "phone": "0899999999",
                    "primaryContact": true
                  }]
                }
                """;
    }
}
