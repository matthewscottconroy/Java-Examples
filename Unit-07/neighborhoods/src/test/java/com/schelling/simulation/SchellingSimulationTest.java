package com.schelling.simulation;

import com.schelling.model.AgentType;
import com.schelling.model.Grid;
import com.schelling.model.SimulationConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SchellingSimulation}.
 *
 * <p>Tests are deterministic because a fixed random seed is used in the
 * configuration.  Where exact values depend on grid layout, tests verify
 * invariants (e.g. total agents preserved) rather than exact positions.
 */
@DisplayName("SchellingSimulation")
class SchellingSimulationTest {

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Returns a small, repeatable config suitable for unit testing. */
    private static SimulationConfig smallConfig() {
        return new SimulationConfig.Builder()
            .rows(10)
            .cols(10)
            .satisfactionThreshold(0.33)
            .emptyFraction(0.20)
            .typeBFraction(0.50)
            .randomSeed(1234L)
            .build();
    }

    /** Config with a very high threshold so nearly everyone is unsatisfied. */
    private static SimulationConfig highThresholdConfig() {
        return new SimulationConfig.Builder()
            .rows(10)
            .cols(10)
            .satisfactionThreshold(0.99)
            .emptyFraction(0.20)
            .typeBFraction(0.50)
            .randomSeed(42L)
            .build();
    }

    /** Config with threshold 0.0 so every agent is immediately satisfied. */
    private static SimulationConfig zeroThresholdConfig() {
        return new SimulationConfig.Builder()
            .rows(10)
            .cols(10)
            .satisfactionThreshold(0.0)
            .emptyFraction(0.20)
            .typeBFraction(0.50)
            .randomSeed(42L)
            .build();
    }

    // =========================================================================
    // Initialisation
    // =========================================================================

    @Nested
    @DisplayName("Initialisation")
    class Initialisation {

        @Test
        @DisplayName("step count starts at zero")
        void stepCountStartsZero() {
            SchellingSimulation sim = new SchellingSimulation(smallConfig());
            assertEquals(0, sim.getStepCount());
        }

        @Test
        @DisplayName("grid is populated with agents and empty cells")
        void gridPopulated() {
            SimulationConfig cfg = smallConfig();
            SchellingSimulation sim = new SchellingSimulation(cfg);
            Grid grid = sim.getGrid();

            int totalCells    = cfg.getRows() * cfg.getCols();
            int expectedEmpty = (int) Math.round(totalCells * cfg.getEmptyFraction());
            int actualEmpty   = grid.getEmptyCells().size();

            // Allow ±1 for rounding
            assertTrue(Math.abs(actualEmpty - expectedEmpty) <= 1,
                "Empty cell count should be close to configured fraction");
        }

        @Test
        @DisplayName("total agent count equals occupied cells")
        void agentCountConsistent() {
            SchellingSimulation sim = new SchellingSimulation(smallConfig());
            Grid grid  = sim.getGrid();
            int total  = grid.getTotalCells();
            int empty  = grid.getEmptyCells().size();
            int occupied = grid.getOccupiedCells().size();
            assertEquals(total, empty + occupied);
        }

        @Test
        @DisplayName("both agent types are present")
        void bothTypesPresent() {
            SchellingSimulation sim = new SchellingSimulation(smallConfig());
            Grid grid = sim.getGrid();
            long typeA = grid.getOccupiedCells().stream()
                .filter(p -> grid.getCell(p.x, p.y) == AgentType.TYPE_A)
                .count();
            long typeB = grid.getOccupiedCells().stream()
                .filter(p -> grid.getCell(p.x, p.y) == AgentType.TYPE_B)
                .count();
            assertTrue(typeA > 0, "Should have some TYPE_A agents");
            assertTrue(typeB > 0, "Should have some TYPE_B agents");
        }
    }

    // =========================================================================
    // Stepping
    // =========================================================================

    @Nested
    @DisplayName("Stepping")
    class Stepping {

        @Test
        @DisplayName("step increments the step counter")
        void stepIncrementsCounter() {
            SchellingSimulation sim = new SchellingSimulation(highThresholdConfig());
            sim.step();
            assertEquals(1, sim.getStepCount());
            sim.step();
            assertEquals(2, sim.getStepCount());
        }

