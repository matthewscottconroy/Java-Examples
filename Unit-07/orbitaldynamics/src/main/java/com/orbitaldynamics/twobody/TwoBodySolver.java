package com.orbitaldynamics.twobody;

import com.orbitaldynamics.math.Vector2D;

/**
 * Analytical two-body problem solver using Kepler's equations.
 *
 * <h2>The Two-Body Problem</h2>
 * <p>The relative motion of two gravitating masses reduces to a one-body problem
 * in which a "reduced mass" particle orbits a fixed center with gravitational
 * parameter μ = G(m₁+m₂). Kepler showed that the orbit is always a conic section.
 *
 * <h2>Solution Method</h2>
 * <ol>
 *   <li>Compute orbital elements from initial conditions (r₀, v₀)</li>
 *   <li>For a given time t, solve Kepler's equation for the anomaly</li>
 *   <li>Convert anomaly → true anomaly → position in the orbital plane</li>
 *   <li>Add back the periapsis orientation to get absolute position</li>
 * </ol>
 *
 * <h2>Kepler's Equation by Orbit Type</h2>
 * <ul>
 *   <li>Ellipse:   M = E − e·sin(E)  where M = n·(t−t₀), n = 2π/T</li>
 *   <li>Parabola:  D + D³/3 = W  where W = (t−t₀)·sqrt(μ/2)·(2/p)^(3/2)</li>
 *   <li>Hyperbola: M_h = e·sinh(F) − F  where M_h = n_h·(t−t₀)</li>
 * </ul>
 */
public final class TwoBodySolver {

    /** Maximum Newton iterations for solving Kepler's equation. */
    private static final int MAX_ITER  = 100;
    /** Convergence tolerance. */
    private static final double TOL    = 1e-12;
    /** Eccentricity threshold for circle vs. ellipse. */
    private static final double E_CIRC = 1e-6;
    /** Eccentricity threshold for parabola. */
    private static final double E_PARA = 1e-4;

    private TwoBodySolver() {}

    // -------------------------------------------------------------------------
    // Element derivation
    // -------------------------------------------------------------------------

    /**
     * Derives orbital elements from relative initial conditions.
     *
     * @param relPos  relative position vector r₂ − r₁ (pixels)
     * @param relVel  relative velocity vector v₂ − v₁ (px/s)
     * @param mu      gravitational parameter G(m₁+m₂)
     */
    public static OrbitalElements fromInitialConditions(Vector2D relPos, Vector2D relVel, double mu) {
        double r = relPos.magnitude();
        double v = relVel.magnitude();

        // Specific energy
        double energy = 0.5 * v * v - mu / r;

        // Specific angular momentum (z-component of r × v)
        double h = relPos.cross(relVel);

        // Eccentricity vector: e = v×h/μ − r_hat
        // In 2D: e_vec = (v²/μ − 1/r)·r − (r·v/μ)·v
        double rdotv = relPos.dot(relVel);
        Vector2D eVec = relPos.scale(v * v / mu - 1.0 / r)
                              .sub(relVel.scale(rdotv / mu));
        double e = eVec.magnitude();

        // Periapsis direction
        double periAngle;
        if (e < E_CIRC) {
            // Circular — periapsis direction is undefined; use position angle
            periAngle = Math.atan2(relPos.y(), relPos.x());
        } else {
            periAngle = Math.atan2(eVec.y(), eVec.x());
        }

        // Semi-major axis
        double a = (Math.abs(energy) > 1e-15) ? -mu / (2.0 * energy) : Double.POSITIVE_INFINITY;

        // Period
        double T;
        if (e < 1.0 - E_PARA && a > 0) {
            T = 2.0 * Math.PI * Math.sqrt(a * a * a / mu);
        } else {
            T = Double.NaN;
        }

        // Periapsis and apoapsis distances
        double p = h * h / mu;  // semi-latus rectum
        double periapsis = p / (1.0 + e);
        double apoapsis  = (e < 1.0) ? p / (1.0 - e) : Double.POSITIVE_INFINITY;

        // True anomaly at epoch
        double f0;
        if (e < E_CIRC) {
            f0 = Math.atan2(relPos.y(), relPos.x()) - periAngle;
        } else {
            // cos(f) = (p/r − 1)/e;  sign from h·rdotv
            double cosf = (p / r - 1.0) / e;
            cosf = Math.max(-1.0, Math.min(1.0, cosf));
            f0 = Math.acos(cosf);
            if (rdotv < 0) f0 = -f0;  // moving toward periapsis → negative anomaly
        }

        // Orbit type
        OrbitalElements.OrbitType type;
        if (e < E_CIRC) {
            type = OrbitalElements.OrbitType.CIRCULAR;
        } else if (e < 1.0 - E_PARA) {
            type = OrbitalElements.OrbitType.ELLIPTICAL;
        } else if (e < 1.0 + E_PARA) {
            type = OrbitalElements.OrbitType.PARABOLIC;
        } else {
            type = OrbitalElements.OrbitType.HYPERBOLIC;
        }

        return new OrbitalElements(mu, h, e, a, periAngle, f0, energy, T, periapsis, apoapsis, type);
    }

