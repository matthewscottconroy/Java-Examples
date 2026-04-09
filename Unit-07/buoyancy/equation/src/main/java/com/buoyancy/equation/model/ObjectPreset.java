package com.buoyancy.equation.model;

/**
 * Common material density presets for {@link BuoyancyObject}.
 *
 * <p>Densities are in kg/m³ and represent typical room-temperature values.
 */
public enum ObjectPreset {
    BALSA_WOOD ("Balsa Wood",     170.0),
    CORK       ("Cork",           240.0),
    PINE       ("Pine Wood",      530.0),
    OAK        ("Oak Wood",       700.0),
    ICE        ("Ice",            917.0),
    WATER      ("Water",         1000.0),
    SEAWATER   ("Sea Water",     1025.0),
    CONCRETE   ("Concrete",      2400.0),
    ALUMINIUM  ("Aluminium",     2700.0),
    STEEL      ("Steel",         7850.0),
    LEAD       ("Lead",         11340.0),
    MERCURY    ("Mercury",      13534.0);

    private final String  label;
    private final double  densityKgM3;

    ObjectPreset(String label, double densityKgM3) {
        this.label       = label;
        this.densityKgM3 = densityKgM3;
    }

    public String getLabel()           { return label; }
    public double getDensityKgM3()     { return densityKgM3; }

    @Override
    public String toString() { return label; }
}
