package th.ac.dusit.dorm.identity;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RoleController.class)
@Import(SecurityConfig.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService service;

    @Test
    void adminCanReadRolePermissionMatrix() throws Exception {
        when(service.findAll()).thenReturn(List.of(
                new RoleResponse("ADMIN", "ผู้ดูแลระบบ", null, true, 0,
                        Set.of("USERS:READ"))));

        mockMvc.perform(get("/api/v1/roles").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("ADMIN"))
                .andExpect(jsonPath("$[0].permissions[0]").value("USERS:READ"));
    }

    @Test
    void nonAdminCannotReadRolePermissionMatrix() throws Exception {
        mockMvc.perform(get("/api/v1/roles").with(user("staff").roles("DORM_STAFF")))
                .andExpect(status().isForbidden());
    }
}
