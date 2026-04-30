package com.patterns.builder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Builder pattern — Custom PC Builder.
 */
class ComputerBuilderTest {

    @Test
    @DisplayName("Builder sets all fields correctly")
    void allFieldsSet() {
        Computer pc = new ComputerBuilder()
                .cpu("Test CPU")
                .ram(16)
                .storage(512)
                .gpu("Test GPU")
                .caseStyle("Test case")
                .wifi()
                .bluetooth()
                .build();

        assertEquals("Test CPU",   pc.getCpu());
        assertEquals(16,           pc.getRamGb());
        assertEquals(512,          pc.getStorageGb());
        assertEquals("Test GPU",   pc.getGpu());
        assertEquals("Test case",  pc.getCaseStyle());
        assertTrue(pc.hasWifi());
        assertTrue(pc.hasBluetooth());
    }

    @Test
    @DisplayName("GamingPCBuilder produces a high-RAM machine")
    void gamingBuilderHighRam() {
        Computer pc = new GamingPCBuilder().build();
        assertTrue(pc.getRamGb() >= 32, "Gaming PC should have at least 32 GB RAM");
        assertNotEquals("Integrated", pc.getGpu(), "Gaming PC should have a discrete GPU");
    }

    @Test
    @DisplayName("OfficePCBuilder produces an integrated-GPU machine")
    void officeBuilderIntegrated() {
        Computer pc = new OfficePCBuilder().build();
        assertTrue(pc.getGpu().toLowerCase().contains("integrated"));
    }

    @Test
    @DisplayName("ram(0) throws IllegalArgumentException")
    void zeroRamThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new ComputerBuilder().cpu("CPU").ram(0).build());
    }

    @Test
    @DisplayName("Each build() call returns a new independent Computer")
    void buildsAreIndependent() {
        ComputerBuilder builder = new ComputerBuilder().cpu("Shared CPU").ram(8);
        Computer a = builder.build();
        Computer b = builder.ram(16).build();
        assertNotSame(a, b);
        assertEquals(8,  a.getRamGb());
        assertEquals(16, b.getRamGb());
    }
}
