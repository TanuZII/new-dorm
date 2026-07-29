package th.ac.dusit.dorm.identity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService service;

    @Test
    void adminCanCreateAUser() throws Exception {
        when(service.create(any(), eq("admin"), any())).thenReturn(
                new UserResponse(12L, "finance.one", "เจ้าหน้าที่การเงิน",
                        "finance@example.org", UserRole.FINANCE, true));

        mockMvc.perform(post("/api/v1/users")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "finance.one",
                                  "password": "Strong@1234",
                                  "displayName": "เจ้าหน้าที่การเงิน",
                                  "email": "finance@example.org",
                                  "role": "FINANCE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.role").value("FINANCE"));
    }

    @Test
    void nonAdminCannotCreateAUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(user("staff").roles("DORM_STAFF"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "finance.one",
                                  "password": "Strong@1234",
                                  "displayName": "Finance",
                                  "role": "FINANCE"
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
