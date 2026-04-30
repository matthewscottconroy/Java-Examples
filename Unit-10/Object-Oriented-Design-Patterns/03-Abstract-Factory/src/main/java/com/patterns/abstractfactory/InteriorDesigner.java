package com.patterns.abstractfactory;

/**
 * Client — furnishes a room using whatever factory it receives.
 *
 * <p>The designer never imports {@code ScandinavianFactory} or
 * {@code IndustrialFactory}. It works entirely through the abstract
 * {@link FurnitureFactory} interface. Swap the factory and every piece
 * of furniture in the room changes style automatically.
 */
public class InteriorDesigner {

    private final FurnitureFactory factory;

    /**
     * @param factory the furniture factory to use for this room
     */
    public InteriorDesigner(FurnitureFactory factory) {
        this.factory = factory;
    }

    /**
     * Furnishes a room by creating a chair, table, and lamp from the factory,
     * then printing the result.
     */
    public void furnishRoom() {
        Chair chair = factory.createChair();
        Table table = factory.createTable();
        Lamp  lamp  = factory.createLamp();

        System.out.println("  Chair : " + chair.describe());
        System.out.println("  Table : " + table.describe());
        System.out.println("  Lamp  : " + lamp.describe());
    }
}
