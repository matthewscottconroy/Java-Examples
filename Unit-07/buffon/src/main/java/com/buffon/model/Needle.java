package com.buffon.model;

/**
 * An immutable snapshot of a single dropped needle.
 *
 * <h2>Geometry</h2>
 * <p>The needle is placed with its centre at {@code (cx, cy)} and oriented at
 * angle {@code angle} (radians from horizontal, in [0, π)).  Its two endpoints
 * are computed from these values and the needle's {@code length}.</p>
 *
 * <h2>Crossing test</h2>
 * <p>Given horizontal parallel lines spaced {@code d} apart, the needle crosses
 * a line whenever the vertical span of the needle straddles at least one of them:
 * <pre>
 *   yMin = cy − (L/2)|sin θ|
 *   yMax = cy + (L/2)|sin θ|
 *   crosses ⟺ ⌊yMin / d⌋ ≠ ⌊yMax / d⌋
 * </pre>
 * The {@link #crosses} flag is computed by {@link BuffonExperiment} and stored
 * here for rendering convenience.
 *
 * @param cx      x-coordinate of the needle centre (pixels)
 * @param cy      y-coordinate of the needle centre (pixels)
 * @param angle   orientation angle from horizontal (radians, in [0, π))
 * @param length  needle length (pixels)
 * @param crosses whether this needle crosses a floor line
 */
public record Needle(double cx, double cy, double angle, double length, boolean crosses) {

    /** Left/upper endpoint x-coordinate. */
    public double x1() { return cx - length * 0.5 * Math.cos(angle); }

    /** Left/upper endpoint y-coordinate. */
    public double y1() { return cy - length * 0.5 * Math.sin(angle); }

    /** Right/lower endpoint x-coordinate. */
    public double x2() { return cx + length * 0.5 * Math.cos(angle); }

    /** Right/lower endpoint y-coordinate. */
    public double y2() { return cy + length * 0.5 * Math.sin(angle); }
}
