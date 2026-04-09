package com.markovmonopoly.core;

import java.util.*;

/**
 * Static utility class for analyzing Markov chains.
 *
 * <p>Provides algorithms for:
 * <ul>
 *   <li>Stationary distribution (long-run frequencies)</li>
 *   <li>Multi-step transition probabilities</li>
 *   <li>Mean first passage times (MFPT)</li>
 *   <li>Absorbing chain analysis (absorption probabilities, expected absorption time)</li>
 *   <li>State classification (transient, recurrent, absorbing)</li>
 *   <li>Communication classes (strongly connected components)</li>
 *   <li>Periodicity and ergodicity</li>
 * </ul>
 *
 * <p>All methods are stateless (pure functions) — they take a {@link MarkovChain}
 * and return results without modifying any state.
 */
public final class MarkovAnalysis {

    private MarkovAnalysis() {}

    // =========================================================================
    // Stationary distribution
    // =========================================================================

    /**
     * Computes the stationary (steady-state) distribution using power iteration.
     *
     * <p>The stationary distribution {@code π} satisfies {@code π·P = π} and
     * {@code Σπ[i] = 1}. It represents the long-run fraction of time the chain
     * spends in each state, regardless of starting state (for ergodic chains).
     */
    public static double[] stationaryDistribution(MarkovChain chain) {
        return chain.getMatrix().stationaryDistribution();
    }

    // =========================================================================
    // Multi-step analysis
    // =========================================================================

    /**
     * Returns the n-step transition matrix P^n.
     *
     * <p>Entry [i][j] is the probability of being in state j after exactly n steps,
     * starting from state i. Uses repeated squaring for O(size³ log n) performance.
     */
    public static TransitionMatrix nStepMatrix(MarkovChain chain, int n) {
        return chain.getMatrix().power(n);
    }

    /**
     * Computes the probability distribution over states after {@code steps} transitions,
     * given an initial distribution {@code initialDist}.
     *
     * <p>The result is the vector {@code initialDist * P^steps}.
     *
     * @param initialDist probability vector (must sum to 1 and be non-negative)
     * @param steps       number of time steps
     * @return updated probability distribution
     */
    public static double[] distributionAfterSteps(MarkovChain chain,
                                                   double[] initialDist, int steps) {
        double[] dist = Arrays.copyOf(initialDist, initialDist.length);
        TransitionMatrix p = chain.getMatrix();
        for (int i = 0; i < steps; i++) {
            dist = p.leftMultiply(dist);
        }
        return dist;
    }

    /**
     * Returns the distribution after {@code steps} steps, starting deterministically
     * in the given state (initial distribution is a unit vector).
     */
    public static double[] distributionAfterSteps(MarkovChain chain, int startState, int steps) {
        double[] initial = new double[chain.size()];
        initial[startState] = 1.0;
        return distributionAfterSteps(chain, initial, steps);
    }

    // =========================================================================
    // Convergence
    // =========================================================================

    /**
     * Returns the total variation distance between two probability distributions.
     *
     * <p>TV distance = (1/2) * Σ|p[i] - q[i]|. It ranges from 0 (identical) to 1
     * (disjoint supports). Used to measure how quickly a chain converges to its
     * stationary distribution.
     */
    public static double totalVariationDistance(double[] p, double[] q) {
        double sum = 0;
        for (int i = 0; i < p.length; i++) sum += Math.abs(p[i] - q[i]);
        return sum / 2.0;
    }

    /**
     * Computes the TV distance from the stationary distribution at each step,
     * starting from the given state.
     *
     * @param maxSteps maximum number of steps to profile
     * @return array of TV distances, index = number of steps taken
     */
    public static double[] convergenceProfile(MarkovChain chain, int startState, int maxSteps) {
        double[] stationary = stationaryDistribution(chain);
        double[] dist = new double[chain.size()];
        dist[startState] = 1.0;
        double[] profile = new double[maxSteps + 1];
        profile[0] = totalVariationDistance(dist, stationary);
        TransitionMatrix p = chain.getMatrix();
        for (int step = 1; step <= maxSteps; step++) {
            dist = p.leftMultiply(dist);
            profile[step] = totalVariationDistance(dist, stationary);
        }
        return profile;
    }

