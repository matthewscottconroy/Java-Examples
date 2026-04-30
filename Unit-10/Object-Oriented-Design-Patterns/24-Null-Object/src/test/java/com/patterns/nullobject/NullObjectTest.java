package com.patterns.nullobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NullObjectTest {

    @Test
    @DisplayName("NullDiscount returns price unchanged")
    void nullDiscountIsIdentity() {
        assertEquals(1000, NullDiscount.INSTANCE.apply(1000));
    }

    @Test
    @DisplayName("PercentageDiscount reduces price correctly")
    void percentageDiscountReducesPrice() {
        Discount d = new PercentageDiscount(25, "TEST25");
        assertEquals(750, d.apply(1000));
    }

    @Test
    @DisplayName("Cart with no promo code uses NullDiscount by default")
    void cartDefaultsToNullDiscount() {
        ShoppingCart cart = new ShoppingCart();
        assertInstanceOf(NullDiscount.class, cart.getDiscount());
    }

    @Test
    @DisplayName("Cart total with NullDiscount equals subtotal")
    void cartTotalEqualsSubtotalWithNoDiscount() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(500);
        cart.addItem(300);
        assertEquals(800, cart.total());
        assertEquals(cart.subtotal(), cart.total());
    }

    @Test
    @DisplayName("Cart total with 20% discount is 80% of subtotal")
    void cartAppliesPercentageDiscount() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(1000);
        cart.applyDiscount(new PercentageDiscount(20, "SAVE20"));
        assertEquals(800, cart.total());
    }

    @Test
    @DisplayName("NullDiscount description is non-null and non-empty")
    void nullDiscountHasDescription() {
        String desc = NullDiscount.INSTANCE.description();
        assertNotNull(desc);
        assertFalse(desc.isBlank());
    }

    @Test
    @DisplayName("PercentageDiscount rejects invalid percent values")
    void invalidPercentThrows() {
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscount(101, "X"));
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscount(-1, "X"));
    }

    @Test
    @DisplayName("NullDiscount is a singleton")
    void nullDiscountIsSingleton() {
        assertSame(NullDiscount.INSTANCE, NullDiscount.INSTANCE);
    }
}
