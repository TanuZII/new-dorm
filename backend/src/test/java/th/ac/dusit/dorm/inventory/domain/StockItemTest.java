package th.ac.dusit.dorm.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class StockItemTest {

    @Test
    void issueReducesAvailableStock() {
        var item = new StockItem("LAMP-01", new BigDecimal("10"));

        item.issue(new BigDecimal("3"));

        assertThat(item.quantity()).isEqualByComparingTo("7");
    }

    @Test
    void issueCannotMakeStockNegative() {
        var item = new StockItem("LAMP-01", new BigDecimal("2"));

        assertThatThrownBy(() -> item.issue(new BigDecimal("3")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Insufficient stock for LAMP-01");
    }
}
