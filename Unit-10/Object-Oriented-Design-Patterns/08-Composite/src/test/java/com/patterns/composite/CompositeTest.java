package com.patterns.composite;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Composite pattern — Company Org Chart.
 */
class CompositeTest {

    @Test
    @DisplayName("Staff salary equals its own salary")
    void leafSalary() {
        Staff s = new Staff("Alice", "Engineer", 100_000);
        assertEquals(100_000, s.getTotalSalary());
    }

    @Test
    @DisplayName("Team salary is the sum of its members")
    void teamSalaryIsSum() {
        Team team = new Team("Dev")
                .add(new Staff("A", "E", 80_000))
                .add(new Staff("B", "E", 90_000));
        assertEquals(170_000, team.getTotalSalary());
    }

    @Test
    @DisplayName("Nested teams aggregate recursively")
    void nestedTeamSalary() {
        Team inner = new Team("Inner")
                .add(new Staff("X", "Dev", 50_000));
        Team outer = new Team("Outer")
                .add(inner)
                .add(new Staff("Y", "PM", 60_000));
        assertEquals(110_000, outer.getTotalSalary());
    }

    @Test
    @DisplayName("applyRaise increases salary by the correct percentage")
    void raiseOnLeaf() {
        Staff s = new Staff("Bob", "Dev", 100_000);
        s.applyRaise(10);
        assertEquals(110_000, s.getTotalSalary());
    }

    @Test
    @DisplayName("applyRaise on a team propagates to all members")
    void raiseOnTeam() {
        Staff alice = new Staff("Alice", "Dev", 100_000);
        Staff bob   = new Staff("Bob",   "Dev", 100_000);
        Team  team  = new Team("Dev").add(alice).add(bob);

        team.applyRaise(10);

        assertEquals(110_000, alice.getTotalSalary());
        assertEquals(110_000, bob.getTotalSalary());
        assertEquals(220_000, team.getTotalSalary());
    }

    @Test
    @DisplayName("Client code uses Employee interface for both leaf and composite")
    void uniformInterface() {
        Employee leaf      = new Staff("Alice", "Dev", 100_000);
        Employee composite = new Team("Team").add(leaf);

        // Same method call on both — no instanceof checks
        assertDoesNotThrow(() -> {
            leaf.printStructure(0);
            composite.printStructure(0);
        });
    }
}
