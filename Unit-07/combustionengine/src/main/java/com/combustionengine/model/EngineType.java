package com.combustionengine.model;

/**
 * Combustion cycle type — determines the thermodynamic model used by the physics engine.
 *
 * <h3>Otto cycle (spark-ignition)</h3>
 * Heat is added at <em>constant volume</em> (isochoric) after the spark fires at TDC.
 * Typical compression ratios: 8–13 : 1.
 *
 * <h3>Diesel cycle (compression-ignition)</h3>
 * Air alone is compressed to a high enough temperature to auto-ignite injected fuel.
 * Heat is added at <em>constant pressure</em> (isobaric) as the piston begins its
 * power stroke.  Typical compression ratios: 14–22 : 1.
 */
public enum EngineType {
    /** Spark-ignition — isochoric (constant-volume) heat addition. */
    OTTO,
    /** Compression-ignition — isobaric (constant-pressure) heat addition. */
    DIESEL
}
