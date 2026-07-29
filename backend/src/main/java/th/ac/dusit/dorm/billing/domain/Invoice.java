package th.ac.dusit.dorm.billing.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Invoice {
    private final BigDecimal total;
    private BigDecimal paid = BigDecimal.ZERO.setScale(2);
    private InvoiceStatus status = InvoiceStatus.DRAFT;
    private String voidReason;

    public Invoice(BigDecimal total) {
        if (total == null || total.signum() <= 0) {
            throw new IllegalArgumentException("Invoice total must be greater than zero");
        }
        this.total = total.setScale(2, RoundingMode.HALF_UP);
    }

    public void issue() {
        if (status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only a draft invoice can be issued");
        }
        status = InvoiceStatus.ISSUED;
    }

    public void recordPayment(BigDecimal amount) {
        if (status != InvoiceStatus.ISSUED && status != InvoiceStatus.PARTIALLY_PAID) {
            throw new IllegalStateException("Invoice is not open for payment");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment must be greater than zero");
        }
        if (amount.compareTo(balance()) > 0) {
            throw new IllegalStateException("Payment exceeds outstanding balance");
        }

        paid = paid.add(amount).setScale(2, RoundingMode.HALF_UP);
        status = balance().signum() == 0 ? InvoiceStatus.PAID : InvoiceStatus.PARTIALLY_PAID;
    }

    public void voidInvoice(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Void reason is required");
        }
        if (status == InvoiceStatus.PAID) {
            throw new IllegalStateException("Paid invoice cannot be voided");
        }
        voidReason = reason.trim();
        status = InvoiceStatus.VOID;
    }

    public BigDecimal balance() {
        return total.subtract(paid).setScale(2, RoundingMode.HALF_UP);
    }

    public InvoiceStatus status() {
        return status;
    }

    public String voidReason() {
        return voidReason;
    }
}

