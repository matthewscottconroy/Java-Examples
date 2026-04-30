package com.evolutionary.pso;

import java.util.Arrays;
import java.util.Random;
import java.util.function.ToDoubleFunction;

/**
 * Particle Swarm Optimisation (PSO) — a swarm intelligence algorithm.
 *
 * <p>A swarm of particles moves through the search space. Each particle
 * remembers its personal best position and is attracted toward both its
 * personal best and the global best found by any particle.
 *
 * <p>Velocity update (per dimension d):
 * <pre>
 *   v[d] = ω·v[d]
 *         + c₁·r₁·(pBest[d] - pos[d])   ← cognitive component
 *         + c₂·r₂·(gBest[d] - pos[d])   ← social component
 * </pre>
 * ω  = inertia weight (balances exploration vs exploitation)
 * c₁ = cognitive coefficient (trust in personal best)
 * c₂ = social coefficient (trust in global best)
 * r₁, r₂ = random values in [0,1] sampled each step
 */
public class PSO {

    public record Config(int    swarmSize,
                          int    dimensions,
                          double[] lowerBound,
                          double[] upperBound,
                          double inertia,          // ω
                          double cognitiveCoeff,   // c₁
                          double socialCoeff,      // c₂
                          double maxVelocityFrac)  // max velocity as fraction of range
    {}

    public record Result(double[] position, double fitness, int iterations) {}

    private final Config              config;
    private final ToDoubleFunction<double[]> objective; // higher = better
    private final Random              rng;

    public PSO(Config config, ToDoubleFunction<double[]> objective, long seed) {
        this.config    = config;
        this.objective = objective;
        this.rng       = new Random(seed);
    }

    public Result optimise(int iterations) {
        int n = config.swarmSize(), d = config.dimensions();
        double[] lo = config.lowerBound(), hi = config.upperBound();

        double[][] pos    = new double[n][d];
        double[][] vel    = new double[n][d];
        double[][] pBest  = new double[n][d];
        double[]   pBestF = new double[n];
        double[]   gBest  = new double[d];
        double     gBestF = Double.NEGATIVE_INFINITY;

        // Initialise
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < d; k++) {
                double range = hi[k] - lo[k];
                pos[i][k] = lo[k] + rng.nextDouble() * range;
                vel[i][k] = (rng.nextDouble() - 0.5) * range * config.maxVelocityFrac();
            }
            pBest[i] = pos[i].clone();
            pBestF[i] = objective.applyAsDouble(pos[i]);
            if (pBestF[i] > gBestF) { gBestF = pBestF[i]; gBest = pos[i].clone(); }
        }

        double omega = config.inertia(), c1 = config.cognitiveCoeff(), c2 = config.socialCoeff();

        for (int iter = 0; iter < iterations; iter++) {
            for (int i = 0; i < n; i++) {
                for (int k = 0; k < d; k++) {
                    double r1 = rng.nextDouble(), r2 = rng.nextDouble();
                    vel[i][k] = omega * vel[i][k]
                              + c1 * r1 * (pBest[i][k] - pos[i][k])
                              + c2 * r2 * (gBest[k]    - pos[i][k]);
                    // Clamp velocity
                    double vMax = (hi[k] - lo[k]) * config.maxVelocityFrac();
                    vel[i][k] = Math.max(-vMax, Math.min(vMax, vel[i][k]));

                    pos[i][k] = Math.max(lo[k], Math.min(hi[k], pos[i][k] + vel[i][k]));
                }
                double f = objective.applyAsDouble(pos[i]);
                if (f > pBestF[i]) { pBest[i] = pos[i].clone(); pBestF[i] = f; }
                if (f > gBestF)    { gBest = pos[i].clone();    gBestF = f;    }
            }
        }
        return new Result(gBest, gBestF, iterations * n);
    }
}