    // =========================================================================
    // Mean first passage times
    // =========================================================================

    /**
     * Computes the mean first passage time (MFPT) from every state to every other state.
     *
     * <p>The MFPT {@code m[i][j]} is the expected number of steps to reach state j
     * for the first time, starting from state i. For i = j, this is the mean
     * recurrence time (expected time to return to state i).
     *
     * <p>For ergodic chains, {@code m[i][i] = 1 / π[i]} where π is the stationary
     * distribution — states visited rarely take longer to return to.
     *
     * <p>MFPT satisfies the system: {@code m[i][j] = 1 + Σ_{k≠j} P[i][k] * m[k][j]}.
     * This method solves these equations via Gaussian elimination for each target j.
     *
     * <p>Returns {@link Double#POSITIVE_INFINITY} if state j is not reachable from i.
     */
    public static double[][] meanFirstPassageTimes(MarkovChain chain) {
        int n = chain.size();
        double[][] mfpt = new double[n][n];

        // For each target state j, solve for m[i][j], i ≠ j
        for (int j = 0; j < n; j++) {
            // Build (n-1) × (n-1) system: for i ≠ j:
            //   m[i][j] - Σ_{k≠j} P[i][k] * m[k][j] = 1
            // The variable ordering skips index j.
            int[] idx = new int[n - 1];  // maps reduced index -> original index
            int pos = 0;
            for (int i = 0; i < n; i++) if (i != j) idx[pos++] = i;

            double[][] A = new double[n - 1][n - 1];
            double[]   b = new double[n - 1];

            for (int r = 0; r < n - 1; r++) {
                int i = idx[r];
                b[r] = 1.0;
                for (int c = 0; c < n - 1; c++) {
                    int k = idx[c];
                    A[r][c] = (r == c ? 1.0 : 0.0) - chain.probability(i, k);
                }
            }

            double[] solution;
            try {
                solution = gaussianEliminate(A, b);
            } catch (SingularMatrixException e) {
                // Unreachable states have infinite MFPT
                solution = new double[n - 1];
                Arrays.fill(solution, Double.POSITIVE_INFINITY);
            }

            for (int r = 0; r < n - 1; r++) {
                mfpt[idx[r]][j] = solution[r];
            }
            // Mean recurrence time: m[j][j] = 1 / π[j]
            double[] pi = stationaryDistribution(chain);
            mfpt[j][j] = (pi[j] > 1e-15) ? 1.0 / pi[j] : Double.POSITIVE_INFINITY;
        }
        return mfpt;
    }

    // =========================================================================
    // Absorbing chain analysis
    // =========================================================================

    /**
     * Identifies absorbing states (states with P[i][i] = 1).
     *
     * @return list of state indices that are absorbing
     */
    public static List<Integer> absorbingStates(MarkovChain chain) {
        List<Integer> absorbing = new ArrayList<>();
        TransitionMatrix p = chain.getMatrix();
        for (int i = 0; i < chain.size(); i++) {
            if (Math.abs(p.get(i, i) - 1.0) < TransitionMatrix.ROW_SUM_TOLERANCE) {
                absorbing.add(i);
            }
        }
        return absorbing;
    }

    /**
     * Identifies transient (non-absorbing) states in an absorbing chain.
     *
     * @return list of state indices that are transient
     */
    public static List<Integer> transientStates(MarkovChain chain) {
        List<Integer> absorbing = absorbingStates(chain);
        List<Integer> transientList = new ArrayList<>();
        for (int i = 0; i < chain.size(); i++) {
            if (!absorbing.contains(i)) transientList.add(i);
        }
        return transientList;
    }

