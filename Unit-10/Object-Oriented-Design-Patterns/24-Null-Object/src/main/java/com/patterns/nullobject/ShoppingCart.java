package com.patterns.nullobject;

import java.util.ArrayList;
import java.util.List;

/**
 * A shopping cart that applies a {@link Discount} at checkout.
 *
 * <p>The cart never checks whether the discount is null — it simply calls
 * {@code discount.apply(total)}. When no code is entered, the cart holds
 * a {@link NullDiscount} that returns the price unchanged.
 */
public class ShoppingCart {

    private final List<Integer> itemPricesCents = new ArrayList<>();
    private Discount discount;

    public ShoppingCart() {
        this.discount = NullDiscount.INSTANCE;
    }

    public void addItem(int priceCents) {
        itemPricesCents.add(priceCents);
    }

    public void applyDiscount(Discount discount) {
        this.discount = discount;
    }

    /** Total after discount (in cents). */
    public int total() {
        int subtotal = itemPricesCents.stream().mapToInt(Integer::intValue).sum();
        return discount.apply(subtotal);
    }

    public Discount getDiscount()    { return discount; }
    public int subtotal()            { return itemPricesCents.stream().mapToInt(Integer::intValue).sum(); }
}
