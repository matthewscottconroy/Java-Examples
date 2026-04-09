package com.wattsstrogatz.model;

/**
 * Immutable configuration for a Watts-Strogatz simulation run.
 *
 * <p>Parameters map directly to the model introduced in:
 * <blockquote>Watts, D.J. &amp; Strogatz, S.H. (1998). "Collective dynamics of
 * 'small-world' networks." <i>Nature</i>, 393, 440–442.</blockquote>
 *
 * <table border="1">
 *   <caption>Parameter summary</caption>
 *   <tr><th>Symbol</th><th>Meaning</th><th>Constraint</th></tr>
 *   <tr><td>n</td><td>number of nodes</td><td>n ≥ 4 and n &gt; 2k</td></tr>
 *   <tr><td>k</td><td>each node connects to k nearest neighbours on each side</td>
 *       <td>k ≥ 1</td></tr>
 *   <tr><td>p</td><td>probability each edge is rewired (0=regular, 1=random)</td>
 *       <td>0 ≤ p ≤ 1</td></tr>
 * </table>
 *
 * <p>Build with the nested {@link Builder}; individual setter methods validate
 * their argument immediately, and {@link Builder#build()} performs the cross-
 * parameter check {@code n > 2k}:
 * <pre>{@code
 * NetworkConfig cfg = new NetworkConfig.Builder()
 *     .nodeCount(100).k(4).rewiringProbability(0.1).build();
 * }</pre>
 *
 * @see com.wattsstrogatz.simulation.WattsStrogatzSimulation
 */
public final class NetworkConfig {

    public static final int    DEFAULT_NODE_COUNT           = 60;
    public static final int    DEFAULT_K                    = 3;
    public static final double DEFAULT_REWIRING_PROBABILITY = 0.05;
    public static final long   DEFAULT_RANDOM_SEED          = 42L;

    private final int    nodeCount;
    private final int    k;
    private final double rewiringProbability;
    private final long   randomSeed;

    private NetworkConfig(Builder b) {
        this.nodeCount           = b.nodeCount;
        this.k                   = b.k;
        this.rewiringProbability = b.rewiringProbability;
        this.randomSeed          = b.randomSeed;
    }

    /** @return a config with all default values */
    public static NetworkConfig defaults() { return new Builder().build(); }

    /** @return number of nodes n */
    public int getNodeCount() { return nodeCount; }

    /** @return neighbourhood half-degree k */
    public int getK() { return k; }

    /** @return rewiring probability p in [0, 1] */
    public double getRewiringProbability() { return rewiringProbability; }

    /** @return random seed */
    public long getRandomSeed() { return randomSeed; }

    /** @return total edges in the initial ring lattice (n*k) */
    public int getTotalEdges() { return nodeCount * k; }

    /** Fluent builder for {@link NetworkConfig}. */
    public static final class Builder {
        private int    nodeCount           = DEFAULT_NODE_COUNT;
        private int    k                   = DEFAULT_K;
        private double rewiringProbability = DEFAULT_REWIRING_PROBABILITY;
        private long   randomSeed          = DEFAULT_RANDOM_SEED;

        /**
         * Sets the number of nodes.
         *
         * @param n node count; must be &ge; 4 (also must satisfy n &gt; 2k at
         *          {@link #build()} time)
         * @return this builder
         * @throws IllegalArgumentException if n &lt; 4
         */
        public Builder nodeCount(int n) {
            if (n < 4) throw new IllegalArgumentException("nodeCount must be >= 4");
            this.nodeCount = n; return this;
        }

        /**
         * Sets the neighbourhood half-degree.  Each node is connected to its k
         * nearest neighbours on each side, giving an initial degree of 2k.
         *
         * @param k half-degree; must be &ge; 1
         * @return this builder
         * @throws IllegalArgumentException if k &lt; 1
         */
        public Builder k(int k) {
            if (k < 1) throw new IllegalArgumentException("k must be >= 1");
            this.k = k; return this;
        }

        /**
         * Sets the rewiring probability per edge.
         *
         * @param p probability in [0, 1]; 0 = regular ring lattice,
         *          1 = fully random Erdős–Rényi graph
         * @return this builder
         * @throws IllegalArgumentException if p &lt; 0 or p &gt; 1
         */
        public Builder rewiringProbability(double p) {
            if (p < 0.0 || p > 1.0)
                throw new IllegalArgumentException("rewiringProbability must be in [0, 1]");
            this.rewiringProbability = p; return this;
        }

        /**
         * Sets the random seed used during rewiring.  Fixing the seed makes runs
         * fully reproducible.
         *
         * @param seed any long value
         * @return this builder
         */
        public Builder randomSeed(long seed) { this.randomSeed = seed; return this; }

        /**
         * Validates all parameters and constructs the immutable
         * {@link NetworkConfig}.
         *
         * @return new config
         * @throws IllegalArgumentException if {@code nodeCount <= 2k}
         */
        public NetworkConfig build() {
            if (nodeCount <= 2 * k)
                throw new IllegalArgumentException(
                    "nodeCount (" + nodeCount + ") must be > 2k (" + 2 * k + ")");
            return new NetworkConfig(this);
        }
    }

    @Override
    public String toString() {
        return String.format("NetworkConfig{n=%d, k=%d, p=%.3f, seed=%d}",
            nodeCount, k, rewiringProbability, randomSeed);
    }
}
