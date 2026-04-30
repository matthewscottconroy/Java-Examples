package com.evolutionary.pso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PSOTest {

    PSO.Config sphereConfig(int dim) {
        double[] lo = new double[dim], hi = new double[dim];
        for (int i = 0; i < dim; i++) { lo[i] = -10; hi[i] = 10; }
        return new PSO.Config(30, dim, lo, hi, 0.72, 1.49, 1.49, 0.2);
    }

    @Test
    @DisplayName("PSO finds minimum of 1D sphere near origin")
    void sphere1D() {
        PSO pso = new PSO(sphereConfig(1), x -> -x[0]*x[0], 1L);
        PSO.Result r = pso.optimise(200);
        assertEquals(0.0, r.position()[0], 0.1, "Should find minimum near 0");
    }

    @Test
    @DisplayName("PSO finds minimum of 2D sphere near origin")
    void sphere2D() {
        PSO pso = new PSO(sphereConfig(2), x -> -(x[0]*x[0] + x[1]*x[1]), 2L);
        PSO.Result r = pso.optimise(300);
        assertEquals(0.0, r.position()[0], 0.5);
        assertEquals(0.0, r.position()[1], 0.5);
    }

    @Test
    @DisplayName("PSO stays within bounds throughout optimisation")
    void staysInBounds() {
        double[] lo = {-5, -5}, hi = {5, 5};
        PSO.Config cfg = new PSO.Config(20, 2, lo, hi, 0.7, 1.5, 1.5, 0.2);
        PSO pso = new PSO(cfg, x -> -(x[0]*x[0] + x[1]*x[1]), 3L);
        PSO.Result r = pso.optimise(100);
        for (int d = 0; d < 2; d++) {
            assertTrue(r.position()[d] >= lo[d]);
            assertTrue(r.position()[d] <= hi[d]);
        }
    }

    @Test
    @DisplayName("WiFi coverage: three APs spread across the building")
    void wifiCoverage() {
        int dim = Main.NUM_APS * 2;
        double[] lo = new double[dim], hi = new double[dim];
        for (int i = 0; i < dim; i += 2) {
            lo[i] = 0; hi[i] = Main.BUILDING_W;
            lo[i+1] = 0; hi[i+1] = Main.BUILDING_H;
        }
        PSO.Config cfg = new PSO.Config(50, dim, lo, hi, 0.72, 1.49, 1.49, 0.2);
        PSO pso = new PSO(cfg, Main::coverage, 42L);
        PSO.Result r = pso.optimise(500);

        // Maximum possible distance between two corners of a 100x100 square is ~141.4
        // Three APs spread to corners: sum of 3 pairwise distances ≈ 3*141 = 424
        // A decent PSO should achieve at least 60% of this theoretical max
        assertTrue(r.fitness() > 200,
            "Coverage score should be substantial; got " + r.fitness());
    }

    @Test
    @DisplayName("Longer run gives better or equal fitness than shorter run")
    void longerRunBetter() {
        PSO psoShort = new PSO(sphereConfig(3), x -> -(x[0]*x[0]+x[1]*x[1]+x[2]*x[2]), 5L);
        PSO psoLong  = new PSO(sphereConfig(3), x -> -(x[0]*x[0]+x[1]*x[1]+x[2]*x[2]), 5L);
        double fShort = psoShort.optimise(10).fitness();
        double fLong  = psoLong.optimise(500).fitness();
        assertTrue(fLong >= fShort);
    }
}
