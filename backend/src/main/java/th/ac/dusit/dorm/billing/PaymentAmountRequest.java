package th.ac.dusit.dorm.billing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentAmountRequest(@NotNull @DecimalMin("0.01") BigDecimal amount) {
}

