package com.combustionengine.model;

/** Named factory presets for common engine configurations. */
public enum EnginePreset {

    /** Everyday 4-cylinder economy petrol engine (~1.6 L). */
    ECONOMY_4("Economy Inline-4") {
        @Override public EngineConfig config() {
            return new EngineConfig(0.078, 0.085, 0.140, 9.5, 4, 44.0e6, 14.7, 0.30, EngineType.OTTO);
        }
    },

    /** High-performance 4-cylinder sport engine (~2.0 L). */
    SPORT_4("Sport Inline-4") {
        @Override public EngineConfig config() {
            return new EngineConfig(0.086, 0.086, 0.145, 12.5, 4, 44.0e6, 14.7, 0.22, EngineType.OTTO);
        }
    },

    /** Compact parallel-twin motorcycle engine (~650 cc). */
    TWIN("Parallel Twin") {
        @Override public EngineConfig config() {
            return new EngineConfig(0.072, 0.080, 0.130, 11.0, 2, 44.0e6, 14.7, 0.18, EngineType.OTTO);
        }
    },

    /** Single-cylinder utility engine (~400 cc). */
    SINGLE("Single Cylinder") {
        @Override public EngineConfig config() {
            return new EngineConfig(0.076, 0.086, 0.140, 8.5, 1, 44.0e6, 14.7, 0.20, EngineType.OTTO);
        }
    },

    /**
     * Passenger-car compression-ignition diesel (~2.0 L).
     * High compression ratio (18 : 1) raises air temperature enough to
     * auto-ignite injected diesel fuel.  Fuel burns at nearly constant
     * pressure as the piston begins to descend (Diesel cycle).
     * Diesel LHV ≈ 42.5 MJ/kg; effective A/F ~22 represents part-load operation.
     */
    DIESEL_4("Diesel Inline-4") {
        @Override public EngineConfig config() {
            return new EngineConfig(0.081, 0.096, 0.155, 18.0, 4, 42.5e6, 22.0, 0.32, EngineType.DIESEL);
        }
    },

    /**
     * Heavy-duty compression-ignition diesel inline-6 (~3.2 L).
     * Very high compression ratio (20 : 1) and large displacement give high
     * torque at low RPM, typical of truck and industrial engines.
     * Note: the cylinder-status panel displays only the first four cylinders.
     */
    DIESEL_6("Diesel Inline-6") {
        @Override public EngineConfig config() {
            return new EngineConfig(0.092, 0.100, 0.170, 20.0, 6, 42.5e6, 24.0, 0.50, EngineType.DIESEL);
        }
    };

    private final String label;

    EnginePreset(String label) { this.label = label; }

    /** Returns the {@link EngineConfig} for this preset. */
    public abstract EngineConfig config();

    @Override public String toString() { return label; }
}
