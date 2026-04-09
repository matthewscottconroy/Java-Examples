package com.orbitaldynamics.twobody;

/**
 * Classical orbital elements derived from a two-body initial condition.
 *
 * <h2>Orbital Mechanics Summary</h2>
 * <p>Given initial position {@code r} and velocity {@code v} for two bodies with
 * combined gravitational parameter {@code μ = G(m1+m2)}, the orbital elements are:
 * <ul>
 *   <li><b>h</b>: specific angular momentum = r × v (z-component in 2D)</li>
 *   <li><b>e</b>: eccentricity — from the eccentricity vector; determines orbit shape</li>
 *   <li><b>a</b>: semi-major axis — from energy: a = −μ/(2E) for bound orbits</li>
 *   <li><b>ω</b>: argument of periapsis — angle from reference direction to periapsis</li>
 *   <li><b>T</b>: orbital period — from Kepler's third law: T² = 4π²a³/μ</li>
 * </ul>
 *
 * <h2>Orbit Types by Eccentricity</h2>
 * <ul>
 *   <li>e = 0: circle</li>
 *   <li>0 &lt; e &lt; 1: ellipse (bound orbit)</li>
 *   <li>e = 1: parabola (escape at minimum energy)</li>
 *   <li>e &gt; 1: hyperbola (unbound flyby)</li>
 * </ul>
 */
public record OrbitalElements(
    /** Reduced mass gravitational parameter μ = G(m₁+m₂). */
    double mu,

    /** Specific angular momentum (z-component). */
    double h,

    /** Eccentricity (0 = circle, <1 = ellipse, 1 = parabola, >1 = hyperbola). */
    double eccentricity,

    /** Semi-major axis in pixels. Positive for ellipse, negative for hyperbola. */
    double semiMajorAxis,

    /** Argument of periapsis in radians (angle from +x axis to periapsis direction). */
    double periapsisAngle,

    /** True anomaly at t=0 in radians. */
    double trueAnomalyAtEpoch,

    /** Specific orbital energy: E = v²/2 − μ/r. */
    double specificEnergy,

    /** Orbital period in simulation seconds. NaN for unbound orbits. */
    double period,

    /** Periapsis distance in pixels. */
    double periapsis,

    /** Apoapsis distance. Positive infinity for unbound orbits. */
    double apoapsis,

    /** Classification of the orbit. */
    OrbitType orbitType
) {
    public enum OrbitType {
        CIRCULAR("Circular (e≈0)"),
        ELLIPTICAL("Elliptical (0<e<1)"),
        PARABOLIC("Parabolic (e≈1)"),
        HYPERBOLIC("Hyperbolic (e>1)");

        public final String label;
        OrbitType(String label) { this.label = label; }
    }

    /** True if the orbit is bound (will return to periapsis). */
    public boolean isBound() {
        return orbitType == OrbitType.CIRCULAR || orbitType == OrbitType.ELLIPTICAL;
    }

    /** Semi-latus rectum: p = h²/μ. */
    public double semiLatusRectum() {
        return h * h / mu;
    }
}
