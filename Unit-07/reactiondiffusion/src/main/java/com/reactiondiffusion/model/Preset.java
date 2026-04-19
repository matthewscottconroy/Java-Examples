package com.reactiondiffusion.model;

import java.util.List;

/**
 * An immutable parameter preset for the Gray-Scott reaction-diffusion model.
 *
 * <p>Each preset encodes a (F, k) pair that produces a characteristic
 * Turing-pattern morphology.  The feed rate {@code F} controls how quickly
 * chemical A is replenished from the external reservoir; the kill rate
 * {@code k} determines how quickly chemical B is removed.
 *
 * <h2>Presets</h2>
 * <ul>
 *   <li><b>Spots</b>    — F=0.035, k=0.065</li>
 *   <li><b>Stripes</b>  — F=0.060, k=0.062</li>
 *   <li><b>Worms</b>    — F=0.046, k=0.063</li>
 *   <li><b>Mitosis</b>  — F=0.028, k=0.053</li>
 *   <li><b>Spirals</b>  — F=0.018, k=0.055</li>
 *   <li><b>Coral</b>    — F=0.062, k=0.061</li>
 * </ul>
 *
 * @param name  human-readable preset label
 * @param F     feed rate (dimensionless, typically 0.01–0.08)
 * @param k     kill rate (dimensionless, typically 0.04–0.075)
 */
public record Preset(String name, double F, double k) {

    /**
     * All built-in parameter presets, ordered for display in the UI combo-box.
     *
     * <p>The list is unmodifiable; each entry is an immutable {@link Preset} record.
     */
    public static final List<Preset> PRESETS = List.of(
        new Preset("Spots",   0.035, 0.065),
        new Preset("Stripes", 0.060, 0.062),
        new Preset("Worms",   0.046, 0.063),
        new Preset("Mitosis", 0.028, 0.053),
        new Preset("Spirals", 0.018, 0.055),
        new Preset("Coral",   0.062, 0.061)
    );

    /**
     * Returns the preset name for display (e.g. in a {@link javax.swing.JComboBox}).
     *
     * @return the name string passed at construction
     */
    @Override
    public String toString() {
        return name;
    }
}
