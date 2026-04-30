package com.patterns.abstractfactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Abstract Factory pattern — Furniture Store.
 */
class FurnitureFactoryTest {

    @Test
    @DisplayName("ScandinavianFactory products mention wood materials")
    void scandinavianUsesWood() {
        FurnitureFactory factory = new ScandinavianFactory();
        assertTrue(factory.createChair().getMaterial().contains("wood")
                || factory.createChair().getMaterial().contains("birch"));
        assertTrue(factory.createTable().getMaterial().contains("wood")
                || factory.createTable().getMaterial().contains("pine"));
    }

    @Test
    @DisplayName("IndustrialFactory products mention metal materials")
    void industrialUsesMetal() {
        FurnitureFactory factory = new IndustrialFactory();
        assertTrue(factory.createChair().getMaterial().contains("steel")
                || factory.createChair().getMaterial().contains("iron"));
    }

    @Test
    @DisplayName("Scandinavian and Industrial chairs are distinct")
    void chairsAreDifferent() {
        Chair scandi    = new ScandinavianFactory().createChair();
        Chair industrial = new IndustrialFactory().createChair();
        assertNotEquals(scandi.describe(), industrial.describe());
    }

    @Test
    @DisplayName("All products return non-blank descriptions")
    void descriptionsNotBlank() {
        for (FurnitureFactory f : new FurnitureFactory[]{
                new ScandinavianFactory(), new IndustrialFactory()}) {
            assertFalse(f.createChair().describe().isBlank());
            assertFalse(f.createTable().describe().isBlank());
            assertFalse(f.createLamp().describe().isBlank());
        }
    }

    @Test
    @DisplayName("InteriorDesigner works with any factory without changes")
    void designerIsDecoupled() {
        // If this compiles and runs, the designer is truly decoupled
        assertDoesNotThrow(() -> {
            new InteriorDesigner(new ScandinavianFactory()).furnishRoom();
            new InteriorDesigner(new IndustrialFactory()).furnishRoom();
        });
    }
}
