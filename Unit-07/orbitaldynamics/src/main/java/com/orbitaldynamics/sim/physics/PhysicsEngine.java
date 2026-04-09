package com.orbitaldynamics.sim.physics;

import com.orbitaldynamics.math.Vector2D;
import com.orbitaldynamics.sim.body.OrbitalBody;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * N-body physics engine using a 4th-order Runge-Kutta (RK4) integrator.
 *
 * <h2>Equations of Motion</h2>
 * <p>Each body i experiences gravitational acceleration from every other body j:
 * <pre>
 *   a_i = G × Σ_{j≠i}  m_j × (r_j - r_i) / (|r_j - r_i|² + ε²)^(3/2)
 * </pre>
 * The softening parameter ε prevents numerical blow-up at very short distances.
 *
 * <h2>RK4 Integration</h2>
 * <p>The state vector is a flat double[] encoding [x₀,y₀,vx₀,vy₀, x₁,y₁,...].
 * Four derivative evaluations per step give O(dt⁴) local error.
 *
 * <h2>Collision Response</h2>
 * <p>Two collision modes are available:
 * <ul>
 *   <li><b>Elastic/inelastic bounce</b> — impulse J = (1+e)·v_rel·(1/mₐ+1/m_b)⁻¹
 *       where e is the coefficient of restitution (0=perfectly inelastic, 1=elastic).</li>
 *   <li><b>Merge</b> — approaching bodies are absorbed: momentum is conserved,
 *       area is conserved (r_new = √(r_a²+r_b²)), and spin angular momentum is
 *       conserved. The absorbed body is returned via {@link #drainMergeRemovals()}.</li>
 * </ul>
 */
public final class PhysicsEngine {

    public static final double DEFAULT_G     = 5000.0;
    public static final double SOFTENING_SQ  = 25.0;   // ε² = 5²

    private double  G;
    private boolean elasticCollisions = true;
    private double  restitution       = 1.0;   // 0=perfectly inelastic, 1=elastic
    private boolean mergeOnCollision  = false;

    /** Bodies removed during the last step due to merge; drained by caller. */
    private final List<OrbitalBody> mergeRemovals = new ArrayList<>();

    public PhysicsEngine()          { this(DEFAULT_G); }
    public PhysicsEngine(double G)  { this.G = G; }

    // -- Getters / setters ----------------------------------------------------

    public double  getG()                        { return G; }
    public void    setG(double g)                { this.G = g; }

    public boolean isElasticCollisions()         { return elasticCollisions; }
    public void    setElasticCollisions(boolean b){ elasticCollisions = b; }

    /** Coefficient of restitution [0,1] used in bounce mode. 0 = stick, 1 = elastic. */
    public double  getRestitution()              { return restitution; }
    public void    setRestitution(double e)      { this.restitution = Math.max(0, Math.min(1, e)); }

    /** When true, colliding bodies merge instead of bouncing. */
    public boolean isMergeOnCollision()          { return mergeOnCollision; }
    public void    setMergeOnCollision(boolean b){ this.mergeOnCollision = b; }

    /**
     * Returns (and clears) any bodies that were absorbed into another body
     * during the most recent {@link #step} call.  The caller should remove
     * these from the body list.
     */
    public List<OrbitalBody> drainMergeRemovals() {
        if (mergeRemovals.isEmpty()) return List.of();
        List<OrbitalBody> result = new ArrayList<>(mergeRemovals);
        mergeRemovals.clear();
        return result;
    }

    // -------------------------------------------------------------------------
    // Main step
    // -------------------------------------------------------------------------

    /**
     * Advances the simulation by {@code dt} seconds.
     * Pinned bodies are held fixed but still exert gravity.
     */
    public void step(List<OrbitalBody> bodies, double dt) {
        if (bodies.isEmpty()) return;

        int n = bodies.size();
        double[] state  = flattenPositionsVelocities(bodies, n);
        double[] masses = getMasses(bodies, n);
        boolean[] pinned= getPinned(bodies, n);

        // RK4
        double[] k1 = derivative(state, masses, pinned, n);
        double[] k2 = derivative(addScaled(state, k1, dt * 0.5), masses, pinned, n);
        double[] k3 = derivative(addScaled(state, k2, dt * 0.5), masses, pinned, n);
        double[] k4 = derivative(addScaled(state, k3, dt),       masses, pinned, n);

        for (int i = 0; i < state.length; i++) {
            state[i] += dt / 6.0 * (k1[i] + 2 * k2[i] + 2 * k3[i] + k4[i]);
        }

        applyState(bodies, state, n);

        // Rotation (omega is constant — no external torques)
        for (OrbitalBody b : bodies) {
            if (!b.isPinned()) b.setAngle(b.getAngle() + b.getOmega() * dt);
        }

        // Collision detection and resolution
        if (elasticCollisions || mergeOnCollision) resolveCollisions(bodies);

        // Trail recording
        for (OrbitalBody b : bodies) b.tickTrail();
    }

    // -------------------------------------------------------------------------
    // RK4 derivative computation
    // -------------------------------------------------------------------------

    private double[] derivative(double[] state, double[] masses, boolean[] pinned, int n) {
        double[] d = new double[4 * n];

        for (int i = 0; i < n; i++) {
            double xi  = state[4 * i],     yi  = state[4 * i + 1];
            double vxi = state[4 * i + 2], vyi = state[4 * i + 3];

            d[4 * i]     = pinned[i] ? 0 : vxi;
            d[4 * i + 1] = pinned[i] ? 0 : vyi;

            if (pinned[i]) continue;

            double ax = 0, ay = 0;
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                double dx    = state[4 * j]     - xi;
                double dy    = state[4 * j + 1] - yi;
                double dist2 = dx * dx + dy * dy + SOFTENING_SQ;
                double dist  = Math.sqrt(dist2);
                double fac   = G * masses[j] / (dist2 * dist);
                ax += fac * dx;
                ay += fac * dy;
            }
            d[4 * i + 2] = ax;
            d[4 * i + 3] = ay;
        }
        return d;
    }

    // -------------------------------------------------------------------------
    // Collision resolution
    // -------------------------------------------------------------------------

    private void resolveCollisions(List<OrbitalBody> bodies) {
        int n = bodies.size();
        // Track bodies absorbed this step so we skip them in subsequent pairs
        Set<Long> absorbedIds = mergeOnCollision ? new HashSet<>() : null;

        for (int i = 0; i < n; i++) {
            OrbitalBody a = bodies.get(i);
            if (absorbedIds != null && absorbedIds.contains(a.getId())) continue;

            for (int j = i + 1; j < n; j++) {
                OrbitalBody b = bodies.get(j);
                if (absorbedIds != null && absorbedIds.contains(b.getId())) continue;
                if (a.isPinned() && b.isPinned()) continue;

                Vector2D delta = b.getPosition().sub(a.getPosition());
                double dist    = delta.magnitude();
                double minDist = a.getRadius() + b.getRadius();

                if (dist >= minDist || dist <= 1e-6) continue;

                Vector2D n_hat  = delta.scale(1.0 / dist);
                Vector2D relVel = a.getVelocity().sub(b.getVelocity());
                double relSpeed = relVel.dot(n_hat);

                if (relSpeed > 0) {  // bodies approaching
                    if (mergeOnCollision && !a.isPinned() && !b.isPinned()) {
                        mergeBodies(a, b);
                        absorbedIds.add(b.getId());
                        mergeRemovals.add(b);
                        continue;  // positional correction not needed after merge
                    }

                    // Elastic/inelastic bounce
                    double ma = a.getMass(), mb = b.getMass();
                    double invMass = (a.isPinned() ? 0 : 1.0 / ma)
                                   + (b.isPinned() ? 0 : 1.0 / mb);
                    if (invMass < 1e-15) continue;
                    double J = (1.0 + restitution) * relSpeed / invMass;
                    if (!a.isPinned()) a.setVelocity(a.getVelocity().sub(n_hat.scale(J / ma)));
                    if (!b.isPinned()) b.setVelocity(b.getVelocity().add(n_hat.scale(J / mb)));
                }

                // Positional correction: push overlapping bodies apart
                double overlap = minDist - dist + 0.5;
                if (!a.isPinned() && !b.isPinned()) {
                    a.setPosition(a.getPosition().sub(n_hat.scale(overlap * 0.5)));
                    b.setPosition(b.getPosition().add(n_hat.scale(overlap * 0.5)));
                } else if (!a.isPinned()) {
                    a.setPosition(a.getPosition().sub(n_hat.scale(overlap)));
                } else if (!b.isPinned()) {
                    b.setPosition(b.getPosition().add(n_hat.scale(overlap)));
                }
            }
        }
    }

    /**
     * Merges body {@code absorbed} into body {@code survivor}, conserving:
     * <ul>
     *   <li>Linear momentum: p = m₁v₁ + m₂v₂</li>
     *   <li>Cross-sectional area: r_new = √(r₁²+r₂²)</li>
     *   <li>Spin angular momentum: L = ½mr²ω</li>
     * </ul>
     * The survivor's position is set to the center of mass.
     */
    private static void mergeBodies(OrbitalBody survivor, OrbitalBody absorbed) {
        double ma = survivor.getMass(), mb = absorbed.getMass();
        double totalMass = ma + mb;

        // Center of mass position
        Vector2D newPos = survivor.getPosition().scale(ma)
                            .add(absorbed.getPosition().scale(mb))
                            .scale(1.0 / totalMass);

        // Momentum-conserving velocity
        Vector2D newVel = survivor.getVelocity().scale(ma)
                            .add(absorbed.getVelocity().scale(mb))
                            .scale(1.0 / totalMass);

        // Area-conserving radius
        double ra = survivor.getRadius(), rb = absorbed.getRadius();
        double newR = Math.sqrt(ra * ra + rb * rb);

        // Angular momentum conservation: L = I*ω = (½mr²)ω
        double La = 0.5 * ma * ra * ra * survivor.getOmega();
        double Lb = 0.5 * mb * rb * rb * absorbed.getOmega();
        double newI = 0.5 * totalMass * newR * newR;
        double newOmega = (newI > 1e-15) ? (La + Lb) / newI : 0;

        double newDensity = totalMass / (Math.PI * newR * newR);

        survivor.setPosition(newPos);
        survivor.setVelocity(newVel);
        survivor.setRadius(newR);
        survivor.setDensity(newDensity);
        survivor.setOmega(newOmega);
    }

    // -------------------------------------------------------------------------
    // Energy and momentum calculations
    // -------------------------------------------------------------------------

    /** Total kinetic + gravitational potential energy. */
    public double totalEnergy(List<OrbitalBody> bodies) {
        double ke = 0, pe = 0;
        for (OrbitalBody b : bodies) ke += b.kineticEnergy();
        int n = bodies.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                OrbitalBody a = bodies.get(i);
                OrbitalBody b = bodies.get(j);
                double r = a.getPosition().distanceTo(b.getPosition());
                if (r > 0.1) pe -= G * a.getMass() * b.getMass() / r;
            }
        }
        return ke + pe;
    }

    /** Total angular momentum (z-component). */
    public double totalAngularMomentum(List<OrbitalBody> bodies) {
        double L = 0;
        for (OrbitalBody b : bodies) {
            L += b.getPosition().cross(b.getVelocity()) * b.getMass();
            L += 0.5 * b.getMass() * b.getRadius() * b.getRadius() * b.getOmega();
        }
        return L;
    }

    /** Center of mass position. */
    public Vector2D centerOfMass(List<OrbitalBody> bodies) {
        double totalMass = 0, cx = 0, cy = 0;
        for (OrbitalBody b : bodies) {
            double m = b.getMass();
            totalMass += m;
            cx += m * b.getPosition().x();
            cy += m * b.getPosition().y();
        }
        if (totalMass < 1e-15) return Vector2D.ZERO;
        return new Vector2D(cx / totalMass, cy / totalMass);
    }

    // -------------------------------------------------------------------------
    // Preset scenarios
    // -------------------------------------------------------------------------

    /**
     * Circular binary-star orbit.
     *
     * <p>Two equal-mass stars separated by {@code sep}, each orbiting the
     * shared center of mass at radius sep/2.  The correct circular orbital
     * speed for an equal-mass pair is v = √(G·m / (2·sep)).
     */
    public static List<OrbitalBody> presetBinaryStars(double G) {
        double sep    = 300.0;
        double mass   = 5000.0;
        double radius = 30.0;

        // Each star orbits the COM at distance sep/2.
        // Centripetal: mv²/(sep/2) = G·m²/sep²  →  v = √(G·m/(2·sep))
        double v = Math.sqrt(G * mass / (2.0 * sep));

        double density = mass / (Math.PI * radius * radius);
        List<OrbitalBody> bodies = new ArrayList<>();
        bodies.add(new OrbitalBody(
            new Vector2D(-sep / 2, 0), new Vector2D(0, -v), radius, density, 0,  0.3));
        bodies.add(new OrbitalBody(
            new Vector2D( sep / 2, 0), new Vector2D(0,  v), radius, density, 0, -0.2));
        bodies.get(0).setName("Star A");
        bodies.get(1).setName("Star B");
        return bodies;
    }

    /**
     * Star + two-planet system.
     */
    public static List<OrbitalBody> presetStarAndPlanets(double G) {
        List<OrbitalBody> bodies = new ArrayList<>();

        double starMass    = 50000.0;
        double starR       = 50.0;
        double starDensity = starMass / (Math.PI * starR * starR);
        OrbitalBody star   = new OrbitalBody(Vector2D.ZERO, Vector2D.ZERO,
                                             starR, starDensity, 0, 0.05);
        star.setName("Star");
        bodies.add(star);

        double[] orbitR  = {200, 380};
        double[] planetR = {12, 18};
        String[] names   = {"Planet A", "Planet B"};

        for (int i = 0; i < 2; i++) {
            double r   = orbitR[i];
            double v   = Math.sqrt(G * starMass / r);
            double pd  = 1.0;
            OrbitalBody planet = new OrbitalBody(
                new Vector2D(r, 0), new Vector2D(0, v), planetR[i], pd, 0, 0.5);
            planet.setName(names[i]);
            bodies.add(planet);
        }
        return bodies;
    }

    /**
     * The famous figure-eight three-body solution (Chenciner & Montgomery, 2000).
     * Three equal-mass bodies trace a figure-eight orbit with period T ≈ 6.3259.
     *
     * <h2>Scaling</h2>
     * <p>The original initial conditions use G=1, m=1, positions ~1.  We scale
     * positions AND velocities by {@code scale=200} to fill the screen.  Keeping
     * the same time unit then requires G·m to scale as scale³:
     * <pre>  m = scale³ / G</pre>
     */
    public static List<OrbitalBody> presetFigureEight(double G) {
        double scale = 200.0;

        // Chenciner-Montgomery initial conditions (G=1, m=1, unit length)
        double[][] pos = {
            {-0.97000436,  0.24308753},
            { 0.97000436, -0.24308753},
            { 0.0,         0.0       }
        };
        double[][] vel = {
            { 0.93240737 / 2,  0.86473146 / 2},
            { 0.93240737 / 2,  0.86473146 / 2},
            {-0.93240737,     -0.86473146     }
        };

        // Scale positions and velocities by `scale`; then G·m must equal scale³
        // to preserve the orbit shape at the same time rate.
        double mass    = scale * scale * scale / G;   // e.g. 200³/5000 = 1600
        double radius  = 18.0;
        double density = mass / (Math.PI * radius * radius);

        String[] names = {"Alpha", "Beta", "Gamma"};
        List<OrbitalBody> bodies = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            bodies.add(new OrbitalBody(
                new Vector2D(pos[i][0] * scale, pos[i][1] * scale),
                new Vector2D(vel[i][0] * scale, vel[i][1] * scale),
                radius, density, 0, 0.8));
            bodies.get(i).setName(names[i]);
        }
        return bodies;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static double[] flattenPositionsVelocities(List<OrbitalBody> bodies, int n) {
        double[] state = new double[4 * n];
        for (int i = 0; i < n; i++) {
            OrbitalBody b = bodies.get(i);
            state[4*i]   = b.getPosition().x();
            state[4*i+1] = b.getPosition().y();
            state[4*i+2] = b.getVelocity().x();
            state[4*i+3] = b.getVelocity().y();
        }
        return state;
    }

    private static void applyState(List<OrbitalBody> bodies, double[] state, int n) {
        for (int i = 0; i < n; i++) {
            OrbitalBody b = bodies.get(i);
            if (!b.isPinned()) {
                b.setPosition(new Vector2D(state[4*i],   state[4*i+1]));
                b.setVelocity(new Vector2D(state[4*i+2], state[4*i+3]));
            }
        }
    }

    private static double[] getMasses(List<OrbitalBody> bodies, int n) {
        double[] m = new double[n];
        for (int i = 0; i < n; i++) m[i] = bodies.get(i).getMass();
        return m;
    }

    private static boolean[] getPinned(List<OrbitalBody> bodies, int n) {
        boolean[] p = new boolean[n];
        for (int i = 0; i < n; i++) p[i] = bodies.get(i).isPinned();
        return p;
    }

    private static double[] addScaled(double[] a, double[] b, double s) {
        double[] r = new double[a.length];
        for (int i = 0; i < a.length; i++) r[i] = a[i] + b[i] * s;
        return r;
    }
}
