package th.ac.dusit.dorm.billing.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ChargeCalculator {

    public BigDecimal utilityCharge(
            BigDecimal previousReading,
            BigDecimal currentReading,
            BigDecimal ratePerUnit,
            int occupantCount) {
        if (currentReading.compareTo(previousReading) < 0) {
            throw new IllegalArgumentException(
                    "Current meter reading cannot be lower than previous reading");
        }
        if (occupantCount <= 0) {
            throw new IllegalArgumentException("Occupant count must be greater than zero");
        }

        return currentReading.subtract(previousReading)
                .multiply(ratePerUnit)
                .divide(BigDecimal.valueOf(occupantCount), 2, RoundingMode.HALF_UP);
    }
}
