package com.combustionengine.model;

/** Named factory presets for common engine configurations. */
public enum EnginePreset {

    /** Everyday 4-cylinder economy petrol engine (~1.6 L). */
    ECONOMY_4("Economy Inline-4") {
        @Override public EngineConfig config() {
            return new EngineConfig(0.078, 0.085, 0.140, 9.5, 4, 44.0e6, 14.7, 0.30);
        }
    },

    /** High-performance 4-cylinder sport engine (~2.0 L). */
    SPORT_4("Sport Inline-4") {
        @Override public EngineConfig config() {
            return new EngineConfig(0.086, 0.086, 0.145, 12.5, 4, 44.0e6, 14.7, 0.22);
        }
    },

    /** Compact parallel-twin motorcycle engine (~650 cc). */
    TWIN("Parallel Twin") {
        @Override public EngineConfig config() {
            return new EngineConfig(0.072, 0.080, 0.130, 11.0, 2, 44.0e6, 14.7, 0.18);
        }
    },

    /** Single-cylinder utility engine (~400 cc). */
    SINGLE("Single Cylinder") {
        @Override public EngineConfig config() {
            return new EngineConfig(0.076, 0.086, 0.140, 8.5, 1, 44.0e6, 14.7, 0.20);
        }
    };

    private final String label;

    EnginePreset(String label) { this.label = label; }

    /** Returns the {@link EngineConfig} for this preset. */
    public abstract EngineConfig config();

    @Override public String toString() { return label; }
}
