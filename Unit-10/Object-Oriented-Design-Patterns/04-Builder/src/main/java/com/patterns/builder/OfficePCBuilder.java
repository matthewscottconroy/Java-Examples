package com.patterns.builder;

/**
 * Concrete Builder — pre-configures budget office components as defaults.
 *
 * <p>An office PC handles documents, email, and video calls — no need for a
 * discrete GPU or flashy case.
 */
public class OfficePCBuilder extends ComputerBuilder {

    /**
     * Initialises an office build: mid-range CPU, 16 GB RAM, 512 GB SSD,
     * integrated graphics, a compact quiet case, and WiFi.
     */
    public OfficePCBuilder() {
        cpu("Intel Core i5-13500")
            .ram(16)
            .storage(512)
            .gpu("Integrated Intel UHD 770")
            .caseStyle("Compact quiet mini-tower")
            .wifi();
    }
}
