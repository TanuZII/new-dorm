package th.ac.dusit.dorm.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class DatabaseUserDetailsServiceTest {

    @Test
    void resolvesAllDatabaseRolesAndPermissionsAsAuthorities() {
        var repository = org.mockito.Mockito.mock(AppUserRepository.class);
        var user = new AppUserEntity("manager", "hash", "Manager", UserRole.DORM_STAFF);
        when(repository.findByUsernameIgnoreCase("manager")).thenReturn(Optional.of(user));
        when(repository.findRoleCodes(user.getId())).thenReturn(List.of("DORM_STAFF", "APPROVER"));
        when(repository.findPermissionCodes(user.getId())).thenReturn(
                List.of("PROPERTY:READ", "APPROVAL:WRITE"));
        var service = new DatabaseUserDetailsService(repository);

        var details = service.loadUserByUsername("manager");

        assertThat(details.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactlyInAnyOrder(
                        "ROLE_DORM_STAFF",
                        "ROLE_APPROVER",
                        "PERM_PROPERTY_READ",
                        "PERM_APPROVAL_WRITE");
    }
}
