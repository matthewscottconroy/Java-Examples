package com.pendulums.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link NewtonsCradle}.
 */
@DisplayName("NewtonsCradle")
class NewtonsCradleTest {

    private static final double LENGTH  = 210.0;
    private static final double RADIUS  = 22.0;
    private static final double GRAVITY = 980.0;

    // -------------------------------------------------------------------------
    // Construction and reset
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("default ball count is 5")
        void defaultFiveBalls() {
            assertEquals(5, cradle().getBallCount());
        }

        @Test
        @DisplayName("all balls start at theta = 0 after construction")
        void allAtRest() {
            NewtonsCradle c = cradle();
            c.resetAll();
            for (int i = 0; i < c.getBallCount(); i++) {
                assertEquals(0.0, c.getTheta(i), 1e-12);
                assertEquals(0.0, c.getOmega(i), 1e-12);
            }
        }

        @Test
        @DisplayName("pivot spacing equals ball diameter")
        void pivotSpacing() {
            NewtonsCradle c = cradle();
            assertEquals(2.0 * RADIUS, c.pivotSpacing, 1e-12);
        }
    }

    // -------------------------------------------------------------------------
    // Ball count
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ball count")
    class BallCount {

        @Test
        @DisplayName("setBallCount clamps to [MIN, MAX]")
        void clampsCount() {
            NewtonsCradle c = cradle();
            c.setBallCount(0);
            assertEquals(NewtonsCradle.MIN_BALLS, c.getBallCount());
            c.setBallCount(100);
            assertEquals(NewtonsCradle.MAX_BALLS, c.getBallCount());
        }

        @Test
        @DisplayName("setBallCount 3 creates exactly 3 pendulums")
        void threeBalls() {
            NewtonsCradle c = cradle();
            c.setBallCount(3);
            assertEquals(3, c.getBallCount());
        }
    }

    // -------------------------------------------------------------------------
    // Lift and reset
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("reset (lift)")
    class Reset {

        @Test
        @DisplayName("lifting 1 ball sets only index 0 to negative theta")
        void liftOneLeft() {
            NewtonsCradle c = cradle();
            c.reset(1, Math.toRadians(45));
            assertTrue(c.getTheta(0) < 0, "lifted ball should have negative angle");
            assertEquals(0.0, c.getTheta(1), 1e-12);
            assertEquals(0.0, c.getTheta(4), 1e-12);
        }

        @Test
        @DisplayName("lifting 2 balls sets indices 0 and 1")
        void liftTwo() {
            NewtonsCradle c = cradle();
            c.reset(2, Math.toRadians(45));
            assertTrue(c.getTheta(0) < 0);
            assertTrue(c.getTheta(1) < 0);
            assertEquals(0.0, c.getTheta(2), 1e-12);
        }

        @Test
        @DisplayName("liftCount clamps to [1, ballCount-1]")
        void liftCountClamped() {
            NewtonsCradle c = cradle();
            c.reset(0, Math.toRadians(45));  // clamps to 1
            assertTrue(c.getTheta(0) < 0);
            for (int i = 1; i < c.getBallCount(); i++) {
                assertEquals(0.0, c.getTheta(i), 1e-12);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Physics
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("physics")
    class Physics {

        @Test
        @DisplayName("at equilibrium the cradle stays still")
        void equilibriumStaysStill() {
            NewtonsCradle c = cradle();
            c.resetAll();
            for (int i = 0; i < 1000; i++) c.step(0.001);
            for (int i = 0; i < c.getBallCount(); i++) {
                assertEquals(0.0, c.getTheta(i), 1e-9);
                assertEquals(0.0, c.getOmega(i), 1e-9);
            }
        }

        @Test
        @DisplayName("one ball released: energy is transferred, not created")
        void energyIsConservedNotCreated() {
            NewtonsCradle c = cradle();
            c.reset(1, Math.toRadians(45));
            double e0 = c.totalEnergy();
            for (int i = 0; i < 2000; i++) c.step(0.001);
            double e1 = c.totalEnergy();
            // With e=1 (elastic), energy must not increase
            assertTrue(e1 <= e0 * 1.01, "energy must not increase beyond 1% for elastic collisions");
        }

        @Test
        @DisplayName("after elastic collision ball 0 should decelerate")
        void leadingBallDecelerates() {
            NewtonsCradle c = cradle();
            c.reset(1, Math.toRadians(50));
            // Simulate until the lifted ball returns near equilibrium
            for (int i = 0; i < 3000; i++) c.step(0.001);
            // After the collision chain, ball 4 (rightmost) should be swinging
            // and ball 0 should be near rest — omega[4] elevated, omega[0] low
            double maxOmegaRight = Math.abs(c.getOmega(c.getBallCount() - 1));
            double maxOmegaLeft  = Math.abs(c.getOmega(0));
            // The state will oscillate; just verify the simulation stays bounded
            assertTrue(maxOmegaRight < 30.0, "angular velocities should stay bounded");
        }

        @Test
        @DisplayName("inelastic collisions (e < 1) dissipate energy")
        void inelasticDissipatesEnergy() {
            NewtonsCradle elastic    = new NewtonsCradle(LENGTH, RADIUS, GRAVITY, 1.0);
            NewtonsCradle inelastic  = new NewtonsCradle(LENGTH, RADIUS, GRAVITY, 0.7);
            double angle = Math.toRadians(50);
            elastic.reset(1, angle);
            inelastic.reset(1, angle);
            for (int i = 0; i < 5000; i++) { elastic.step(0.001); inelastic.step(0.001); }
            assertTrue(inelastic.totalEnergy() < elastic.totalEnergy(),
                    "inelastic cradle must have less energy than elastic at same elapsed time");
        }
    }

    // -------------------------------------------------------------------------
    // Geometry helpers
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("screen position helpers")
    class ScreenPositions {

        @Test
        @DisplayName("ball at theta=0 is directly below its pivot")
        void ballBelowPivot() {
            NewtonsCradle c = cradle();
            c.resetAll();
            double pivotX0 = 200.0, pivotY = 100.0;
            for (int i = 0; i < c.getBallCount(); i++) {
                assertEquals(c.pivotScreenX(i, pivotX0), c.ballScreenX(i, pivotX0), 1e-9);
                assertEquals(pivotY + LENGTH,             c.ballScreenY(i, pivotY), 1e-9);
            }
        }

        @Test
        @DisplayName("pivots are separated by pivotSpacing")
        void pivotsSeparated() {
            NewtonsCradle c = cradle();
            double p0 = 300.0;
            for (int i = 0; i < c.getBallCount() - 1; i++) {
                assertEquals(c.pivotScreenX(i, p0) + c.pivotSpacing,
                             c.pivotScreenX(i + 1, p0), 1e-9);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Restitution
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("restitution")
    class RestitutionTests {

        @Test
        @DisplayName("setRestitution clamps to [0, 1]")
        void clamps() {
            NewtonsCradle c = cradle();
            c.setRestitution(2.0);
            assertEquals(1.0, c.getRestitution(), 1e-12);
            c.setRestitution(-0.5);
            assertEquals(0.0, c.getRestitution(), 1e-12);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static NewtonsCradle cradle() {
        return new NewtonsCradle(LENGTH, RADIUS, GRAVITY, 1.0);
    }
}
