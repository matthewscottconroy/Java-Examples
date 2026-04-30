package com.patterns.decorator;

/**
 * Demonstrates the Decorator pattern at a coffee shop.
 *
 * <p>Each beverage is built by wrapping a base drink in condiment decorators.
 * The cost and description accumulate through the chain. No subclass per
 * combination is needed — the combinations are infinite.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== The Coffee Shop (Decorator Pattern) ===\n");

        // Plain espresso
        Beverage espresso = new Espresso();
        printOrder(espresso);

        // Latte = espresso + steamed milk
        Beverage latte = new SteamedMilk(new Espresso());
        printOrder(latte);

        // Vanilla latte = espresso + steamed milk + vanilla syrup
        Beverage vanillaLatte = new VanillaSyrup(new SteamedMilk(new Espresso()));
        printOrder(vanillaLatte);

        // The full monty
        Beverage grandeDrink = new WhipCream(
                                 new VanillaSyrup(
                                   new ExtraShot(
                                     new SteamedMilk(
                                       new Espresso()))));
        printOrder(grandeDrink);

        // House blend with just whip
        Beverage whippedHouseBlend = new WhipCream(new HouseBlend());
        printOrder(whippedHouseBlend);

        System.out.println("\nNote: no EspressoWithMilkAndVanilla class was written.");
        System.out.println("The combinations are formed by composing decorators at runtime.");
    }

    private static void printOrder(Beverage b) {
        System.out.printf("%-55s %s%n", b.getDescription(), b.formattedCost());
    }
}
