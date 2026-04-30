package com.ml.linear;

/**
 * House price predictor.
 *
 * Features: [size_sqft, bedrooms, age_years]
 * Target:   price in $1000s
 *
 * The model learns: price ≈ w0 + w1*size + w2*bedrooms + w3*age
 */
public class Main {

    // [size_sqft, bedrooms, age_years]
    static final double[][] X = {
        { 850,  2, 30 }, { 1200, 3, 15 }, { 950,  2, 20 }, { 1500, 4,  5 },
        { 700,  1, 40 }, { 1800, 4,  8 }, { 1100, 3, 25 }, { 2000, 5,  3 },
        { 600,  1, 50 }, { 1350, 3, 12 }, { 1650, 4, 10 }, { 900,  2, 35 },
        { 1050, 3, 22 }, { 1400, 3,  7 }, { 2200, 5,  1 }, { 750,  2, 45 },
        { 1750, 4,  6 }, { 1000, 2, 18 }, { 1300, 3, 14 }, { 1600, 4,  9 },
    };

    // price in $1000s
    static final double[] Y = {
        180, 240, 195, 320, 140, 380, 210, 430, 120, 265,
        340, 170, 220, 290, 480, 155, 370, 200, 260, 330,
    };

    public static void main(String[] args) {
        System.out.println("=== Linear Regression — House Price Predictor ===\n");

        // Feature scaling: normalise each feature to [0,1]
        double[] mins  = { 600,  1,  1 };
        double[] maxes = { 2200, 5, 50 };
        double[][] Xn = normalise(X, mins, maxes);

        LinearRegression model = new LinearRegression(0.1, 2000);
        model.fit(Xn, Y);

        double[] w = model.weights();
        System.out.printf("Learned weights: bias=%.2f  size=%.2f  beds=%.2f  age=%.2f%n",
            w[0], w[1], w[2], w[3]);
        System.out.printf("Training MSE: %.2f ($1000s²)%n", model.mse(Xn, Y));
        System.out.printf("R²:           %.4f%n", model.r2(Xn, Y));

        System.out.println("\nSample predictions vs actual:");
        System.out.printf("  %-25s  %8s  %8s%n", "House", "Actual", "Predicted");
        System.out.println("  " + "-".repeat(44));
        String[] labels = { "850sqft 2bd 30yr", "1500sqft 4bd 5yr", "2200sqft 5bd 1yr" };
        int[] idxs = { 0, 3, 14 };
        for (int i = 0; i < idxs.length; i++) {
            int idx = idxs[i];
            double pred = model.predict(Xn[idx]);
            System.out.printf("  %-25s  $%6.1fk  $%6.1fk%n", labels[i], Y[idx], pred);
        }

        System.out.println("\nPredicting a new house (1600 sqft, 3 bedrooms, 10 years old):");
        double[] newHouse = normalise(new double[]{ 1600, 3, 10 }, mins, maxes);
        System.out.printf("  Predicted price: $%.1fk%n", model.predict(newHouse));
    }

    static double[][] normalise(double[][] X, double[] mins, double[] maxes) {
        double[][] out = new double[X.length][X[0].length];
        for (int i = 0; i < X.length; i++)
            for (int j = 0; j < X[i].length; j++)
                out[i][j] = (X[i][j] - mins[j]) / (maxes[j] - mins[j]);
        return out;
    }

    static double[] normalise(double[] x, double[] mins, double[] maxes) {
        double[] out = new double[x.length];
        for (int j = 0; j < x.length; j++)
            out[j] = (x[j] - mins[j]) / (maxes[j] - mins[j]);
        return out;
    }
}
