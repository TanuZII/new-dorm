package th.ac.dusit.dorm.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import th.ac.dusit.dorm.tenant.persistence.TenantEntity;
import th.ac.dusit.dorm.tenant.persistence.TenantRepository;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {
    @Mock
    private TenantRepository repository;

    @Test
    void createsTenantWithNormalizedCode() {
        var service = new TenantService(repository);
        when(repository.existsByTenantCodeIgnoreCase("S68001")).thenReturn(false);
        when(repository.save(any(TenantEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.create(new CreateTenantRequest(
                " s68001 ", "STUDENT", "สมชาย", "ใจดี", "somchai@example.com", "0812345678"));

        assertThat(created.tenantCode()).isEqualTo("S68001");
        assertThat(created.fullName()).isEqualTo("สมชาย ใจดี");
        assertThat(created.active()).isTrue();
    }

    @Test
    void rejectsDuplicateTenantCode() {
        var service = new TenantService(repository);
        when(repository.existsByTenantCodeIgnoreCase("S68001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateTenantRequest(
                "S68001", "STUDENT", "สมชาย", "ใจดี", null, null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Tenant S68001 already exists");
        verify(repository, never()).save(any());
    }
}

