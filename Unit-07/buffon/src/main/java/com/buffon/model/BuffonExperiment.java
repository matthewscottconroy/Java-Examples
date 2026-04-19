package com.buffon.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * The Buffon's Needle experiment: drop needles onto a ruled floor and
 * estimate π from the crossing frequency.
 *
 * <h2>Formula (L ≤ d)</h2>
 * <p>The probability that a needle of length L crosses one of the parallel
 * lines spaced d apart is:
 * <pre>
 *   P = 2L / (π d)
 * </pre>
 * Solving for π gives the Monte Carlo estimator:
 * <pre>
 *   π̂ = 2 L N / (d C)
 * </pre>
 * where N is the total number of drops and C is the number of crossings.
 * By the law of large numbers, π̂ → π as N → ∞.
 *
 * <h2>Convergence rate</h2>
 * <p>The standard error of π̂ is proportional to 1/√N.  To gain one
 * additional decimal place of accuracy, roughly 100× more trials are needed.
 * This makes Buffon's method a very slow Monte Carlo estimator in practice,
 * though it is historically important as one of the earliest probabilistic
 * arguments for the value of π.
 *
 * <h2>Long needles (L &gt; d)</h2>
 * <p>When L &gt; d the simple formula above no longer holds; the correct
 * expression involves an arccosine term.  This class still correctly detects
 * crossings for L &gt; d via the floor-division check, but the π estimator
 * is only valid when L ≤ d.  The UI indicates when this condition is violated.
 */
public class BuffonExperiment {

    /** Maximum number of needles retained for on-screen rendering. */
    public static final int MAX_DISPLAY = 4000;

    /** Maximum number of history points kept for the convergence graph. */
    public static final int MAX_HISTORY = 800;

    private final Random rng;
    private final Deque<Needle> displayNeedles = new ArrayDeque<>(MAX_DISPLAY);
    private final List<Double>  piHistory      = new ArrayList<>(MAX_HISTORY);

    private long   totalDrops;
    private long   crossings;
    private double lineSpacing;
    private double needleLength;

    /**
     * Create a new experiment with the given geometry.
     *
     * @param lineSpacing  distance between parallel floor lines (pixels; &gt; 0)
     * @param needleLength needle length (pixels; &gt; 0)
     */
    public BuffonExperiment(double lineSpacing, double needleLength) {
        this.lineSpacing  = lineSpacing;
        this.needleLength = needleLength;
        this.rng          = new Random();
    }

    /**
     * Create a new experiment with a fixed seed (for reproducible testing).
     *
     * @param lineSpacing  distance between parallel floor lines (pixels)
     * @param needleLength needle length (pixels)
     * @param seed         RNG seed
     */
    public BuffonExperiment(double lineSpacing, double needleLength, long seed) {
        this.lineSpacing  = lineSpacing;
        this.needleLength = needleLength;
        this.rng          = new Random(seed);
    }

    // -------------------------------------------------------------------------
    // Dropping needles
    // -------------------------------------------------------------------------

    /**
     * Drop {@code count} needles uniformly at random within the given area and
     * record their statistics.
     *
     * @param count     number of needles to drop
     * @param areaWidth  width of the drop area (pixels)
     * @param areaHeight height of the drop area (pixels)
     */
    public void drop(int count, double areaWidth, double areaHeight) {
        for (int i = 0; i < count; i++) {
            double cx    = rng.nextDouble() * areaWidth;
            double cy    = rng.nextDouble() * areaHeight;
            double angle = rng.nextDouble() * Math.PI;
            boolean cross = crosses(cy, angle);

            Needle needle = new Needle(cx, cy, angle, needleLength, cross);
            if (displayNeedles.size() >= MAX_DISPLAY) displayNeedles.pollFirst();
            displayNeedles.addLast(needle);

            totalDrops++;
            if (cross) crossings++;
        }
        recordHistory();
    }

    /**
     * Determine whether a needle centred at height {@code cy} with orientation
     * {@code angle} crosses one of the horizontal floor lines.
     *
     * <p>The vertical half-span of the needle is {@code (L/2)|sin θ|}.
     * A line crossing occurs when yMin and yMax straddle a multiple of d:
     * <pre>  ⌊yMin / d⌋ ≠ ⌊yMax / d⌋</pre>
     *
     * @param cy    y-coordinate of needle centre (pixels)
     * @param angle needle angle from horizontal (radians)
     * @return {@code true} if the needle crosses a line
     */
    public boolean crosses(double cy, double angle) {
        double halfSpan = 0.5 * needleLength * Math.abs(Math.sin(angle));
        double yMin = cy - halfSpan;
        double yMax = cy + halfSpan;
        return (long) Math.floor(yMin / lineSpacing) != (long) Math.floor(yMax / lineSpacing);
    }

    private void recordHistory() {
        if (totalDrops == 0) return;
        // Record first 100 drops individually, then every 50, and every 1000 at scale.
        boolean record = totalDrops <= 100
                      || (totalDrops <= 5_000  && totalDrops % 50   == 0)
                      || (totalDrops <= 50_000 && totalDrops % 500  == 0)
                      || totalDrops % 5_000 == 0;
        if (!record) return;

        if (piHistory.size() >= MAX_HISTORY) piHistory.remove(0);
        piHistory.add(estimatePi());
    }

    // -------------------------------------------------------------------------
    // Reset / reconfigure
    // -------------------------------------------------------------------------

    /** Clear all dropped needles and reset statistics. */
    public void reset() {
        displayNeedles.clear();
        piHistory.clear();
        totalDrops = 0;
        crossings  = 0;
    }

    /**
     * Change the line spacing and reset the experiment.
     *
     * @param d new line spacing (pixels; must be &gt; 0)
     */
    public void setLineSpacing(double d) {
        this.lineSpacing = d;
        reset();
    }

    /**
     * Change the needle length and reset the experiment.
     *
     * @param l new needle length (pixels; must be &gt; 0)
     */
    public void setNeedleLength(double l) {
        this.needleLength = l;
        reset();
    }

    // -------------------------------------------------------------------------
    // Statistics
    // -------------------------------------------------------------------------

    /**
     * Current Monte Carlo estimate of π using the formula π̂ = 2LN / (dC).
     *
     * @return estimated π, or {@link Double#NaN} if no crossings have occurred yet
     */
    public double estimatePi() {
        if (crossings == 0) return Double.NaN;
        return (2.0 * needleLength * totalDrops) / (lineSpacing * crossings);
    }

    /**
     * Theoretical crossing probability for the current geometry: P = 2L/(πd).
     *
     * <p>Only valid when {@code needleLength ≤ lineSpacing}.
     */
    public double theoreticalCrossingProbability() {
        return (2.0 * needleLength) / (Math.PI * lineSpacing);
    }

    /**
     * Observed crossing probability: C / N.
     *
     * @return observed probability, or 0 if no drops have been made
     */
    public double observedCrossingProbability() {
        return totalDrops == 0 ? 0.0 : (double) crossings / totalDrops;
    }

    /** {@code true} when needle length exceeds line spacing (simple formula breaks down). */
    public boolean isLongNeedle() { return needleLength > lineSpacing; }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public long   getTotalDrops()                  { return totalDrops; }
    public long   getCrossings()                   { return crossings; }
    public double getLineSpacing()                 { return lineSpacing; }
    public double getNeedleLength()                { return needleLength; }
    public Deque<Needle>  getDisplayNeedles()      { return displayNeedles; }
    public List<Double>   getPiHistory()           { return Collections.unmodifiableList(piHistory); }
}
