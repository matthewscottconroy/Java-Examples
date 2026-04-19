package com.boids.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FlockSimulation}.
 */
@DisplayName("FlockSimulation")
class FlockSimulationTest {

    private static final double W = 700.0;
    private static final double H = 500.0;

    // -------------------------------------------------------------------------
    // Reset / boid count
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("reset")
    class Reset {

        @Test
        @DisplayName("reset creates the requested number of boids")
        void resetCreatesCorrectCount() {
            FlockSimulation sim = new FlockSimulation(W, H);
            sim.reset(50);
            assertEquals(50, sim.getBoids().size());
        }

        @Test
        @DisplayName("reset with 0 boids yields an empty flock")
        void resetZeroBoids() {
            FlockSimulation sim = new FlockSimulation(W, H);
            sim.reset(0);
            assertEquals(0, sim.getBoids().size());
        }

        @Test
        @DisplayName("reset replaces existing boids")
        void resetReplacesExisting() {
            FlockSimulation sim = new FlockSimulation(W, H);
            sim.reset(100);
            sim.reset(20);
            assertEquals(20, sim.getBoids().size());
        }
    }

    // -------------------------------------------------------------------------
    // Wrapping — boids stay within bounds
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("wrapping")
    class Wrapping {

        @Test
        @DisplayName("all boids remain within world bounds after many steps")
        void boidsStayInBounds() {
            FlockSimulation sim = new FlockSimulation(W, H);
            sim.reset(50);
            double dt = 0.016;
            for (int i = 0; i < 500; i++) {
                sim.step(dt);
            }
            for (Boid b : sim.getBoids()) {
                assertTrue(b.getX() >= 0 && b.getX() <= W,
                        "x=" + b.getX() + " must be within [0, " + W + "]");
                assertTrue(b.getY() >= 0 && b.getY() <= H,
                        "y=" + b.getY() + " must be within [0, " + H + "]");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Step — robustness
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("step")
    class Step {

        @Test
        @DisplayName("step with 0 boids completes without exception")
        void stepZeroBoids() {
            FlockSimulation sim = new FlockSimulation(W, H);
            sim.reset(0);
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 10; i++) sim.step(0.016);
            });
        }

        @Test
        @DisplayName("step with 100 boids completes without exception")
        void step100Boids() {
            FlockSimulation sim = new FlockSimulation(W, H);
            sim.reset(100);
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 60; i++) sim.step(0.016);
            });
        }

        @Test
        @DisplayName("boid count does not change after stepping")
        void stepPreservesCount() {
            FlockSimulation sim = new FlockSimulation(W, H);
            sim.reset(80);
            for (int i = 0; i < 30; i++) sim.step(0.016);
            assertEquals(80, sim.getBoids().size());
        }
    }

    // -------------------------------------------------------------------------
    // Separation — boids move apart
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("separation")
    class Separation {

        @Test
        @DisplayName("separation force moves a boid away from a nearby neighbour")
        void separationMovesBoidAway() {
            // Build a simulation with exactly two boids that are very close,
            // strong separation, and no alignment/cohesion.
            FlockSimulation sim = new FlockSimulation(W, H);
            sim.reset(0);
            sim.setSeparationWeight(5.0);
            sim.setAlignmentWeight(0.0);
            sim.setCohesionWeight(0.0);
            sim.setPerceptionRadius(200.0);
            sim.setSeparationRadius(50.0);
            sim.setMaxSpeed(500.0);
            sim.setMaxForce(1000.0);

            // Place two boids 10 px apart horizontally, both nearly stationary
            sim.addBoid(345.0, 250.0);
            sim.addBoid(355.0, 250.0);

            Boid left  = sim.getBoids().get(0);
            Boid right = sim.getBoids().get(1);

            double initialDist = Math.abs(right.getX() - left.getX());

            // Step the simulation a number of times to let separation act
            for (int i = 0; i < 20; i++) sim.step(0.016);

            // The boids should be further apart now
            // Use the shortest toroidal distance
            double dx   = right.getX() - left.getX();
            if (dx > W / 2)  dx -= W;
            if (dx < -W / 2) dx += W;
            double finalDist = Math.abs(dx);

            assertTrue(finalDist > initialDist,
                    "boids should have separated; initial=" + initialDist
                    + ", final=" + finalDist);
        }
    }

    // -------------------------------------------------------------------------
    // Predator
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("predator")
    class PredatorTests {

        @Test
        @DisplayName("getPredator returns null when no predator is set")
        void noPredatorByDefault() {
            FlockSimulation sim = new FlockSimulation(W, H);
            assertNull(sim.getPredator());
        }

        @Test
        @DisplayName("setPredator stores the position; clearPredator removes it")
        void setPredatorAndClear() {
            FlockSimulation sim = new FlockSimulation(W, H);
            sim.setPredator(300.0, 200.0);
            assertNotNull(sim.getPredator());
            assertEquals(300.0, sim.getPredator()[0], 1e-9);
            assertEquals(200.0, sim.getPredator()[1], 1e-9);

            sim.clearPredator();
            assertNull(sim.getPredator());
        }

        @Test
        @DisplayName("step with predator set completes without exception")
        void stepWithPredator() {
            FlockSimulation sim = new FlockSimulation(W, H);
            sim.reset(50);
            sim.setPredator(350.0, 250.0);
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 30; i++) sim.step(0.016);
            });
        }
    }

    // -------------------------------------------------------------------------
    // addBoid
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("addBoid")
    class AddBoid {

        @Test
        @DisplayName("addBoid increases the flock size by one")
        void addBoidIncrementsCount() {
            FlockSimulation sim = new FlockSimulation(W, H);
            sim.reset(10);
            sim.addBoid(200.0, 200.0);
            assertEquals(11, sim.getBoids().size());
        }

        @Test
        @DisplayName("added boid appears at the given position")
        void addBoidPosition() {
            FlockSimulation sim = new FlockSimulation(W, H);
            sim.reset(0);
            sim.addBoid(123.0, 456.0);
            Boid b = sim.getBoids().get(0);
            assertEquals(123.0, b.getX(), 1e-9);
            assertEquals(456.0, b.getY(), 1e-9);
        }
    }
}
