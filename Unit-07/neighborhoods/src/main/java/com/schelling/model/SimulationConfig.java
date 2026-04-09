package com.schelling.model;

/**
 * Immutable configuration for a Schelling segregation simulation run.
 *
 * <p>Use the nested {@link Builder} to construct instances:
 * <pre>{@code
 * SimulationConfig config = new SimulationConfig.Builder()
 *     .rows(40).cols(40)
 *     .thresholdA(0.40).thresholdB(0.30)
 *     .emptyFraction(0.15)
 *     .neighborhoodType(NeighborhoodType.MOORE)
 *     .build();
 * }</pre>
 *
 * <p>For backward compatibility, {@link Builder#satisfactionThreshold(double)} sets
 * both {@code thresholdA} and {@code thresholdB} to the same value, and
 * {@link #getSatisfactionThreshold()} returns {@code thresholdA}.
 */
public final class SimulationConfig {

    // -------------------------------------------------------------------------
    // Defaults
    // -------------------------------------------------------------------------

    public static final int               DEFAULT_ROWS                  = 50;
    public static final int               DEFAULT_COLS                  = 50;
    public static final double            DEFAULT_SATISFACTION_THRESHOLD = 0.33;
    public static final double            DEFAULT_THRESHOLD_A            = 0.33;
    public static final double            DEFAULT_THRESHOLD_B            = 0.33;
    public static final double            DEFAULT_EMPTY_FRACTION         = 0.15;
    public static final double            DEFAULT_TYPE_B_FRACTION        = 0.50;
    public static final long              DEFAULT_RANDOM_SEED            = 42L;
    public static final NeighborhoodType  DEFAULT_NEIGHBORHOOD_TYPE      = NeighborhoodType.MOORE;
    public static final InitialCondition  DEFAULT_INITIAL_CONDITION      = InitialCondition.RANDOM;

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final int              rows;
    private final int              cols;
    private final double           thresholdA;
    private final double           thresholdB;
    private final double           emptyFraction;
    private final double           typeBFraction;
    private final long             randomSeed;
    private final NeighborhoodType neighborhoodType;
    private final InitialCondition initialCondition;

    // -------------------------------------------------------------------------
    // Constructor (private — use Builder)
    // -------------------------------------------------------------------------

    private SimulationConfig(Builder b) {
        this.rows             = b.rows;
        this.cols             = b.cols;
        this.thresholdA       = b.thresholdA;
        this.thresholdB       = b.thresholdB;
        this.emptyFraction    = b.emptyFraction;
        this.typeBFraction    = b.typeBFraction;
        this.randomSeed       = b.randomSeed;
        this.neighborhoodType = b.neighborhoodType;
        this.initialCondition = b.initialCondition;
    }

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    /** Returns a config populated entirely with default values. */
    public static SimulationConfig defaults() {
        return new Builder().build();
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public int              getRows()             { return rows; }
    public int              getCols()             { return cols; }
    public double           getThresholdA()       { return thresholdA; }
    public double           getThresholdB()       { return thresholdB; }
    public double           getEmptyFraction()    { return emptyFraction; }
    public double           getTypeBFraction()    { return typeBFraction; }
    public long             getRandomSeed()       { return randomSeed; }
    public NeighborhoodType getNeighborhoodType() { return neighborhoodType; }
    public InitialCondition getInitialCondition() { return initialCondition; }

    /** Backward-compatible alias for {@link #getThresholdA()}. */
    public double getSatisfactionThreshold() { return thresholdA; }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    /** Fluent builder for {@link SimulationConfig}. */
    public static final class Builder {

        private int              rows             = DEFAULT_ROWS;
        private int              cols             = DEFAULT_COLS;
        private double           thresholdA       = DEFAULT_THRESHOLD_A;
        private double           thresholdB       = DEFAULT_THRESHOLD_B;
        private double           emptyFraction    = DEFAULT_EMPTY_FRACTION;
        private double           typeBFraction    = DEFAULT_TYPE_B_FRACTION;
        private long             randomSeed       = DEFAULT_RANDOM_SEED;
        private NeighborhoodType neighborhoodType = DEFAULT_NEIGHBORHOOD_TYPE;
        private InitialCondition initialCondition = DEFAULT_INITIAL_CONDITION;

        public Builder rows(int rows) {
            if (rows < 2) throw new IllegalArgumentException("rows must be >= 2");
            this.rows = rows;
            return this;
        }

        public Builder cols(int cols) {
            if (cols < 2) throw new IllegalArgumentException("cols must be >= 2");
            this.cols = cols;
            return this;
        }

        /** Sets both thresholdA and thresholdB to the same value. */
        public Builder satisfactionThreshold(double threshold) {
            if (threshold < 0.0 || threshold > 1.0)
                throw new IllegalArgumentException("threshold must be in [0, 1]");
            this.thresholdA = threshold;
            this.thresholdB = threshold;
            return this;
        }

        public Builder thresholdA(double a) {
            if (a < 0.0 || a > 1.0)
                throw new IllegalArgumentException("thresholdA must be in [0, 1]");
            this.thresholdA = a;
            return this;
        }

        public Builder thresholdB(double b) {
            if (b < 0.0 || b > 1.0)
                throw new IllegalArgumentException("thresholdB must be in [0, 1]");
            this.thresholdB = b;
            return this;
        }

        public Builder emptyFraction(double fraction) {
            if (fraction < 0.0 || fraction >= 1.0)
                throw new IllegalArgumentException("emptyFraction must be in [0, 1)");
            this.emptyFraction = fraction;
            return this;
        }

        public Builder typeBFraction(double fraction) {
            if (fraction < 0.0 || fraction > 1.0)
                throw new IllegalArgumentException("typeBFraction must be in [0, 1]");
            this.typeBFraction = fraction;
            return this;
        }

        public Builder randomSeed(long seed) {
            this.randomSeed = seed;
            return this;
        }

        public Builder neighborhoodType(NeighborhoodType nt) {
            if (nt == null) throw new NullPointerException("neighborhoodType must not be null");
            this.neighborhoodType = nt;
            return this;
        }

        public Builder initialCondition(InitialCondition ic) {
            if (ic == null) throw new NullPointerException("initialCondition must not be null");
            this.initialCondition = ic;
            return this;
        }

        public SimulationConfig build() {
            return new SimulationConfig(this);
        }
    }

    @Override
    public String toString() {
        return String.format(
            "SimulationConfig{%dx%d, tA=%.2f, tB=%.2f, empty=%.2f, typeBFrac=%.2f, %s, %s, seed=%d}",
            rows, cols, thresholdA, thresholdB, emptyFraction, typeBFraction,
            neighborhoodType, initialCondition, randomSeed);
    }
}