        @Test
        @DisplayName("total number of agents is preserved after a step")
        void agentCountPreserved() {
            SchellingSimulation sim = new SchellingSimulation(smallConfig());
            int beforeOccupied = sim.getGrid().getOccupiedCells().size();
            sim.step();
            int afterOccupied  = sim.getGrid().getOccupiedCells().size();
            assertEquals(beforeOccupied, afterOccupied,
                "Agents are never created or destroyed");
        }

        @Test
        @DisplayName("agent type counts are preserved after a step")
        void typeCountsPreserved() {
            SchellingSimulation sim = new SchellingSimulation(smallConfig());
            Grid before = sim.getGrid().copy();

            long aCountBefore = countType(before, AgentType.TYPE_A);
            long bCountBefore = countType(before, AgentType.TYPE_B);

            sim.step();
            Grid after = sim.getGrid();

            assertEquals(aCountBefore, countType(after, AgentType.TYPE_A));
            assertEquals(bCountBefore, countType(after, AgentType.TYPE_B));
        }

        @Test
        @DisplayName("zero-threshold simulation is stable immediately")
        void zeroThresholdStableImmediately() {
            SchellingSimulation sim = new SchellingSimulation(zeroThresholdConfig());
            assertTrue(sim.isStable());
            int moves = sim.step();
            assertEquals(0, moves, "No moves expected when stable");
            assertEquals(0, sim.getStepCount(),
                "Step counter should not advance when already stable");
        }

        @Test
        @DisplayName("high-threshold simulation has unsatisfied agents initially")
        void highThresholdHasUnsatisfied() {
            SchellingSimulation sim = new SchellingSimulation(highThresholdConfig());
            assertFalse(sim.isStable(),
                "A threshold of 0.99 should leave many agents unsatisfied");
        }

        @Test
        @DisplayName("step returns a positive move count when agents are unsatisfied")
        void stepReturnsMoves() {
            SchellingSimulation sim = new SchellingSimulation(highThresholdConfig());
            int moves = sim.step();
            assertTrue(moves > 0);
        }
    }

    // =========================================================================
    // Stability
    // =========================================================================

    @Nested
    @DisplayName("Stability")
    class Stability {

        @Test
        @DisplayName("running to completion eventually reaches stability")
        void runsToStability() {
            SchellingSimulation sim = new SchellingSimulation(smallConfig());
            int maxSteps = 1000;
            int steps = 0;
            while (!sim.isStable() && steps < maxSteps) {
                sim.step();
                steps++;
            }
            assertTrue(sim.isStable(),
                "Simulation should converge within " + maxSteps + " steps");
        }

        @Test
        @DisplayName("satisfaction rate is 1.0 at stability")
        void satisfactionRateAtStability() {
            SchellingSimulation sim = new SchellingSimulation(zeroThresholdConfig());
            assertEquals(1.0, sim.getSatisfactionRate(), 1e-9);
        }
    }

    // =========================================================================
    // Reset
    // =========================================================================

    @Nested
    @DisplayName("Reset")
    class Reset {

        @Test
        @DisplayName("reset restores step count to zero")
        void resetRestoresStepCount() {
            SchellingSimulation sim = new SchellingSimulation(highThresholdConfig());
            sim.step();
            sim.step();
            sim.reset();
            assertEquals(0, sim.getStepCount());
        }

        @Test
        @DisplayName("reset with same seed produces identical initial grid")
        void resetProducesIdenticalGrid() {
            SimulationConfig cfg = smallConfig();
            SchellingSimulation sim = new SchellingSimulation(cfg);
            Grid before = sim.getGrid().copy();

            sim.step();
            sim.step();
            sim.reset();
            Grid after = sim.getGrid();

            for (int r = 0; r < cfg.getRows(); r++)
                for (int c = 0; c < cfg.getCols(); c++)
                    assertEquals(before.getCell(r, c), after.getCell(r, c),
                        String.format("Cell (%d,%d) differs after reset", r, c));
        }
    }

    // =========================================================================
    // Null guard
    // =========================================================================

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        assertThrows(NullPointerException.class,
            () -> new SchellingSimulation(null));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static long countType(Grid grid, AgentType type) {
        return grid.getOccupiedCells().stream()
            .filter(p -> grid.getCell(p.x, p.y) == type)
            .count();
    }
}
