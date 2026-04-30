package com.patterns.mediator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Mediator pattern — Air Traffic Control Tower.
 */
class MediatorTest {

    @Test
    @DisplayName("First aircraft gets takeoff clearance on a free runway")
    void firstTakeoffGranted() {
        ControlTower tower = new ControlTower();
        Aircraft a = new Aircraft("TEST-1", tower);
        a.requestTakeoff();
        assertEquals("AIRBORNE", a.getStatus());
    }

    @Test
    @DisplayName("Second aircraft denied takeoff while runway occupied")
    void secondTakeoffDenied() {
        ControlTower tower = new ControlTower();
        Aircraft a = new Aircraft("TEST-1", tower);
        Aircraft b = new Aircraft("TEST-2", tower);
        a.requestTakeoff(); // occupies runway and then departs
        // After departure notifyComplete is called, runway is free
        // So let's test simultaneous: tower with manual occupation
        // We test with a tower that stays occupied:
        ControlTower busyTower = new ControlTower() {
            { requestClearance(new Aircraft("BUSY", this), "TAKEOFF"); } // occupy runway
        };
        Aircraft c = new Aircraft("TEST-3", busyTower);
        c.requestTakeoff();
        assertEquals("HOLDING", c.getStatus());
    }

    @Test
    @DisplayName("Landing granted when runway is free")
    void landingGranted() {
        ControlTower tower = new ControlTower();
        Aircraft a = new Aircraft("TEST-4", tower);
        a.requestLanding();
        assertEquals("LANDED", a.getStatus());
    }

    @Test
    @DisplayName("notifyComplete(DEPARTED) frees the runway")
    void notifyCompleteFreesRunway() {
        ControlTower tower = new ControlTower();
        Aircraft a = new Aircraft("TEST-5", tower);
        tower.requestClearance(a, "TAKEOFF"); // occupies runway
        assertTrue(tower.isRunwayOccupied());
        tower.notifyComplete(a, "DEPARTED");
        assertFalse(tower.isRunwayOccupied());
    }

    @Test
    @DisplayName("Traffic log records all interactions")
    void trafficLogGrows() {
        ControlTower tower = new ControlTower();
        Aircraft a = new Aircraft("LOG-1", tower);
        a.requestTakeoff();
        assertFalse(tower.getTrafficLog().isEmpty());
    }
}
