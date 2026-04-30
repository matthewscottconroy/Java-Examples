package com.ml.kmeans;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class KMeansTest {

    // Three clearly separated clusters
    static final double[][] THREE_CLUSTERS = {
        // Cluster A — top-right
        {0.9, 0.9}, {0.85, 0.95}, {0.95, 0.85},
        // Cluster B — middle
        {0.5, 0.5}, {0.45, 0.55}, {0.55, 0.45},
        // Cluster C — bottom-left
        {0.1, 0.1}, {0.05, 0.15}, {0.15, 0.05},
    };

    @Test
    @DisplayName("k=3 on three-cluster data: each cluster gets exactly 3 members")
    void threeCluster_correctAssignments() {
        KMeans km = new KMeans(3, 100, 0L);
        km.fit(THREE_CLUSTERS);

        int[] assignments = km.assignments();
        Map<Integer, Integer> counts = new HashMap<>();
        for (int a : assignments) counts.merge(a, 1, Integer::sum);

        // All 3 clusters should be represented
        assertEquals(3, counts.size(), "All 3 clusters should have members");
        // Each cluster should have exactly 3 members
        for (int count : counts.values()) assertEquals(3, count);
    }

    @Test
    @DisplayName("WCSS decreases (or stays equal) as k increases")
    void wcss_decreasesWithK() {
        double prev = Double.MAX_VALUE;
        for (int k = 1; k <= 4; k++) {
            KMeans km = new KMeans(k, 100, 0L);
            km.fit(THREE_CLUSTERS);
            double wcss = km.wcss(THREE_CLUSTERS);
            assertTrue(wcss <= prev + 1e-9,
                "WCSS should not increase as k grows (k=" + k + ")");
            prev = wcss;
        }
    }

    @Test
    @DisplayName("k=1: single centroid is the mean of all points")
    void k1_centroidIsMean() {
        double[][] X = { {0.0, 0.0}, {1.0, 0.0}, {0.5, 1.0} };
        KMeans km = new KMeans(1, 10, 0L);
        km.fit(X);
        double[] c = km.centroids()[0];
        assertEquals(0.5, c[0], 1e-6, "x centroid should be mean of 0, 1, 0.5");
        assertEquals(1.0/3.0, c[1], 1e-6, "y centroid should be mean of 0, 0, 1");
    }

    @Test
    @DisplayName("Number of centroids equals k")
    void centroids_countEqualsK() {
        KMeans km = new KMeans(4, 50, 0L);
        km.fit(THREE_CLUSTERS);
        assertEquals(4, km.centroids().length);
    }

    @Test
    @DisplayName("predict(x) returns cluster nearest to x")
    void predict_single() {
        KMeans km = new KMeans(3, 100, 0L);
        km.fit(THREE_CLUSTERS);
        // A point in cluster A's region should map to A's cluster
        int cluster = km.predict(new double[]{0.92, 0.88});
        // Find which cluster is nearest to top-right
        double[][] centroids = km.centroids();
        double minDist = Double.MAX_VALUE;
        int expected = 0;
        for (int c = 0; c < 3; c++) {
            double d = KMeans.euclidean(new double[]{0.92, 0.88}, centroids[c]);
            if (d < minDist) { minDist = d; expected = c; }
        }
        assertEquals(expected, cluster);
    }

    @Test
    @DisplayName("Assignments array length equals number of training samples")
    void assignments_length() {
        KMeans km = new KMeans(3, 100, 0L);
        km.fit(THREE_CLUSTERS);
        assertEquals(THREE_CLUSTERS.length, km.assignments().length);
    }

    @Test
    @DisplayName("WCSS is zero for k = number of samples")
    void wcss_zeroWhenKEqualsM() {
        double[][] X = { {0.1, 0.2}, {0.8, 0.7}, {0.5, 0.5} };
        KMeans km = new KMeans(3, 200, 0L);
        km.fit(X);
        assertEquals(0.0, km.wcss(X), 1e-9, "WCSS must be 0 when k=m");
    }

    @Test
    @DisplayName("Customer dataset: k=3 WCSS much lower than k=1")
    void customerDataset_elbowEffect() {
        KMeans km1 = new KMeans(1, 100, 42L);
        km1.fit(Main.CUSTOMERS);

        KMeans km3 = new KMeans(3, 100, 42L);
        km3.fit(Main.CUSTOMERS);

        assertTrue(km3.wcss(Main.CUSTOMERS) < km1.wcss(Main.CUSTOMERS) * 0.3,
            "k=3 should dramatically reduce WCSS vs k=1");
    }

    @Test
    @DisplayName("Euclidean distance: (0,0) to (3,4) = 5")
    void euclidean_pythagorean() {
        assertEquals(5.0, KMeans.euclidean(new double[]{0,0}, new double[]{3,4}), 1e-9);
    }
}
