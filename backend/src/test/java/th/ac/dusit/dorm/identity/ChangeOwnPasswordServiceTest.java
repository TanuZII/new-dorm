package th.ac.dusit.dorm.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import th.ac.dusit.dorm.audit.AuditService;

class ChangeOwnPasswordServiceTest {

    private final AppUserRepository repository = org.mockito.Mockito.mock(AppUserRepository.class);
    private final AuditService auditService = org.mockito.Mockito.mock(AuditService.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);

    @Test
    void replacesTheHashWhenCurrentPasswordAndConfirmationAreCorrect() {
        var user = new AppUserEntity(
                "staff", encoder.encode("Current@1234"), "Staff", UserRole.DORM_STAFF);
        when(repository.findByUsernameIgnoreCase("staff")).thenReturn(Optional.of(user));
        var service = new UserService(repository, encoder, auditService);

        service.changeOwnPassword(
                "staff",
                new ChangePasswordRequest("Current@1234", "NewStrong@1234", "NewStrong@1234"),
                "127.0.0.1");

        assertThat(encoder.matches("NewStrong@1234", user.getPasswordHash())).isTrue();
        assertThat(encoder.matches("Current@1234", user.getPasswordHash())).isFalse();
    }

    @Test
    void rejectsAnIncorrectCurrentPasswordWithoutChangingTheHash() {
        String originalHash = encoder.encode("Current@1234");
        var user = new AppUserEntity("staff", originalHash, "Staff", UserRole.DORM_STAFF);
        when(repository.findByUsernameIgnoreCase("staff")).thenReturn(Optional.of(user));
        var service = new UserService(repository, encoder, auditService);

        assertThatThrownBy(() -> service.changeOwnPassword(
                "staff",
                new ChangePasswordRequest("Wrong@1234", "NewStrong@1234", "NewStrong@1234"),
                "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current password is incorrect");
        assertThat(user.getPasswordHash()).isEqualTo(originalHash);
    }

    @Test
    void rejectsMismatchedPasswordConfirmationWithoutChangingTheHash() {
        String originalHash = encoder.encode("Current@1234");
        var user = new AppUserEntity("staff", originalHash, "Staff", UserRole.DORM_STAFF);
        when(repository.findByUsernameIgnoreCase("staff")).thenReturn(Optional.of(user));
        var service = new UserService(repository, encoder, auditService);

        assertThatThrownBy(() -> service.changeOwnPassword(
                "staff",
                new ChangePasswordRequest("Current@1234", "NewStrong@1234", "Different@1234"),
                "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password confirmation does not match");
        assertThat(user.getPasswordHash()).isEqualTo(originalHash);
    }
}
