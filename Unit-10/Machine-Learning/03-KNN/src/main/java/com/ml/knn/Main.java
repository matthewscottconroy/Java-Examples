package com.ml.knn;

/**
 * Medical diagnosis classifier.
 *
 * A patient is described by four blood-panel readings (all normalised to [0,1]).
 * KNN classifies them into one of three conditions:
 *   0 = Healthy
 *   1 = Pre-diabetic
 *   2 = Diabetic
 *
 * Features: [glucose_norm, insulin_norm, bmi_norm, age_norm]
 */
public class Main {

    static final String[] CLASS_NAMES = { "Healthy", "Pre-diabetic", "Diabetic" };

    // [glucose, insulin, bmi, age] — all normalised
    static final double[][] X = {
        // Healthy (0)
        { 0.3, 0.2, 0.3, 0.2 }, { 0.2, 0.3, 0.2, 0.1 }, { 0.4, 0.2, 0.4, 0.3 },
        { 0.3, 0.3, 0.3, 0.2 }, { 0.2, 0.2, 0.2, 0.2 }, { 0.4, 0.3, 0.3, 0.1 },
        // Pre-diabetic (1)
        { 0.5, 0.5, 0.6, 0.5 }, { 0.6, 0.4, 0.5, 0.4 }, { 0.5, 0.6, 0.6, 0.6 },
        { 0.6, 0.5, 0.5, 0.5 }, { 0.5, 0.4, 0.7, 0.4 }, { 0.7, 0.5, 0.6, 0.5 },
        // Diabetic (2)
        { 0.8, 0.8, 0.8, 0.7 }, { 0.9, 0.7, 0.9, 0.8 }, { 0.8, 0.9, 0.8, 0.9 },
        { 0.9, 0.8, 0.9, 0.7 }, { 0.7, 0.9, 0.8, 0.8 }, { 0.9, 0.9, 0.9, 0.9 },
    };

    static final int[] Y = { 0,0,0,0,0,0, 1,1,1,1,1,1, 2,2,2,2,2,2 };

    public static void main(String[] args) {
        System.out.println("=== K-Nearest Neighbours — Medical Diagnosis ===\n");

        for (int k : new int[]{ 1, 3, 5 }) {
            KNN knn = new KNN(k);
            knn.fit(X, Y);
            System.out.printf("k=%d  training accuracy: %.1f%%%n", k, knn.accuracy(X, Y) * 100);
        }

        KNN knn = new KNN(3);
        knn.fit(X, Y);

        System.out.println("\nSample predictions:");
        System.out.printf("  %-35s  %-14s  %-14s%n", "Patient features", "Actual", "Predicted");
        System.out.println("  " + "-".repeat(65));
        for (int i = 0; i < X.length; i += 4) {
            int pred = knn.predict(X[i]);
            System.out.printf("  %-35s  %-14s  %-14s%n",
                String.format("gluc=%.1f ins=%.1f bmi=%.1f age=%.1f", X[i][0], X[i][1], X[i][2], X[i][3]),
                CLASS_NAMES[Y[i]], CLASS_NAMES[pred]);
        }

        System.out.println("\nNew patient (glucose=0.65, insulin=0.55, bmi=0.60, age=0.50):");
        double[] newPatient = { 0.65, 0.55, 0.60, 0.50 };
        int pred = knn.predict(newPatient);
        double[] dists = knn.distances(newPatient);
        System.out.printf("  Diagnosis: %s%n", CLASS_NAMES[pred]);
        System.out.printf("  Nearest distances: %.3f, %.3f, %.3f%n", dists[0], dists[1], dists[2]);
    }
}
