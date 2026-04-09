package com.combustionengine.model;

/**
 * <h2>Engine Configuration</h2>
 * Immutable record holding the geometric and thermodynamic parameters that
 * define an engine's character.
 *
 * @param boreM            cylinder bore (inner diameter) in metres
 * @param strokeM          piston stroke (TDC-to-BDC distance) in metres
 * @param rodLengthM       connecting-rod centre-to-centre length in metres
 * @param compressionRatio volumetric compression ratio V_max / V_min  (≥ 2)
 * @param numCylinders     number of cylinders (1, 2, or 4)
 * @param fuelLHV          lower heating value of the fuel in J/kg
 * @param airFuelRatio     stoichiometric air-to-fuel mass ratio
 * @param inertiaKgM2      effective rotational inertia of crank + flywheel (kg·m²)
 */
public record EngineConfig(
        double boreM,
        double strokeM,
        double rodLengthM,
        double compressionRatio,
        int    numCylinders,
        double fuelLHV,
        double airFuelRatio,
        double inertiaKgM2) {

    /** Crank throw (half-stroke) in metres. */
    public double crankRadiusM() { return strokeM / 2.0; }

    /** Bore cross-sectional area in m². */
    public double boreAreaM2() { return Math.PI * boreM * boreM / 4.0; }

    /**
     * Clearance volume (volume at TDC) per cylinder in m³.
     * <pre>V_c = A · L / (r_c − 1)</pre>
     */
    public double clearanceVolumeM3() {
        return boreAreaM2() * strokeM / (compressionRatio - 1.0);
    }

    /** Maximum cylinder volume (at BDC) in m³. */
    public double maxVolumeM3() { return clearanceVolumeM3() * compressionRatio; }

    /** Displacement volume per cylinder in m³. */
    public double displacementM3() { return boreAreaM2() * strokeM; }

    /** Total engine displacement in cm³ (cc). */
    public double totalDisplacementCC() {
        return displacementM3() * numCylinders * 1_000_000.0;
    }
}
