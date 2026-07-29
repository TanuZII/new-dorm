package th.ac.dusit.dorm.billing.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ChargeCalculatorTest {

    @Test
    void splitsUtilityChargeEquallyWithTwoDecimalRounding() {
        var calculator = new ChargeCalculator();

        var charge = calculator.utilityCharge(
                new BigDecimal("120"),
                new BigDecimal("135"),
                new BigDecimal("7.00"),
                2);

        assertThat(charge).isEqualByComparingTo("52.50");
    }

    @Test
    void rejectsMeterReadingLowerThanPreviousReading() {
        var calculator = new ChargeCalculator();

        assertThatThrownBy(() -> calculator.utilityCharge(
                new BigDecimal("135"),
                new BigDecimal("120"),
                new BigDecimal("7.00"),
                2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current meter reading cannot be lower than previous reading");
    }

    @Test
    void rejectsRoomWithoutOccupants() {
        var calculator = new ChargeCalculator();

        assertThatThrownBy(() -> calculator.utilityCharge(
                BigDecimal.ZERO,
                BigDecimal.ONE,
                new BigDecimal("7.00"),
                0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Occupant count must be greater than zero");
    }
}
