package com.pendulums.physics;

/**
 * Numerical integrators for systems of first-order ODEs of the form
 * {@code dy/dt = f(y)}, where {@code y} is a state vector.
 *
 * <h2>Available methods</h2>
 * <ul>
 *   <li><b>Euler</b> — first-order, O(dt) local error. Simple to implement but
 *       accumulates energy drift over time: a pendulum simulated with Euler will
 *       slowly gain energy and swing to ever-wider angles.</li>
 *   <li><b>RK4</b> — classical fourth-order Runge-Kutta, O(dt⁴) local error.
 *       Four derivative evaluations per step; far better energy conservation and
 *       the default choice for pendulum physics.</li>
 * </ul>
 *
 * <h2>Why not Euler?</h2>
 * <p>Euler's method is a forward-difference approximation. It introduces a
 * phase lag that causes the integrator to over-estimate the total energy on
 * every step. RK4 samples the derivative at four points within the interval
 * and combines them with Simpson-rule weights, achieving much higher accuracy
 * at modest extra cost.</p>
 *
 * <h2>Alternatives</h2>
 * <p>Symplectic (structure-preserving) integrators such as Leapfrog / Störmer-Verlet
 * and the Symplectic Euler method conserve a <em>modified</em> Hamiltonian exactly,
 * making them attractive for very long simulations. They are not implemented here
 * but are worth exploring for planetary-orbit-level accuracy requirements.</p>
 */
public final class Integrator {

    /** Choice of numerical integration algorithm. */
    public enum Method { EULER, RK4 }

    /**
     * A system of first-order ODEs expressed as a pure derivative function.
     *
     * @param state current state vector {@code y}
     * @return derivative vector {@code dy/dt}
     */
    @FunctionalInterface
    public interface ODE {
        double[] derivatives(double[] state);
    }

    /**
     * Advance {@code state} by one Euler step.
     *
     * @param state current state vector
     * @param ode   derivative function
     * @param dt    time step in seconds
     * @return new state after one step (original array is not mutated)
     */
    public static double[] euler(double[] state, ODE ode, double dt) {
        double[] d = ode.derivatives(state);
        double[] next = new double[state.length];
        for (int i = 0; i < state.length; i++) {
            next[i] = state[i] + d[i] * dt;
        }
        return next;
    }

    /**
     * Advance {@code state} by one RK4 step.
     *
     * <p>Uses the standard four-slope combination:
     * {@code y_{n+1} = y_n + (dt/6)(k1 + 2k2 + 2k3 + k4)}
     *
     * @param state current state vector
     * @param ode   derivative function
     * @param dt    time step in seconds
     * @return new state after one step (original array is not mutated)
     */
    public static double[] rk4(double[] state, ODE ode, double dt) {
        int n = state.length;

        double[] k1 = ode.derivatives(state);

        double[] s2 = new double[n];
        for (int i = 0; i < n; i++) s2[i] = state[i] + 0.5 * dt * k1[i];
        double[] k2 = ode.derivatives(s2);

        double[] s3 = new double[n];
        for (int i = 0; i < n; i++) s3[i] = state[i] + 0.5 * dt * k2[i];
        double[] k3 = ode.derivatives(s3);

        double[] s4 = new double[n];
        for (int i = 0; i < n; i++) s4[i] = state[i] + dt * k3[i];
        double[] k4 = ode.derivatives(s4);

        double[] next = new double[n];
        for (int i = 0; i < n; i++) {
            next[i] = state[i] + (dt / 6.0) * (k1[i] + 2.0 * k2[i] + 2.0 * k3[i] + k4[i]);
        }
        return next;
    }

    /**
     * Dispatch helper: apply the chosen method in one call.
     *
     * @param state  current state vector
     * @param ode    derivative function
     * @param dt     time step in seconds
     * @param method integration algorithm to use
     * @return new state after one step
     */
    public static double[] step(double[] state, ODE ode, double dt, Method method) {
        return method == Method.RK4 ? rk4(state, ode, dt) : euler(state, ode, dt);
    }

    private Integrator() {}
}
