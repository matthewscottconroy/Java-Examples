package com.patterns.nullobject;

/**
 * Discount strategy — every discount must be able to apply itself to a price.
 *
 * <p>A {@link NullDiscount} implements this interface and does nothing,
 * eliminating the need for null-checks in {@link ShoppingCart}.
 */
public interface Discount {

    /**
     * Apply this discount to a price in cents.
     *
     * @param priceCents original price
     * @return discounted price (may equal the original)
     */
    int apply(int priceCents);

    /** Short human-readable description shown on receipts. */
    String description();
}
