package com.patterns.abstractfactory;

/**
 * Demonstrates the Abstract Factory pattern with two furniture styles.
 *
 * <p>The {@link InteriorDesigner} client receives a factory and uses it to
 * furnish a room. Swapping the factory changes every piece of furniture in the
 * room — the designer's code does not change at all.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== The Furniture Store (Abstract Factory Pattern) ===\n");

        InteriorDesigner designer = new InteriorDesigner(new ScandinavianFactory());
        System.out.println("Room furnished in Scandinavian style:");
        designer.furnishRoom();

        System.out.println();

        // Hand the designer a different factory — same designer, completely different room
        designer = new InteriorDesigner(new IndustrialFactory());
        System.out.println("Room furnished in Industrial style:");
        designer.furnishRoom();

        System.out.println();
        System.out.println("Key point: InteriorDesigner never imported ScandinavianFactory");
        System.out.println("or IndustrialFactory. It only used FurnitureFactory, Chair,");
        System.out.println("Table, and Lamp. Swap the factory; the room changes completely.");
    }
}
