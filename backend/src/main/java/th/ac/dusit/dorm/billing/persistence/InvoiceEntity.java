package th.ac.dusit.dorm.billing.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import th.ac.dusit.dorm.billing.domain.InvoiceStatus;

@Entity
@Table(name = "invoices")
public class InvoiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 40)
    private String invoiceNumber;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO.setScale(2);

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceStatus status = InvoiceStatus.ISSUED;

    @Column(name = "void_reason", length = 500)
    private String voidReason;

    protected InvoiceEntity() {
    }

    public InvoiceEntity(String invoiceNumber, Long tenantId, LocalDate issueDate,
                         LocalDate dueDate, BigDecimal totalAmount) {
        this.invoiceNumber = invoiceNumber;
        this.tenantId = tenantId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
    }

    public void recordPayment(BigDecimal amount) {
        if (status != InvoiceStatus.ISSUED && status != InvoiceStatus.PARTIALLY_PAID) {
            throw new IllegalStateException("Invoice is not open for payment");
        }
        if (amount.signum() <= 0) throw new IllegalArgumentException("Payment must be greater than zero");
        if (amount.compareTo(balance()) > 0) throw new IllegalStateException("Payment exceeds outstanding balance");
        paidAmount = paidAmount.add(amount).setScale(2, RoundingMode.HALF_UP);
        status = balance().signum() == 0 ? InvoiceStatus.PAID : InvoiceStatus.PARTIALLY_PAID;
    }

    public void voidInvoice(String reason) {
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Void reason is required");
        if (status == InvoiceStatus.PAID) throw new IllegalStateException("Paid invoice cannot be voided");
        voidReason = reason.trim();
        status = InvoiceStatus.VOID;
    }

    public BigDecimal balance() { return totalAmount.subtract(paidAmount).setScale(2, RoundingMode.HALF_UP); }
    public Long getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public Long getTenantId() { return tenantId; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public InvoiceStatus getStatus() { return status; }
}

