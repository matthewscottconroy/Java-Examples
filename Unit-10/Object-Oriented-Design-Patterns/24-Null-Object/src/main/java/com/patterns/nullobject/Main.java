package com.patterns.nullobject;

/**
 * Demonstrates the Null Object pattern with a shopping cart discount system.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Shopping Cart (Null Object Pattern) ===\n");

        // Cart with no promo code — NullDiscount applied silently
        ShoppingCart cartA = new ShoppingCart();
        cartA.addItem(1999); // $19.99
        cartA.addItem(4999); // $49.99
        cartA.addItem( 599); // $ 5.99
        printReceipt("Cart A (no promo code)", cartA);

        // Cart with 20% off promo code
        ShoppingCart cartB = new ShoppingCart();
        cartB.addItem(1999);
        cartB.addItem(4999);
        cartB.addItem( 599);
        cartB.applyDiscount(new PercentageDiscount(20, "SAVE20"));
        printReceipt("Cart B (SAVE20)", cartB);
    }

    private static void printReceipt(String label, ShoppingCart cart) {
        System.out.println(label);
        System.out.printf("  Subtotal : $%.2f%n", cart.subtotal() / 100.0);
        System.out.printf("  Discount : %s%n",    cart.getDiscount().description());
        System.out.printf("  Total    : $%.2f%n%n", cart.total() / 100.0);
    }
}
