package com.patterns.facade;

/** Subsystem component — the smart lighting system. */
public class SmartLights {
    private int brightness = 100;

    /**
     * Dims the lights to the specified percentage.
     * @param percent 0 = off, 100 = full brightness
     */
    public void dim(int percent) {
        brightness = Math.max(0, Math.min(100, percent));
        System.out.println("  Lights: dimmed to " + brightness + "%");
    }

    /** Brings lights to full brightness. */
    public void fullBrightness() { dim(100); }
}
