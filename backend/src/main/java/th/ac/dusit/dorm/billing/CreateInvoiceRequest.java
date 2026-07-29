package th.ac.dusit.dorm.billing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateInvoiceRequest(
        @NotBlank @Size(max = 40) String invoiceNumber,
        @NotNull @Positive Long tenantId,
        @NotNull LocalDate issueDate,
        @NotNull @FutureOrPresent LocalDate dueDate,
        @NotNull @DecimalMin(value = "0.01") BigDecimal totalAmount) {
}

