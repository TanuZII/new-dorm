package th.ac.dusit.dorm.billing;

import java.math.BigDecimal;
import java.time.LocalDate;
import th.ac.dusit.dorm.billing.persistence.InvoiceEntity;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        Long tenantId,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal balance,
        String status) {

    static InvoiceResponse from(InvoiceEntity invoice) {
        return new InvoiceResponse(
                invoice.getId(), invoice.getInvoiceNumber(), invoice.getTenantId(),
                invoice.getIssueDate(), invoice.getDueDate(), invoice.getTotalAmount(),
                invoice.getPaidAmount(), invoice.balance(), invoice.getStatus().name());
    }
}

