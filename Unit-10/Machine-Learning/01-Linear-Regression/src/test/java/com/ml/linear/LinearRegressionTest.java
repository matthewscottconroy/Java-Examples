package com.ml.linear;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinearRegressionTest {

    // Perfect linear relationship: y = 2x + 1
    static final double[][] X1D = { {0}, {1}, {2}, {3}, {4} };
    static final double[]   Y1D = {  1,   3,   5,   7,   9  };

    @Test
    @DisplayName("Fits perfect 1D linear relationship (y = 2x + 1)")
    void perfectLinear_1D() {
        LinearRegression lr = new LinearRegression(0.1, 1000);
        lr.fit(X1D, Y1D);
        // Expect near-zero MSE
        assertTrue(lr.mse(X1D, Y1D) < 0.01,
            "MSE should be near zero on perfect linear data");
    }

    @Test
    @DisplayName("Predictions close to true values on perfect linear data")
    void perfectLinear_predictions() {
        LinearRegression lr = new LinearRegression(0.1, 1000);
        lr.fit(X1D, Y1D);
        assertEquals(1.0, lr.predict(new double[]{0}), 0.1);
        assertEquals(5.0, lr.predict(new double[]{2}), 0.1);
        assertEquals(9.0, lr.predict(new double[]{4}), 0.1);
    }

    @Test
    @DisplayName("R² approaches 1.0 on perfect linear data")
    void perfectLinear_r2() {
        LinearRegression lr = new LinearRegression(0.1, 1000);
        lr.fit(X1D, Y1D);
        assertTrue(lr.r2(X1D, Y1D) > 0.99, "R² should be near 1 for perfect fit");
    }

    @Test
    @DisplayName("Fits 2D linear relationship: y = 3x1 + 2x2 + 1")
    void perfectLinear_2D() {
        // y = 3*x1 + 2*x2 + 1
        double[][] X = { {0,0}, {1,0}, {0,1}, {1,1}, {2,1}, {1,2} };
        double[]   y = {   1,    4,    3,    6,    9,    8  };
        LinearRegression lr = new LinearRegression(0.05, 2000);
        lr.fit(X, y);
        assertTrue(lr.mse(X, y) < 0.05, "MSE should be near zero on perfect 2D linear data");
        assertEquals(6.0, lr.predict(new double[]{1, 1}), 0.2);
    }

    @Test
    @DisplayName("R² is less than 1 for noisy data but still positive")
    void noisyData_r2Positive() {
        // Noisy version of y = 2x + 1
        double[][] X = { {0}, {1}, {2}, {3}, {4} };
        double[]   y = { 1.2, 3.5, 4.8, 7.1, 9.3 };
        LinearRegression lr = new LinearRegression(0.1, 1000);
        lr.fit(X, y);
        double r2 = lr.r2(X, y);
        assertTrue(r2 > 0.9 && r2 < 1.0, "R² for noisy data should be high but < 1");
    }

    @Test
    @DisplayName("Constant target: MSE converges to zero")
    void constantTarget() {
        double[][] X = { {1}, {2}, {3}, {4} };
        double[]   y = { 5,   5,   5,   5  };
        LinearRegression lr = new LinearRegression(0.1, 2000);
        lr.fit(X, y);
        assertEquals(5.0, lr.predict(new double[]{10}), 0.5);
    }

    @Test
    @DisplayName("Weights array has length = features + 1 (bias)")
    void weightsLength() {
        LinearRegression lr = new LinearRegression(0.01, 10);
        lr.fit(new double[][]{{1, 2, 3}}, new double[]{1});
        assertEquals(4, lr.weights().length, "Weights must include bias term");
    }

    @Test
    @DisplayName("House price dataset achieves R² > 0.95")
    void housePrices_goodFit() {
        double[] mins  = { 600, 1, 1 };
        double[] maxes = { 2200, 5, 50 };
        double[][] Xn = Main.normalise(Main.X, mins, maxes);
        LinearRegression lr = new LinearRegression(0.1, 2000);
        lr.fit(Xn, Main.Y);
        assertTrue(lr.r2(Xn, Main.Y) > 0.95,
            "Model should explain >95% of variance in house price data");
    }
}
