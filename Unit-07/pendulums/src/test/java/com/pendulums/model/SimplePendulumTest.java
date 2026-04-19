package com.pendulums.model;

import com.pendulums.physics.Integrator;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SimplePendulum}.
 */
@DisplayName("SimplePendulum")
class SimplePendulumTest {

    private static final double LENGTH  = 200.0;
    private static final double GRAVITY = 980.0;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("stores initial angle and zero velocity")
        void initialState() {
            SimplePendulum p = pendulum(Math.PI / 4);
            assertEquals(Math.PI / 4, p.getTheta(), 1e-12);
            assertEquals(0.0,        p.getOmega(), 1e-12);
        }

        @Test
        @DisplayName("bob relative position is zero when theta = 0")
        void bobAtEquilibrium() {
            SimplePendulum p = pendulum(0);
            assertEquals(0.0,   p.bobRelX(), 1e-12);
            assertEquals(LENGTH, p.bobRelY(), 1e-12);
        }

        @Test
        @DisplayName("bob x and y match trigonometry")
        void bobPosition() {
            double theta = Math.PI / 6;  // 30°
            SimplePendulum p = pendulum(theta);
            assertEquals(LENGTH * Math.sin(theta), p.bobRelX(), 1e-9);
            assertEquals(LENGTH * Math.cos(theta), p.bobRelY(), 1e-9);
        }
    }

    // -------------------------------------------------------------------------
    // Physics — small-angle period
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("small-angle period")
    class Period {

        @Test
        @DisplayName("T = 2π√(L/g)")
        void periodFormula() {
            SimplePendulum p = pendulum(0.1);
            double expected  = 2.0 * Math.PI * Math.sqrt(LENGTH / GRAVITY);
            assertEquals(expected, p.smallAnglePeriod(), 1e-9);
        }

        @Test
        @DisplayName("period doubles when length quadruples")
        void periodScalesWithLength() {
            SimplePendulum p1 = new SimplePendulum(0.1, LENGTH, GRAVITY, 0, Integrator.Method.RK4);
            SimplePendulum p2 = new SimplePendulum(0.1, LENGTH * 4, GRAVITY, 0, Integrator.Method.RK4);
            assertEquals(p1.smallAnglePeriod() * 2, p2.smallAnglePeriod(), 1e-9);
        }
    }

    // -------------------------------------------------------------------------
    // Energy
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("energy")
    class Energy {

        @Test
        @DisplayName("potential energy is zero at equilibrium")
        void peAtEquilibrium() {
            SimplePendulum p = pendulum(0);
            assertEquals(0.0, p.potentialEnergy(), 1e-12);
        }

        @Test
        @DisplayName("kinetic energy is zero at release (omega=0)")
        void keAtRelease() {
            SimplePendulum p = pendulum(Math.PI / 3);
            assertEquals(0.0, p.kineticEnergy(), 1e-12);
        }

        @Test
        @DisplayName("total energy = KE + PE")
        void totalEnergyIdentity() {
            SimplePendulum p = pendulum(Math.PI / 4);
            assertEquals(p.kineticEnergy() + p.potentialEnergy(), p.totalEnergy(), 1e-12);
        }

        @Test
        @DisplayName("RK4 conserves total energy over one full period")
        void energyConservationRK4() {
            SimplePendulum p = new SimplePendulum(0.2, LENGTH, GRAVITY, 0, Integrator.Method.RK4);
            double e0   = p.totalEnergy();
            double T    = p.smallAnglePeriod();
            double dt   = 0.001;
            int    n    = (int)(T / dt);
            for (int i = 0; i < n; i++) p.step(dt);
            assertEquals(e0, p.totalEnergy(), e0 * 0.002, "energy drift must be < 0.2 %");
        }

        @Test
        @DisplayName("Euler integration accumulates energy drift")
        void eulerEnergyDrift() {
            SimplePendulum pEuler = new SimplePendulum(0.5, LENGTH, GRAVITY, 0, Integrator.Method.EULER);
            SimplePendulum pRK4   = new SimplePendulum(0.5, LENGTH, GRAVITY, 0, Integrator.Method.RK4);
            double e0 = pEuler.totalEnergy();
            for (int i = 0; i < 2000; i++) { pEuler.step(0.005); pRK4.step(0.005); }
            double eulerDrift = Math.abs(pEuler.totalEnergy() - e0);
            double rk4Drift   = Math.abs(pRK4.totalEnergy()   - e0);
            assertTrue(eulerDrift > rk4Drift,
                    "Euler energy drift should exceed RK4 drift at same step size");
        }
    }

    // -------------------------------------------------------------------------
    // Dynamics
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("dynamics")
    class Dynamics {

        @Test
        @DisplayName("positive theta swings toward negative (restoring force)")
        void restoringForce() {
            SimplePendulum p = new SimplePendulum(0.3, LENGTH, GRAVITY, 0, Integrator.Method.RK4);
            double omega0 = p.getOmega();
            p.step(0.005);
            assertTrue(p.getOmega() < omega0, "angular velocity should become negative for positive release angle");
        }

        @Test
        @DisplayName("damping reduces total energy over time")
        void dampingReducesEnergy() {
            SimplePendulum p = new SimplePendulum(0.5, LENGTH, GRAVITY, 0.5, Integrator.Method.RK4);
            double e0 = p.totalEnergy();
            for (int i = 0; i < 1000; i++) p.step(0.005);
            assertTrue(p.totalEnergy() < e0, "damping must remove energy from the system");
        }

        @Test
        @DisplayName("reset restores angle and clears velocity")
        void resetRestoresState() {
            SimplePendulum p = new SimplePendulum(0.3, LENGTH, GRAVITY, 0, Integrator.Method.RK4);
            for (int i = 0; i < 100; i++) p.step(0.01);
            p.reset(1.0);
            assertEquals(1.0, p.getTheta(), 1e-12);
            assertEquals(0.0, p.getOmega(), 1e-12);
        }
    }

    // -------------------------------------------------------------------------
    // Trail
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("trail")
    class Trail {

        @Test
        @DisplayName("trail grows up to MAX_TRAIL then stops growing")
        void trailCapacity() {
            SimplePendulum p = pendulum(0.5);
            for (int i = 0; i < SimplePendulum.MAX_TRAIL + 50; i++) {
                p.step(0.001);
                p.recordTrail(300, 100);
            }
            assertEquals(SimplePendulum.MAX_TRAIL, p.getTrail().size());
        }

        @Test
        @DisplayName("reset clears trail")
        void resetClearsTrail() {
            SimplePendulum p = pendulum(0.5);
            for (int i = 0; i < 10; i++) { p.step(0.01); p.recordTrail(0, 0); }
            assertTrue(p.getTrail().size() > 0);
            p.reset(0.5);
            assertEquals(0, p.getTrail().size());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static SimplePendulum pendulum(double theta) {
        return new SimplePendulum(theta, LENGTH, GRAVITY, 0, Integrator.Method.RK4);
    }
}
