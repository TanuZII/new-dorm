package th.ac.dusit.dorm.billing.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class InvoiceTest {

    @Test
    void recordsPartialPaymentBeforeMarkingInvoicePaid() {
        var invoice = new Invoice(new BigDecimal("1000.00"));

        invoice.issue();
        invoice.recordPayment(new BigDecimal("400.00"));

        assertThat(invoice.status()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
        assertThat(invoice.balance()).isEqualByComparingTo("600.00");

        invoice.recordPayment(new BigDecimal("600.00"));
        assertThat(invoice.status()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.balance()).isEqualByComparingTo("0.00");
    }

    @Test
    void rejectsPaymentAboveOutstandingBalance() {
        var invoice = new Invoice(new BigDecimal("500.00"));
        invoice.issue();

        assertThatThrownBy(() -> invoice.recordPayment(new BigDecimal("500.01")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Payment exceeds outstanding balance");
    }

    @Test
    void requiresReasonWhenVoidingIssuedInvoice() {
        var invoice = new Invoice(new BigDecimal("500.00"));
        invoice.issue();

        assertThatThrownBy(() -> invoice.voidInvoice(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Void reason is required");
    }
}