    /**
     * Computes the fundamental matrix {@code N = (I - Q)⁻¹} for an absorbing chain.
     *
     * <p>Q is the sub-matrix of transitions among transient states. N[i][j] gives
     * the expected number of times the chain visits transient state j before being
     * absorbed, starting from transient state i.
     *
     * @return fundamental matrix indexed by [transient state rank][transient state rank]
     */
    public static double[][] fundamentalMatrix(MarkovChain chain) {
        List<Integer> tStates = transientStates(chain);
        int t = tStates.size();
        if (t == 0) return new double[0][0];

        // Extract Q (transient → transient sub-matrix) and compute (I - Q)
        double[][] iMinusQ = new double[t][t];
        for (int r = 0; r < t; r++) {
            for (int c = 0; c < t; c++) {
                double q = chain.probability(tStates.get(r), tStates.get(c));
                iMinusQ[r][c] = (r == c ? 1.0 : 0.0) - q;
            }
        }

        // Invert (I - Q) by solving (I - Q) * N = I  →  N = (I - Q)⁻¹
        double[][] N = new double[t][t];
        for (int col = 0; col < t; col++) {
            double[] e = new double[t];
            e[col] = 1.0;
            try {
                double[] sol = gaussianEliminate(iMinusQ, e);
                for (int row = 0; row < t; row++) N[row][col] = sol[row];
            } catch (SingularMatrixException ex) {
                // Singular: some states may be in communicating loops — fill with NaN
                for (int row = 0; row < t; row++) N[row][col] = Double.NaN;
            }
        }
        return N;
    }

    /**
     * Computes the absorption probability matrix B for an absorbing chain.
     *
     * <p>B[i][j] is the probability that the chain is eventually absorbed in
     * absorbing state j, starting from transient state i. Computed as {@code B = N * R}
     * where N is the fundamental matrix and R is the transient→absorbing sub-matrix.
     *
     * @return matrix indexed by [transient state rank][absorbing state rank]
     */
    public static double[][] absorptionProbabilities(MarkovChain chain) {
        List<Integer> tStates = transientStates(chain);
        List<Integer> absorbing = absorbingStates(chain);
        int t = tStates.size(), a = absorbing.size();
        if (t == 0 || a == 0) return new double[0][0];

        double[][] N = fundamentalMatrix(chain);

        // Extract R (transient → absorbing sub-matrix)
        double[][] R = new double[t][a];
        for (int r = 0; r < t; r++) {
            for (int c = 0; c < a; c++) {
                R[r][c] = chain.probability(tStates.get(r), absorbing.get(c));
            }
        }

        // B = N * R
        double[][] B = new double[t][a];
        for (int i = 0; i < t; i++) {
            for (int j = 0; j < a; j++) {
                for (int k = 0; k < t; k++) {
                    B[i][j] += N[i][k] * R[k][j];
                }
            }
        }
        return B;
    }

    /**
     * Expected number of steps before absorption for each transient state.
     * Equal to the row sums of the fundamental matrix N.
     */
    public static double[] expectedStepsToAbsorption(MarkovChain chain) {
        double[][] N = fundamentalMatrix(chain);
        double[] expected = new double[N.length];
        for (int i = 0; i < N.length; i++) {
            for (double v : N[i]) expected[i] += v;
        }
        return expected;
    }

    // =========================================================================
    // State classification via Tarjan's SCC algorithm
    // =========================================================================

    /**
     * Finds all communication classes (strongly connected components) using
     * Tarjan's algorithm. States i and j communicate if there is a directed path
     * from i to j and from j to i.
     *
     * @return list of SCCs, each SCC is a list of state indices
     */
    public static List<List<Integer>> communicationClasses(MarkovChain chain) {
        int n = chain.size();
        int[] index   = new int[n];
        int[] lowlink = new int[n];
        boolean[] onStack = new boolean[n];
        Arrays.fill(index, -1);

        Deque<Integer> stack = new ArrayDeque<>();
        List<List<Integer>> sccs = new ArrayList<>();
        int[] indexCounter = {0};

        for (int v = 0; v < n; v++) {
            if (index[v] == -1) {
                tarjanDFS(v, chain, index, lowlink, onStack, stack, sccs, indexCounter);
            }
        }
        return sccs;
    }

