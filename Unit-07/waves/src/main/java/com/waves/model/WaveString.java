package com.waves.model;

/**
 * Finite-difference model of a 1D vibrating string under the wave equation.
 *
 * <h2>Governing PDE</h2>
 * <pre>  ∂²u/∂t² = c² · ∂²u/∂x²</pre>
 * where {@code u(x,t)} is the transverse displacement, and {@code c} is the
 * wave speed (metres or pixels per second, consistently scaled).
 *
 * <h2>Finite-difference update</h2>
 * <pre>  u_next[i] = 2·u[i] − u_prev[i] + r²·(u[i+1] − 2·u[i] + u[i−1])</pre>
 * where the Courant number {@code r = c·dt/dx}.  Stability requires {@code r ≤ 1};
 * this class enforces {@code r = 0.95} so {@code dt} is derived from {@code c}.
 *
 * <h2>Boundary conditions</h2>
 * <p>Fixed (Dirichlet) endpoints: {@code u[0] = u[N−1] = 0} at all times.
 *
 * <h2>Damping</h2>
 * <p>After each update step the displacement is scaled by {@code (1 − damping)}
 * to model energy loss; {@code damping = 0} gives an undamped string.
 *
 * <h2>Grid</h2>
 * <ul>
 *   <li>{@code N = 400} grid points, {@code dx = 1.0}</li>
 *   <li>{@code dt = r · dx / c} — recomputed every call to {@link #step}</li>
 * </ul>
 */
public class WaveString {

    /** Number of grid points (including fixed endpoints). */
    public static final int N = 400;

    /** Spatial step size (grid units). */
    public static final double DX = 1.0;

    /** Courant number used to choose {@code dt}; must be ≤ 1 for stability. */
    public static final double COURANT = 0.95;

    private final double[] u;
    private final double[] uPrev;
    private final double[] uNext;

    /**
     * Construct a string initially at rest with zero displacement everywhere.
     */
    public WaveString() {
        u     = new double[N];
        uPrev = new double[N];
        uNext = new double[N];
    }

    // -------------------------------------------------------------------------
    // Simulation
    // -------------------------------------------------------------------------

    /**
     * Advance the simulation by one time step.
     *
     * <p>The time step is computed internally as {@code dt = COURANT · dx / c}.
     *
     * @param c       wave speed (grid units per second); must be &gt; 0
     * @param damping per-step energy loss fraction in [0, 1); 0 = no damping
     */
    public void step(double c, double damping) {
        double dt = COURANT * DX / c;
        double r  = c * dt / DX;   // equals COURANT
        double r2 = r * r;

        // Interior points only; endpoints stay at zero (Dirichlet BC)
        for (int i = 1; i < N - 1; i++) {
            uNext[i] = 2.0 * u[i] - uPrev[i]
                       + r2 * (u[i + 1] - 2.0 * u[i] + u[i - 1]);
            uNext[i] *= (1.0 - damping);
        }

        // Rotate arrays: uPrev ← u, u ← uNext
        System.arraycopy(u,     0, uPrev, 0, N);
        System.arraycopy(uNext, 0, u,     0, N);

        // Enforce fixed boundaries
        u[0]     = 0.0;
        u[N - 1] = 0.0;
        uPrev[0]     = 0.0;
        uPrev[N - 1] = 0.0;
    }

    /**
     * Excite the string with a Gaussian displacement bump centred at {@code centerX}.
     *
     * <pre>  u[i] += amplitude · exp(−((i − centerX) / sigma)²)</pre>
     *
     * <p>The previous-step array is synchronised to the current array so the
     * pluck introduces no artificial velocity kick.
     *
     * @param centerX   grid index of the pluck centre (0 … N−1)
     * @param amplitude peak displacement (grid units)
     * @param sigma     standard deviation of the Gaussian (grid units)
     */
    public void pluck(int centerX, double amplitude, double sigma) {
        for (int i = 1; i < N - 1; i++) {
            double dx = (i - centerX) / sigma;
            u[i]     += amplitude * Math.exp(-(dx * dx));
            uPrev[i]  = u[i];   // zero initial velocity
        }
        u[0]     = 0.0;  uPrev[0]     = 0.0;
        u[N - 1] = 0.0;  uPrev[N - 1] = 0.0;
    }

    /**
     * Reset the string to zero displacement and zero velocity everywhere.
     */
    public void reset() {
        for (int i = 0; i < N; i++) {
            u[i]     = 0.0;
            uPrev[i] = 0.0;
            uNext[i] = 0.0;
        }
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Return the current displacement at grid index {@code i}.
     *
     * @param i grid index (0 … N−1)
     * @return transverse displacement
     */
    public double getDisplacement(int i) {
        return u[i];
    }

    /**
     * Compute a representative total mechanical energy scalar.
     *
     * <p>The energy combines a kinetic-like term {@code (u[i] − uPrev[i])²}
     * and a potential-like (elastic) term {@code (u[i+1] − u[i])²}.
     *
     * @return non-negative energy value; 0 when the string is at rest
     */
    public double energy() {
        double ke = 0.0;
        double pe = 0.0;
        for (int i = 0; i < N - 1; i++) {
            double vel = u[i] - uPrev[i];
            ke += vel * vel;
            double grad = u[i + 1] - u[i];
            pe += grad * grad;
        }
        return ke + pe;
    }

    /**
     * Return a direct reference to the current displacement array (length N).
     *
     * <p>Callers must not modify the returned array.
     *
     * @return displacement array
     */
    public double[] getU() {
        return u;
    }
}
