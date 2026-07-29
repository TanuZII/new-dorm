package th.ac.dusit.dorm.inventory.domain;

import java.math.BigDecimal;

public final class StockItem {
    private final String sku;
    private BigDecimal quantity;

    public StockItem(String sku, BigDecimal openingQuantity) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU is required");
        }
        if (openingQuantity == null || openingQuantity.signum() < 0) {
            throw new IllegalArgumentException("Opening quantity cannot be negative");
        }
        this.sku = sku.trim().toUpperCase();
        this.quantity = openingQuantity;
    }

    public void receive(BigDecimal amount) {
        requirePositive(amount);
        quantity = quantity.add(amount);
    }

    public void issue(BigDecimal amount) {
        requirePositive(amount);
        if (amount.compareTo(quantity) > 0) {
            throw new IllegalStateException("Insufficient stock for " + sku);
        }
        quantity = quantity.subtract(amount);
    }

    public BigDecimal quantity() {
        return quantity;
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Movement quantity must be greater than zero");
        }
    }
}