    private static void tarjanDFS(int v, MarkovChain chain,
                                   int[] index, int[] lowlink, boolean[] onStack,
                                   Deque<Integer> stack, List<List<Integer>> sccs,
                                   int[] counter) {
        index[v] = lowlink[v] = counter[0]++;
        stack.push(v);
        onStack[v] = true;

        int n = chain.size();
        for (int w = 0; w < n; w++) {
            if (chain.probability(v, w) <= 0) continue;
            if (index[w] == -1) {
                tarjanDFS(w, chain, index, lowlink, onStack, stack, sccs, counter);
                lowlink[v] = Math.min(lowlink[v], lowlink[w]);
            } else if (onStack[w]) {
                lowlink[v] = Math.min(lowlink[v], index[w]);
            }
        }

        if (lowlink[v] == index[v]) {
            List<Integer> scc = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                onStack[w] = false;
                scc.add(w);
            } while (w != v);
            sccs.add(scc);
        }
    }

    /**
     * Classifies all states of the chain as ABSORBING, RECURRENT, or TRANSIENT.
     *
     * <p>A state is absorbing if P[i][i] = 1. A communicating class is recurrent
     * if no transitions leave it (a "closed" SCC in the DAG of SCCs). Otherwise,
     * all states in the class are transient.
     */
    public static Map<Integer, StateClass> classifyAllStates(MarkovChain chain) {
        List<List<Integer>> sccs = communicationClasses(chain);
        int n = chain.size();
        int[] sccOf = new int[n];
        for (int s = 0; s < sccs.size(); s++) {
            for (int v : sccs.get(s)) sccOf[v] = s;
        }

        // A class is "closed" if no edge leaves it
        boolean[] isClosed = new boolean[sccs.size()];
        Arrays.fill(isClosed, true);
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (chain.probability(u, v) > 0 && sccOf[u] != sccOf[v]) {
                    isClosed[sccOf[u]] = false;
                }
            }
        }

        Map<Integer, StateClass> result = new LinkedHashMap<>();
        TransitionMatrix p = chain.getMatrix();
        for (int i = 0; i < n; i++) {
            if (Math.abs(p.get(i, i) - 1.0) < TransitionMatrix.ROW_SUM_TOLERANCE) {
                result.put(i, StateClass.ABSORBING);
            } else if (isClosed[sccOf[i]]) {
                result.put(i, StateClass.RECURRENT);
            } else {
                result.put(i, StateClass.TRANSIENT);
            }
        }
        return result;
    }

    /** Returns the class of a single state. */
    public static StateClass classifyState(MarkovChain chain, int state) {
        return classifyAllStates(chain).get(state);
    }

    // =========================================================================
    // Irreducibility and periodicity
    // =========================================================================

    /**
     * Returns {@code true} if the chain is irreducible — every state is reachable
     * from every other state (there is exactly one communication class).
     */
    public static boolean isIrreducible(MarkovChain chain) {
        return communicationClasses(chain).size() == 1;
    }

    /**
     * Computes the period of a state — the GCD of all cycle lengths through it.
     *
     * <p>A state with period 1 is aperiodic. A state with period d > 1 can only
     * return to itself in multiples of d steps. For example, in a chain that
     * alternates between two states, both states have period 2.
     *
     * @return period of the state, or 0 if the state has no cycles (transient)
     */
    public static int period(MarkovChain chain, int state) {
        // BFS to find all return-path lengths to `state`
        int n = chain.size();
        int[] dist = new int[n];
        Arrays.fill(dist, -1);
        dist[state] = 0;

        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(state);
        int gcd = 0;

        // We want all paths that start at `state` and return to `state`.
        // BFS tracking: for each node, track dist[node] = minimum distance from `state`.
        // When we find an edge u→state, cycle length = dist[u] + 1.
        // We also handle "shorter" back-edges that create cycles of other lengths.

        // Use a more direct approach: BFS layer by layer, find all edges into `state`
        // from nodes at known distances.
        int[] bfsDist = new int[n];
        Arrays.fill(bfsDist, Integer.MAX_VALUE);
        bfsDist[state] = 0;
        Queue<Integer> q = new ArrayDeque<>();
        q.add(state);

        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v = 0; v < n; v++) {
                if (chain.probability(u, v) <= 0) continue;
                if (v == state) {
                    gcd = gcd(gcd, bfsDist[u] + 1);
                } else if (bfsDist[v] == Integer.MAX_VALUE) {
                    bfsDist[v] = bfsDist[u] + 1;
                    q.add(v);
                }
            }
        }
        return gcd;
    }

    /**
     * Returns {@code true} if all states in the chain are aperiodic (period 1).
     * For an irreducible chain, if one state is aperiodic, all are.
     */
    public static boolean isAperiodic(MarkovChain chain) {
        for (int i = 0; i < chain.size(); i++) {
            if (period(chain, i) != 1) return false;
        }
        return true;
    }

    /**
     * Returns {@code true} if the chain is ergodic — irreducible and aperiodic.
     *
     * <p>Ergodic chains have a unique stationary distribution, and the chain
     * converges to it from any starting state.
     */
    public static boolean isErgodic(MarkovChain chain) {
        return isIrreducible(chain) && isAperiodic(chain);
    }

    // =========================================================================
    // Gaussian elimination (private — used by MFPT and fundamental matrix)
    // =========================================================================

    /**
     * Solves the linear system Ax = b using Gauss-Jordan elimination with
     * partial pivoting.
     *
     * @param A coefficient matrix (n×n), will be modified in place (use a copy)
     * @param b right-hand side vector (length n)
     * @return solution vector x
     * @throws SingularMatrixException if A is singular (no unique solution)
     */
    static double[] gaussianEliminate(double[][] A, double[] b) {
        int n = A.length;
        // Build augmented matrix [A | b]
        double[][] aug = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            aug[i] = Arrays.copyOf(A[i], n + 1);
            aug[i][n] = b[i];
        }

        for (int col = 0; col < n; col++) {
            // Partial pivoting: find row with largest absolute value in this column
            int pivotRow = col;
            double pivotVal = Math.abs(aug[col][col]);
            for (int row = col + 1; row < n; row++) {
                if (Math.abs(aug[row][col]) > pivotVal) {
                    pivotVal = Math.abs(aug[row][col]);
                    pivotRow = row;
                }
            }
            if (pivotVal < 1e-12) {
                throw new SingularMatrixException("Matrix is singular at column " + col);
            }

            // Swap rows
            double[] tmp = aug[col];
            aug[col] = aug[pivotRow];
            aug[pivotRow] = tmp;

            // Scale pivot row so the diagonal = 1
            double scale = aug[col][col];
            for (int j = col; j <= n; j++) aug[col][j] /= scale;

            // Eliminate all other rows (Gauss-Jordan: full RREF)
            for (int row = 0; row < n; row++) {
                if (row == col || aug[row][col] == 0) continue;
                double factor = aug[row][col];
                for (int j = col; j <= n; j++) {
                    aug[row][j] -= factor * aug[col][j];
                }
            }
        }

        double[] x = new double[n];
        for (int i = 0; i < n; i++) x[i] = aug[i][n];
        return x;
    }

    /** Thrown when a matrix is singular during Gaussian elimination. */
    static class SingularMatrixException extends RuntimeException {
        SingularMatrixException(String msg) { super(msg); }
    }

    // =========================================================================
    // Utility
    // =========================================================================

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
