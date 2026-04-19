package com.pendulums.physics;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Integrator}.
 *
 * <p>Uses analytical solutions for simple ODE systems to verify the accuracy
 * and convergence behaviour of the Euler and RK4 integrators.
 */
@DisplayName("Integrator")
class IntegratorTest {

    // -------------------------------------------------------------------------
    // Euler
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Euler method")
    class EulerTests {

        @Test
        @DisplayName("integrates constant derivative exactly")
        void constantDerivative() {
            // dy/dt = 1  →  y(t) = y0 + t
            Integrator.ODE ode = s -> new double[]{1.0};
            double[] state = Integrator.euler(new double[]{0.0}, ode, 0.5);
            assertEquals(0.5, state[0], 1e-12);
        }

        @Test
        @DisplayName("integrates linear derivative (exponential growth) one step")
        void linearDerivativeOneStep() {
            // dy/dt = y  →  exact: y(dt) = y0*exp(dt)
            // Euler one step: y(dt) = y0*(1 + dt)
            Integrator.ODE ode = s -> new double[]{s[0]};
            double dt    = 0.1;
            double[] s   = Integrator.euler(new double[]{1.0}, ode, dt);
            assertEquals(1.0 + dt, s[0], 1e-12);
        }

        @Test
        @DisplayName("returns a new array (does not mutate input)")
        void doesNotMutateInput() {
            Integrator.ODE ode = s -> new double[]{1.0};
            double[] input = {3.0};
            double[] output = Integrator.euler(input, ode, 1.0);
            assertEquals(3.0, input[0], 1e-12, "input must not change");
            assertEquals(4.0, output[0], 1e-12);
        }

        @Test
        @DisplayName("works with a 4-component state vector")
        void fourComponentState() {
            // All components have derivative = component index
            Integrator.ODE ode = s -> new double[]{0, 1, 2, 3};
            double[] s = Integrator.euler(new double[]{0, 0, 0, 0}, ode, 1.0);
            assertEquals(0.0, s[0], 1e-12);
            assertEquals(1.0, s[1], 1e-12);
            assertEquals(2.0, s[2], 1e-12);
            assertEquals(3.0, s[3], 1e-12);
        }
    }

    // -------------------------------------------------------------------------
    // RK4
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("RK4 method")
    class RK4Tests {

        @Test
        @DisplayName("integrates constant derivative exactly")
        void constantDerivative() {
            Integrator.ODE ode = s -> new double[]{2.5};
            double[] state = Integrator.rk4(new double[]{0.0}, ode, 1.0);
            assertEquals(2.5, state[0], 1e-12);
        }

        @Test
        @DisplayName("integrates dy/dt = y more accurately than Euler for same dt")
        void betterThanEulerForExponential() {
            Integrator.ODE ode = s -> new double[]{s[0]};
            double dt    = 0.5;
            double y0    = 1.0;
            double exact = Math.exp(dt);  // e^0.5 ≈ 1.6487212...
            double euler = Integrator.euler(new double[]{y0}, ode, dt)[0];  // 1.5
            double rk4   = Integrator.rk4  (new double[]{y0}, ode, dt)[0];

            double eulerErr = Math.abs(euler - exact);
            double rk4Err   = Math.abs(rk4   - exact);
            assertTrue(rk4Err < eulerErr,
                    "RK4 error " + rk4Err + " should be smaller than Euler error " + eulerErr);
            assertEquals(exact, rk4, 5e-4, "RK4 should match exact solution to 3 d.p. for dt=0.5");
        }

        @Test
        @DisplayName("integrates SHM (harmonic oscillator) and conserves energy")
        void simpleHarmonicOscillator() {
            // d²x/dt² = −x  →  state = [x, v],  d[x,v]/dt = [v, −x]
            // Exact: x(t) = cos(t), v(t) = −sin(t)  with x0=1, v0=0
            Integrator.ODE ode = s -> new double[]{s[1], -s[0]};
            double[] state = {1.0, 0.0};
            double dt      = 0.001;
            int    steps   = 6284;  // approximately 2π seconds (one full period)
            double e0      = 0.5 * state[1] * state[1] + 0.5 * state[0] * state[0];
            for (int i = 0; i < steps; i++) {
                state = Integrator.rk4(state, ode, dt);
            }
            double e1 = 0.5 * state[1] * state[1] + 0.5 * state[0] * state[0];
            assertEquals(e0, e1, 1e-6, "RK4 should conserve energy over one full period");
            assertEquals(1.0, state[0], 1e-4, "position should return to initial value");
        }

        @Test
        @DisplayName("does not mutate input state")
        void doesNotMutateInput() {
            Integrator.ODE ode = s -> new double[]{s[0]};
            double[] input  = {5.0};
            Integrator.rk4(input, ode, 1.0);
            assertEquals(5.0, input[0], 1e-12);
        }
    }

    // -------------------------------------------------------------------------
    // Dispatch helper
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("step dispatch")
    class StepDispatch {

        @Test
        @DisplayName("EULER method dispatches to euler()")
        void dispatchEuler() {
            Integrator.ODE ode = s -> new double[]{1.0};
            double[] via   = Integrator.step(new double[]{0.0}, ode, 1.0, Integrator.Method.EULER);
            double[] direct = Integrator.euler(new double[]{0.0}, ode, 1.0);
            assertEquals(direct[0], via[0], 1e-12);
        }

        @Test
        @DisplayName("RK4 method dispatches to rk4()")
        void dispatchRK4() {
            Integrator.ODE ode = s -> new double[]{s[0]};
            double[] via    = Integrator.step(new double[]{1.0}, ode, 0.1, Integrator.Method.RK4);
            double[] direct = Integrator.rk4(new double[]{1.0}, ode, 0.1);
            assertEquals(direct[0], via[0], 1e-12);
        }
    }
}
