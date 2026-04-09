package com.orbitaldynamics.math;

/**
 * Immutable 2D vector with common linear algebra operations.
 */
public record Vector2D(double x, double y) {

    public static final Vector2D ZERO = new Vector2D(0, 0);

    public Vector2D add(Vector2D o)      { return new Vector2D(x + o.x, y + o.y); }
    public Vector2D sub(Vector2D o)      { return new Vector2D(x - o.x, y - o.y); }
    public Vector2D scale(double s)      { return new Vector2D(x * s, y * s); }
    public Vector2D negate()             { return new Vector2D(-x, -y); }

    /** Dot product. */
    public double dot(Vector2D o)        { return x * o.x + y * o.y; }

    /** Z-component of the cross product (signed area). */
    public double cross(Vector2D o)      { return x * o.y - y * o.x; }

    public double magnitudeSq()          { return x * x + y * y; }
    public double magnitude()            { return Math.sqrt(magnitudeSq()); }

    public Vector2D normalize() {
        double m = magnitude();
        return m < 1e-15 ? ZERO : scale(1.0 / m);
    }

    /** Returns the vector rotated 90° counter-clockwise. */
    public Vector2D perpendicular()      { return new Vector2D(-y, x); }

    public Vector2D rotate(double angle) {
        double c = Math.cos(angle), s = Math.sin(angle);
        return new Vector2D(x * c - y * s, x * s + y * c);
    }

    /** Distance to another vector (treating both as points). */
    public double distanceTo(Vector2D o) { return sub(o).magnitude(); }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}
