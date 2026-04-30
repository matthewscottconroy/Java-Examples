package com.patterns.abstractfactory;

/**
 * Concrete Factory — produces a matching set of Industrial-style furniture.
 *
 * <p>Industrial design: raw steel, reclaimed wood, exposed hardware, dark
 * palette, and a deliberately unfinished look that suggests a converted
 * warehouse or workshop.
 */
public class IndustrialFactory implements FurnitureFactory {

    @Override
    public Chair createChair() {
        return new Chair() {
            public String getMaterial() { return "steel and reclaimed wood"; }
            public String describe()    {
                return "Industrial chair: welded steel frame, reclaimed-wood seat, riveted backrest";
            }
        };
    }

    @Override
    public Table createTable() {
        return new Table() {
            public String getMaterial() { return "cast iron and reclaimed wood"; }
            public String describe()    {
                return "Industrial table: cast-iron pipe legs, wide reclaimed-oak top, raw bolt heads";
            }
        };
    }

    @Override
    public Lamp createLamp() {
        return new Lamp() {
            public String getMaterial() { return "brushed steel and cage wire"; }
            public String describe()    {
                return "Industrial lamp: brushed-steel pipe arm, wire-cage shade, Edison filament bulb";
            }
        };
    }
}
