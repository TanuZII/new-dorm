package th.ac.dusit.dorm.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.http.MediaType;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import th.ac.dusit.dorm.common.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Test
    void loginPersistsAuthenticatedSessionAndReturnsUserProfile() {
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var controller = new AuthController(authenticationManager, userService);

        var result = controller.login(new LoginRequest("admin", "password123"), request, response);

        assertThat(result.username()).isEqualTo("admin");
        assertThat(result.roles()).containsExactly("ADMIN");
        assertThat(request.getSession(false).getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY)).isNotNull();
    }

    @Test
    void invalidCredentialsReturnUnauthorizedInsteadOfServerError() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        var mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authenticationManager, userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
    }
}
