package com.patterns.builder;

/**
 * Demonstrates the Builder pattern with custom PC configurations.
 *
 * <p>Three different builds are assembled:
 * <ol>
 *   <li>A gaming rig using the pre-configured {@link GamingPCBuilder}</li>
 *   <li>An office machine using {@link OfficePCBuilder}</li>
 *   <li>A custom workstation built step by step from a plain {@link ComputerBuilder}</li>
 * </ol>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== The Custom PC Builder (Builder Pattern) ===\n");

        // Pre-configured gaming rig
        Computer gamingPC = new GamingPCBuilder().build();
        System.out.println("Gaming rig:   " + gamingPC);

        // Pre-configured office machine
        Computer officePC = new OfficePCBuilder().build();
        System.out.println("Office PC:    " + officePC);

        // Custom workstation — start from plain builder, pick only what's needed
        Computer workstation = new ComputerBuilder()
                .cpu("AMD Ryzen Threadripper PRO 5965WX")
                .ram(128)
                .storage(4000)
                .gpu("NVIDIA RTX A6000")
                .caseStyle("Workstation tower, no RGB")
                .wifi()
                .build();
        System.out.println("Workstation:  " + workstation);

        // Upgrade the default gaming rig to 64 GB — override just one field
        Computer upgradedGaming = new GamingPCBuilder().ram(64).build();
        System.out.println("64GB gaming:  " + upgradedGaming);
    }
}
