package com.patterns.abstractfactory;

/**
 * Concrete Factory — produces a matching set of Scandinavian-style furniture.
 *
 * <p>Scandinavian design: light birch or pine wood, clean lines, minimal
 * ornamentation, functional but warm. Every product from this factory belongs
 * to the same aesthetic family.
 */
public class ScandinavianFactory implements FurnitureFactory {

    @Override
    public Chair createChair() {
        return new Chair() {
            public String getMaterial() { return "birch wood"; }
            public String describe()    {
                return "Scandinavian chair: slender birch legs, natural linen cushion, clean lines";
            }
        };
    }

    @Override
    public Table createTable() {
        return new Table() {
            public String getMaterial() { return "pine wood"; }
            public String describe()    {
                return "Scandinavian table: solid pine top, tapered wooden legs, light finish";
            }
        };
    }

    @Override
    public Lamp createLamp() {
        return new Lamp() {
            public String getMaterial() { return "beech wood and linen"; }
            public String describe()    {
                return "Scandinavian lamp: beech-wood base, conical linen shade, warm Edison bulb";
            }
        };
    }
}
