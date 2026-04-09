package com.schelling.model;

/**
 * Initial spatial arrangement of agents on the grid.
 *
 * <ul>
 *   <li>{@link #RANDOM} — uniform random distribution (classic Schelling setup)</li>
 *   <li>{@link #SEGREGATED} — Group A on the left half, Group B on the right</li>
 *   <li>{@link #CHECKERBOARD} — strict alternating A/B pattern</li>
 *   <li>{@link #ENCLAVE} — Group B clustered in the centre, Group A surrounding</li>
 *   <li>{@link #CLUSTERS} — Voronoi-seeded macro-clusters of each type</li>
 * </ul>
 */
public enum InitialCondition {
    RANDOM("Random"),
    SEGREGATED("Segregated"),
    CHECKERBOARD("Checkerboard"),
    ENCLAVE("Enclave"),
    CLUSTERS("Clusters");

    private final String displayName;

    InitialCondition(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
