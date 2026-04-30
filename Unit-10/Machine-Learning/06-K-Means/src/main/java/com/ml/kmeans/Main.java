package com.ml.kmeans;

import java.util.*;

/**
 * Customer segmentation for a retail platform.
 *
 * Each customer is described by two features:
 *   [annual_spend_norm, purchase_frequency_norm]
 *
 * K-Means discovers natural customer segments — high-value loyal customers,
 * occasional big spenders, and infrequent low-value shoppers — without any
 * pre-defined labels. The output can inform targeted marketing campaigns.
 */
public class Main {

    // [annual_spend_norm, purchase_frequency_norm]
    static final double[][] CUSTOMERS = {
        // High-value, frequent (cluster A)
        { 0.9, 0.9 }, { 0.8, 0.85 }, { 0.85, 0.9 }, { 0.9, 0.8 }, { 0.8, 0.95 },
        { 0.85, 0.85 }, { 0.9, 0.75 }, { 0.75, 0.9 },
        // Medium-value, moderate frequency (cluster B)
        { 0.5, 0.5 }, { 0.55, 0.45 }, { 0.45, 0.55 }, { 0.5, 0.6 }, { 0.6, 0.5 },
        { 0.55, 0.55 }, { 0.45, 0.45 }, { 0.5, 0.4 },
        // Low-value, infrequent (cluster C)
        { 0.1, 0.1 }, { 0.15, 0.2 }, { 0.2, 0.1 }, { 0.1, 0.15 }, { 0.15, 0.15 },
        { 0.2, 0.2 }, { 0.1, 0.2 }, { 0.15, 0.1 },
    };

    public static void main(String[] args) {
        System.out.println("=== K-Means — Customer Segmentation ===\n");

        // Elbow method: WCSS for k=1..5
        System.out.println("Elbow method (WCSS vs k):");
        for (int k = 1; k <= 5; k++) {
            KMeans km = new KMeans(k, 100, 42L);
            km.fit(CUSTOMERS);
            System.out.printf("  k=%d  WCSS=%.4f%n", k, km.wcss(CUSTOMERS));
        }

        System.out.println();
        KMeans km = new KMeans(3, 100, 42L);
        km.fit(CUSTOMERS);

        // Map cluster index → descriptive segment name
        double[][] centroids = km.centroids();
        String[] segmentNames = new String[3];
        for (int c = 0; c < 3; c++) {
            double spend = centroids[c][0], freq = centroids[c][1];
            segmentNames[c] = spend > 0.7 ? "High-Value Loyal"
                            : spend > 0.4 ? "Mid-Value Occasional"
                            : "Low-Value Infrequent";
        }

        System.out.printf("Cluster centroids (spend, frequency):%n");
        for (int c = 0; c < 3; c++) {
            System.out.printf("  Cluster %d [%s]: spend=%.2f freq=%.2f%n",
                c, segmentNames[c], centroids[c][0], centroids[c][1]);
        }

        // Count members per cluster
        int[] counts = new int[3];
        for (int a : km.assignments()) counts[a]++;
        System.out.println("\nCluster sizes:");
        for (int c = 0; c < 3; c++) System.out.printf("  Cluster %d: %d customers%n", c, counts[c]);

        System.out.printf("%nWCSS (k=3): %.4f%n", km.wcss(CUSTOMERS));
    }
}
