package com.boids.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Boid}.
 */
@DisplayName("Boid")
class BoidTest {

    private static final double MAX_FORCE = 500.0;
    private static final double MAX_SPEED = 100.0;
    private static final double DT        = 0.016;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("stores initial position and velocity")
        void initialState() {
            Boid b = new Boid(10.0, 20.0, 3.0, 4.0);
            assertEquals(10.0, b.getX(),  1e-12);
            assertEquals(20.0, b.getY(),  1e-12);
            assertEquals(3.0,  b.getVx(), 1e-12);
            assertEquals(4.0,  b.getVy(), 1e-12);
        }
    }

    // -------------------------------------------------------------------------
    // applyForce — position update
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("applyForce")
    class ApplyForce {

        @Test
        @DisplayName("position advances in the direction of velocity")
        void positionUpdates() {
            // Start stationary; apply a rightward force
            Boid b = new Boid(100.0, 100.0, 0.0, 0.0);
            b.applyForce(MAX_FORCE, 0.0, MAX_FORCE, MAX_SPEED, DT);
            // velocity = maxForce * dt → position increases in x
            assertTrue(b.getX() > 100.0, "x should increase after rightward force");
            assertEquals(100.0, b.getY(), 1e-6, "y should be unchanged");
        }

        @Test
        @DisplayName("velocity is clamped to maxSpeed")
        void velocityClampedToMaxSpeed() {
            // Start with very high velocity
            Boid b = new Boid(0.0, 0.0, 10_000.0, 0.0);
            b.applyForce(0.0, 0.0, MAX_FORCE, MAX_SPEED, DT);
            assertEquals(MAX_SPEED, b.speed(), 1e-9,
                    "speed must not exceed maxSpeed after clamping");
        }

        @Test
        @DisplayName("force is clamped to maxForce before being applied")
        void forceClampedToMaxForce() {
            // Apply a force 100× larger than maxForce
            Boid b1 = new Boid(0.0, 0.0, 0.0, 0.0);
            Boid b2 = new Boid(0.0, 0.0, 0.0, 0.0);
            b1.applyForce(MAX_FORCE * 100, 0.0, MAX_FORCE, MAX_SPEED, DT);
            b2.applyForce(MAX_FORCE,        0.0, MAX_FORCE, MAX_SPEED, DT);
            // Both should produce the same velocity because force is clamped
            assertEquals(b2.getVx(), b1.getVx(), 1e-9,
                    "excess force magnitude must be clamped to maxForce");
        }

        @Test
        @DisplayName("zero force leaves velocity unchanged and updates position by v*dt")
        void zeroForceKeepsVelocity() {
            Boid b = new Boid(50.0, 50.0, 10.0, 5.0);
            b.applyForce(0.0, 0.0, MAX_FORCE, MAX_SPEED, DT);
            assertEquals(10.0, b.getVx(), 1e-9, "vx must not change with zero force");
            assertEquals(5.0,  b.getVy(), 1e-9, "vy must not change with zero force");
            assertEquals(50.0 + 10.0 * DT, b.getX(), 1e-9);
            assertEquals(50.0 +  5.0 * DT, b.getY(), 1e-9);
        }
    }

    // -------------------------------------------------------------------------
    // wrap — toroidal boundary
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("wrap")
    class Wrap {

        private static final double W = 700.0;
        private static final double H = 500.0;

        @Test
        @DisplayName("x > W wraps to near 0")
        void wrapRightEdge() {
            Boid b = new Boid(W + 10.0, 250.0, 0.0, 0.0);
            b.wrap(W, H);
            assertTrue(b.getX() >= 0 && b.getX() < W,
                    "x must be inside [0, W) after wrap");
            assertEquals(10.0, b.getX(), 1e-9);
        }

        @Test
        @DisplayName("x < 0 wraps to near W")
        void wrapLeftEdge() {
            Boid b = new Boid(-10.0, 250.0, 0.0, 0.0);
            b.wrap(W, H);
            assertEquals(W - 10.0, b.getX(), 1e-9);
        }

        @Test
        @DisplayName("y > H wraps to near 0")
        void wrapBottomEdge() {
            Boid b = new Boid(350.0, H + 5.0, 0.0, 0.0);
            b.wrap(W, H);
            assertEquals(5.0, b.getY(), 1e-9);
        }

        @Test
        @DisplayName("y < 0 wraps to near H")
        void wrapTopEdge() {
            Boid b = new Boid(350.0, -20.0, 0.0, 0.0);
            b.wrap(W, H);
            assertEquals(H - 20.0, b.getY(), 1e-9);
        }

        @Test
        @DisplayName("position already inside bounds is unchanged")
        void noWrapInsideBounds() {
            Boid b = new Boid(350.0, 250.0, 0.0, 0.0);
            b.wrap(W, H);
            assertEquals(350.0, b.getX(), 1e-9);
            assertEquals(250.0, b.getY(), 1e-9);
        }
    }

    // -------------------------------------------------------------------------
    // heading
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("heading")
    class Heading {

        @Test
        @DisplayName("rightward velocity → heading near 0")
        void headingRight() {
            Boid b = new Boid(0, 0, 10.0, 0.0);
            assertEquals(0.0, b.heading(), 1e-9);
        }

        @Test
        @DisplayName("downward velocity → heading near π/2")
        void headingDown() {
            Boid b = new Boid(0, 0, 0.0, 10.0);
            assertEquals(Math.PI / 2, b.heading(), 1e-9);
        }

        @Test
        @DisplayName("heading lies in (-π, π]")
        void headingInRange() {
            Boid b = new Boid(0, 0, -5.0, -5.0);
            double h = b.heading();
            assertTrue(h >= -Math.PI && h <= Math.PI,
                    "heading must lie in (-π, π]");
        }

        @Test
        @DisplayName("heading is atan2(vy, vx)")
        void headingMatchesAtan2() {
            Boid b = new Boid(0, 0, 3.0, 4.0);
            assertEquals(Math.atan2(4.0, 3.0), b.heading(), 1e-12);
        }
    }

    // -------------------------------------------------------------------------
    // speed
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("speed")
    class Speed {

        @Test
        @DisplayName("speed is the Euclidean magnitude of velocity")
        void speedMagnitude() {
            Boid b = new Boid(0, 0, 3.0, 4.0);
            assertEquals(5.0, b.speed(), 1e-9);
        }

        @Test
        @DisplayName("speed is non-negative for zero velocity")
        void speedNonNegative() {
            Boid b = new Boid(0, 0, 0.0, 0.0);
            assertEquals(0.0, b.speed(), 1e-12);
        }
    }
}