    // -------------------------------------------------------------------------
    // Position at time
    // -------------------------------------------------------------------------

    /**
     * Returns the relative position at time {@code t} seconds from epoch (t=0).
     * The position is the vector from body 1 to body 2.
     *
     * @param elements orbital elements (must be from {@link #fromInitialConditions})
     * @param t        elapsed time from epoch in seconds
     */
    public static Vector2D positionAtTime(OrbitalElements elements, double t) {
        double e = elements.eccentricity();
        double h = elements.h();
        double mu = elements.mu();
        double p  = elements.semiLatusRectum();
        double f0 = elements.trueAnomalyAtEpoch();

        double f = trueAnomalyAtTime(elements, t);

        // Polar equation of conic section: r = p / (1 + e·cos(f))
        double r = p / (1.0 + e * Math.cos(f));

        // Position in orbital frame (periapsis along +x), then rotate by ω
        double angle = f + elements.periapsisAngle();
        return new Vector2D(r * Math.cos(angle), r * Math.sin(angle));
    }

    /**
     * Returns the relative velocity vector at time {@code t} from epoch.
     */
    public static Vector2D velocityAtTime(OrbitalElements elements, double t) {
        double e   = elements.eccentricity();
        double h   = elements.h();
        double mu  = elements.mu();
        double p   = elements.semiLatusRectum();
        double omega = elements.periapsisAngle();

        double f = trueAnomalyAtTime(elements, t);

        // Radial and tangential velocity components in orbital frame
        // vr = (μ/h) · e·sin(f)
        // vt = (μ/h) · (1 + e·cos(f))
        double vr = (mu / h) * e * Math.sin(f);
        double vt = (mu / h) * (1.0 + e * Math.cos(f));

        // Convert to Cartesian with periapsis rotation
        double angle = f + omega;
        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);
        // radial unit vector: (cos(f+ω), sin(f+ω))
        // tangential unit vector: (-sin(f+ω), cos(f+ω))
        return new Vector2D(
            vr * cosA - vt * sinA,
            vr * sinA + vt * cosA
        );
    }

    /**
     * Computes the true anomaly at elapsed time {@code t} from epoch.
     */
    public static double trueAnomalyAtTime(OrbitalElements elements, double t) {
        double e  = elements.eccentricity();
        double f0 = elements.trueAnomalyAtEpoch();

        return switch (elements.orbitType()) {
            case CIRCULAR   -> trueAnomalyCircular(elements, t, f0);
            case ELLIPTICAL -> trueAnomalyElliptical(elements, t);
            case PARABOLIC  -> trueAnomalyParabolic(elements, t);
            case HYPERBOLIC -> trueAnomalyHyperbolic(elements, t);
        };
    }

    // -------------------------------------------------------------------------
    // Orbit-type-specific anomaly solvers
    // -------------------------------------------------------------------------

    private static double trueAnomalyCircular(OrbitalElements el, double t, double f0) {
        // Angular velocity n = sqrt(μ/a³) = h/r²; since circular r = a = p
        double a = el.semiMajorAxis();
        double n = Math.sqrt(el.mu() / (a * a * a));
        return f0 + n * t;
    }

    /**
     * Elliptical: Solve Kepler's equation M = E - e·sin(E) via Newton iteration.
     * Convert eccentric anomaly E → true anomaly f.
     */
    private static double trueAnomalyElliptical(OrbitalElements el, double t) {
        double e  = el.eccentricity();
        double a  = el.semiMajorAxis();
        double T  = el.period();
        double f0 = el.trueAnomalyAtEpoch();

        // Mean motion and epoch offset
        double n = 2.0 * Math.PI / T;

        // Compute eccentric anomaly at epoch from true anomaly
        double E0 = trueToEccentric(f0, e);
        double M0 = E0 - e * Math.sin(E0);

        // Mean anomaly at time t
        double M = M0 + n * t;

        // Solve Kepler's equation for E
        double E = solveKeplerElliptical(M, e);

        // Eccentric → true anomaly
        return eccentricToTrue(E, e);
    }

    /**
     * Parabolic: Barker's equation D + D³/3 = W.
     */
    private static double trueAnomalyParabolic(OrbitalElements el, double t) {
        double h  = el.h();
        double mu = el.mu();
        double p  = el.semiLatusRectum();
        double f0 = el.trueAnomalyAtEpoch();

        // D (parabolic anomaly) at epoch
        double D0 = Math.tan(f0 / 2.0);
        double W0 = D0 + D0 * D0 * D0 / 3.0;

        // W at time t: W = W0 + sqrt(μ/2) · (2/p)^(3/2) · t
        double W = W0 + Math.sqrt(mu / 2.0) * Math.pow(2.0 / p, 1.5) * t;

        // Solve cubic: D + D³/3 = W (Barker's equation)
        // Substitution: D = 2·cot(2θ), then real root via Cardano
        double D = solveBarker(W);
        return 2.0 * Math.atan(D);
    }

    /**
     * Hyperbolic: Solve Kepler's hyperbolic equation M_h = e·sinh(F) − F.
     */
    private static double trueAnomalyHyperbolic(OrbitalElements el, double t) {
        double e  = el.eccentricity();
        double a  = Math.abs(el.semiMajorAxis());  // positive convention
        double mu = el.mu();
        double f0 = el.trueAnomalyAtEpoch();

        // Hyperbolic anomaly at epoch
        double F0 = trueToHyperbolic(f0, e);
        double Mh0 = e * Math.sinh(F0) - F0;

        // Hyperbolic mean motion
        double nh = Math.sqrt(mu / (a * a * a));
        double Mh = Mh0 + nh * t;

        // Solve hyperbolic Kepler's equation
        double F = solveKeplerHyperbolic(Mh, e);

        return hyperbolicToTrue(F, e);
    }

    // -------------------------------------------------------------------------
    // Anomaly conversion utilities
    // -------------------------------------------------------------------------

    /** True anomaly → eccentric anomaly (ellipse). */
    private static double trueToEccentric(double f, double e) {
        return 2.0 * Math.atan(Math.sqrt((1.0 - e) / (1.0 + e)) * Math.tan(f / 2.0));
    }

    /** Eccentric anomaly → true anomaly (ellipse). */
    private static double eccentricToTrue(double E, double e) {
        return 2.0 * Math.atan2(
            Math.sqrt(1.0 + e) * Math.sin(E / 2.0),
            Math.sqrt(1.0 - e) * Math.cos(E / 2.0)
        );
    }

    /** True anomaly → hyperbolic anomaly. */
    private static double trueToHyperbolic(double f, double e) {
        // cosh(F) = (e + cos(f)) / (1 + e·cos(f))  [but use atanh form for sign]
        double x = Math.sqrt((e - 1.0) / (e + 1.0)) * Math.tan(f / 2.0);
        return 2.0 * atanh(x);
    }

    /** Hyperbolic anomaly → true anomaly. */
    private static double hyperbolicToTrue(double F, double e) {
        return 2.0 * Math.atan(Math.sqrt((e + 1.0) / (e - 1.0)) * Math.tanh(F / 2.0));
    }

    /** Inverse hyperbolic tangent. */
    private static double atanh(double x) {
        if (x >= 1.0)  return Double.POSITIVE_INFINITY;
        if (x <= -1.0) return Double.NEGATIVE_INFINITY;
        return 0.5 * Math.log((1.0 + x) / (1.0 - x));
    }

    // -------------------------------------------------------------------------
    // Iterative solvers
    // -------------------------------------------------------------------------

    /** Solve M = E - e·sin(E) for E using Newton–Raphson. */
    private static double solveKeplerElliptical(double M, double e) {
        // Normalize M to [−π, π]
        M = M % (2 * Math.PI);
        if (M > Math.PI)  M -= 2 * Math.PI;
        if (M < -Math.PI) M += 2 * Math.PI;

        double E = M + e * Math.sin(M);  // first-order initial guess
        for (int i = 0; i < MAX_ITER; i++) {
            double f  = E - e * Math.sin(E) - M;
            double fp = 1.0 - e * Math.cos(E);
            double dE = f / fp;
            E -= dE;
            if (Math.abs(dE) < TOL) break;
        }
        return E;
    }

    /** Solve Barker's equation D + D³/3 = W for D. Analytic via Cardano. */
    private static double solveBarker(double W) {
        // Substitution W = 3·W, cubic t³ + 3t − 2W' = 0 doesn't work cleanly.
        // Use standard Cardano on D³ + 3D − 3W = 0:
        // discriminant = 1 + (W/2)²  > 0 always → one real root
        double sqrtTerm = Math.sqrt(1.0 + W * W);
        return Math.cbrt(W + sqrtTerm) - Math.cbrt(-W + sqrtTerm);
    }

    /** Solve e·sinh(F) − F = M_h for F using Newton–Raphson. */
    private static double solveKeplerHyperbolic(double Mh, double e) {
        double F = Math.log(2.0 * Math.abs(Mh) / e + 1.8);  // initial guess
        if (Mh < 0) F = -F;
        for (int i = 0; i < MAX_ITER; i++) {
            double f  = e * Math.sinh(F) - F - Mh;
            double fp = e * Math.cosh(F) - 1.0;
            double dF = f / fp;
            F -= dF;
            if (Math.abs(dF) < TOL) break;
        }
        return F;
    }

    // -------------------------------------------------------------------------
    // Convenience: position of each body in simulation coordinates
    // -------------------------------------------------------------------------

    /**
     * Given the relative position at time t and the center-of-mass position,
     * returns [pos1, pos2] in world coordinates.
     *
     * @param com    center of mass position (fixed for isolated two-body)
     * @param relPos relative position (body2 relative to body1) at time t
     * @param m1     mass of body 1
     * @param m2     mass of body 2
     */
    public static Vector2D[] absolutePositions(Vector2D com, Vector2D relPos, double m1, double m2) {
        double totalMass = m1 + m2;
        // body1 is at com − (m2/M)·relPos; body2 is at com + (m1/M)·relPos
        Vector2D pos1 = com.sub(relPos.scale(m2 / totalMass));
        Vector2D pos2 = com.add(relPos.scale(m1 / totalMass));
        return new Vector2D[]{pos1, pos2};
    }
}
