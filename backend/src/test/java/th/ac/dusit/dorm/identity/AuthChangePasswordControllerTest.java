package th.ac.dusit.dorm.identity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthChangePasswordControllerTest {

    @Test
    void authenticatedUserCanChangeOwnPassword() throws Exception {
        var authenticationManager = org.mockito.Mockito.mock(AuthenticationManager.class);
        var userService = org.mockito.Mockito.mock(UserService.class);
        var controller = new AuthController(authenticationManager, userService);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                "staff", null, List.of());
        var mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "Current@1234",
                                  "newPassword": "NewStrong@1234",
                                  "confirmPassword": "NewStrong@1234"
                                }
                                """))
                .andExpect(status().isNoContent());
    }
}
