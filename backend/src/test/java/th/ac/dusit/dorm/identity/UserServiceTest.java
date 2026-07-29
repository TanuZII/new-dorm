package th.ac.dusit.dorm.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import th.ac.dusit.dorm.audit.AuditService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AppUserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @Test
    void createsAUserWithNormalizedUsernameHashedPasswordAndAudit() {
        when(repository.existsByUsernameIgnoreCase("finance.one")).thenReturn(false);
        when(passwordEncoder.encode("Strong@1234")).thenReturn("bcrypt-hash");
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new UserService(repository, passwordEncoder, auditService);

        var result = service.create(
                new CreateUserRequest(
                        " Finance.One ", "Strong@1234", " เจ้าหน้าที่การเงิน ",
                        " finance@example.org ", UserRole.FINANCE),
                "admin",
                "127.0.0.1");

        var captor = ArgumentCaptor.forClass(AppUserEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("finance.one");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(result.displayName()).isEqualTo("เจ้าหน้าที่การเงิน");
        assertThat(result.email()).isEqualTo("finance@example.org");
        assertThat(result.role()).isEqualTo(UserRole.FINANCE);
        verify(auditService).record(
                "admin", "USER_CREATED", "USER", "finance.one", null, "127.0.0.1", "{}");
    }

    @Test
    void rejectsDuplicateUsernameBeforeEncodingPassword() {
        when(repository.existsByUsernameIgnoreCase("admin")).thenReturn(true);
        var service = new UserService(repository, passwordEncoder, auditService);

        assertThatThrownBy(() -> service.create(
                new CreateUserRequest(
                        "ADMIN", "Strong@1234", "Duplicate", null, UserRole.ADMIN),
                "admin",
                "127.0.0.1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Username admin already exists");
    }

    @Test
    void deactivatesAUserOnlyWithARecordedReason() {
        var user = new AppUserEntity(
                "staff.one", "hash", "Dorm staff", "staff@example.org", UserRole.DORM_STAFF);
        when(repository.findById(7L)).thenReturn(Optional.of(user));
        var service = new UserService(repository, passwordEncoder, auditService);

        var result = service.changeStatus(
                7L, new ChangeUserStatusRequest(false, "พ้นสภาพเจ้าหน้าที่"),
                "admin", "127.0.0.1");

        assertThat(result.active()).isFalse();
        verify(auditService).record(
                "admin", "USER_DEACTIVATED", "USER", "staff.one",
                "พ้นสภาพเจ้าหน้าที่", "127.0.0.1", "{}");
    }

    @Test
    void resetsPasswordUsingTheConfiguredEncoderAndAudit() {
        var user = new AppUserEntity(
                "staff.one", "old-hash", "Dorm staff", null, UserRole.DORM_STAFF);
        when(repository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewStrong@1234")).thenReturn("new-hash");
        var service = new UserService(repository, passwordEncoder, auditService);

        service.resetPassword(
                7L, new ResetUserPasswordRequest("NewStrong@1234", "ผู้ใช้ร้องขอ"),
                "admin", "127.0.0.1");

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(auditService).record(
                "admin", "USER_PASSWORD_RESET", "USER", "staff.one",
                "ผู้ใช้ร้องขอ", "127.0.0.1", "{}");
    }
}
