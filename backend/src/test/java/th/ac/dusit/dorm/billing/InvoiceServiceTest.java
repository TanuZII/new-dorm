package th.ac.dusit.dorm.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import th.ac.dusit.dorm.billing.persistence.InvoiceEntity;
import th.ac.dusit.dorm.billing.persistence.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
    @Mock
    private InvoiceRepository repository;

    @Test
    void createsIssuedInvoiceWithFullOutstandingBalance() {
        var service = new InvoiceService(repository);
        when(repository.existsByInvoiceNumber("INV-2569-0001")).thenReturn(false);
        when(repository.save(any(InvoiceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new CreateInvoiceRequest(
                "INV-2569-0001", 15L, LocalDate.of(2026, 7, 29),
                LocalDate.of(2026, 8, 5), new BigDecimal("2500.00")));

        assertThat(response.status()).isEqualTo("ISSUED");
        assertThat(response.balance()).isEqualByComparingTo("2500.00");
    }

    @Test
    void recordsPartialPaymentWithoutLosingOutstandingBalance() {
        var invoice = new InvoiceEntity(
                "INV-2569-0001", 15L, LocalDate.now(), LocalDate.now().plusDays(7),
                new BigDecimal("1000.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(invoice));
        var service = new InvoiceService(repository);

        var response = service.recordPayment(1L, new BigDecimal("400.00"));

        assertThat(response.status()).isEqualTo("PARTIALLY_PAID");
        assertThat(response.balance()).isEqualByComparingTo("600.00");
    }

    @Test
    void rejectsDuplicateInvoiceNumber() {
        when(repository.existsByInvoiceNumber("INV-2569-0001")).thenReturn(true);
        var service = new InvoiceService(repository);

        assertThatThrownBy(() -> service.create(new CreateInvoiceRequest(
                "INV-2569-0001", 15L, LocalDate.now(), LocalDate.now().plusDays(7), BigDecimal.ONE)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invoice INV-2569-0001 already exists");
    }
}
