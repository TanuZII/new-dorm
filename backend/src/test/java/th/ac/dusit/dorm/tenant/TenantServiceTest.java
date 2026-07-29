package th.ac.dusit.dorm.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import th.ac.dusit.dorm.audit.AuditService;
import th.ac.dusit.dorm.common.DomainConflictException;
import th.ac.dusit.dorm.tenant.persistence.TenantEntity;
import th.ac.dusit.dorm.tenant.persistence.TenantRepository;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {
    @Mock private TenantRepository repository;
    @Mock private TenantCodeGenerator codeGenerator;
    @Mock private AuditService auditService;

    @Test
    void createsStudentWithGeneratedCodeNestedDetailsAndAudit() {
        when(codeGenerator.nextCode()).thenReturn("TEN-000001");
        when(repository.existsByInstitutionalIdIgnoreCase("68001")).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = service();

        var result = service.create(studentRequest(), "admin", "127.0.0.1");

        var captor = ArgumentCaptor.forClass(TenantEntity.class);
        verify(repository).save(captor.capture());
        assertThat(result.tenantCode()).isEqualTo("TEN-000001");
        assertThat(result.institutionalId()).isEqualTo("68001");
        assertThat(result.addresses()).hasSize(1);
        assertThat(result.contacts()).hasSize(1);
        assertThat(captor.getValue().getFirstName()).isEqualTo("สมชาย");
        verify(auditService).record(
                "admin", "TENANT_CREATED", "TENANT", "TEN-000001", null,
                "127.0.0.1", Map.of("tenantType", "STUDENT"));
    }

    @Test
    void rejectsMissingOrDuplicateInstitutionalIdentifier() {
        var service = service();

        assertThatThrownBy(() -> service.create(
                studentRequestWithInstitutionalId(" "), "admin", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("institutionalId");

        when(repository.existsByInstitutionalIdIgnoreCase("68001")).thenReturn(true);
        assertThatThrownBy(() -> service.create(studentRequest(), "admin", "127.0.0.1"))
                .isInstanceOf(DomainConflictException.class)
                .extracting("code")
                .isEqualTo("TENANT_IDENTIFIER_DUPLICATE");
    }

    @Test
    void rejectsAStaleUpdateVersion() {
        var tenant = new TenantEntity(
                "TEN-000001", TenantType.STUDENT, "68001", null,
                "สมชาย", "ใจดี", null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(tenant));
        var service = service();

        assertThatThrownBy(() -> service.update(
                1L,
                new UpdateTenantRequest(
                        TenantType.STUDENT, "68001", null, "สมชาย", "ใจดี",
                        null, null, List.of(), List.of(), 99L),
                "admin", "127.0.0.1"))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void deactivationRequiresReasonAndWritesAudit() {
        var tenant = new TenantEntity(
                "TEN-000001", TenantType.EXTERNAL, null, null,
                "Alex", "Smith", null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(tenant));
        var service = service();

        assertThatThrownBy(() -> service.changeStatus(
                1L, new ChangeTenantStatusRequest(false, " ", 0L), "admin", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class);

        var changed = service.changeStatus(
                1L, new ChangeTenantStatusRequest(false, "สิ้นสุดสถานะ", 0L),
                "admin", "127.0.0.1");

        assertThat(changed.active()).isFalse();
        verify(auditService).record(
                "admin", "TENANT_DEACTIVATED", "TENANT", "TEN-000001",
                "สิ้นสุดสถานะ", "127.0.0.1", Map.of());
    }

    private TenantService service() {
        return new TenantService(repository, codeGenerator, auditService);
    }

    private CreateTenantRequest studentRequest() {
        return studentRequestWithInstitutionalId(" 68001 ");
    }

    private CreateTenantRequest studentRequestWithInstitutionalId(String institutionalId) {
        return new CreateTenantRequest(
                TenantType.STUDENT, institutionalId, null, " สมชาย ", " ใจดี ",
                " somchai@example.com ", " 0812345678 ",
                List.of(new TenantAddressRequest(
                        "CURRENT", "123 ถนนมหาวิทยาลัย", null, null, null, "10110", "TH")),
                List.of(new TenantContactRequest(
                        "EMERGENCY", "มานี ใจดี", "มารดา", "0899999999", null, true)));
    }
}
