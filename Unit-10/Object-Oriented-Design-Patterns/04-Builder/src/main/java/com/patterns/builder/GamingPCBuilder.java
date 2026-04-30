package com.patterns.builder;

/**
 * Concrete Builder — pre-configures high-end gaming components as defaults.
 *
 * <p>A caller can use this builder as-is for a solid gaming rig, or override
 * individual components with further fluent calls before {@link #build()}.
 */
public class GamingPCBuilder extends ComputerBuilder {

    /**
     * Initialises a gaming build: high-end CPU, 32 GB RAM, 2 TB NVMe SSD,
     * top-tier GPU, full-tower tempered-glass case, WiFi, and Bluetooth.
     */
    public GamingPCBuilder() {
        cpu("Intel Core i9-14900K")
            .ram(32)
            .storage(2000)
            .gpu("NVIDIA GeForce RTX 4090")
            .caseStyle("Full-tower tempered glass with RGB")
            .wifi()
            .bluetooth();
    }
}
