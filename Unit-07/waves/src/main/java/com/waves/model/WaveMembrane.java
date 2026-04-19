package com.waves.model;

import java.awt.image.BufferedImage;

/**
 * Finite-difference model of a 2D vibrating membrane under the wave equation.
 *
 * <h2>Governing PDE</h2>
 * <pre>  ∂²u/∂t² = c² · (∂²u/∂x² + ∂²u/∂y²)</pre>
 * where {@code u(x,y,t)} is the transverse displacement of the membrane.
 *
 * <h2>Finite-difference update</h2>
 * Using the discrete Laplacian:
 * <pre>
 *   L[i][j] = u[i+1][j] + u[i−1][j] + u[i][j+1] + u[i][j−1] − 4·u[i][j]
 *   u_next[i][j] = 2·u[i][j] − u_prev[i][j] + r²·L[i][j]
 * </pre>
 * where {@code r = c·dt/dx}.  Stability in 2-D requires {@code r ≤ 1/√2 ≈ 0.707};
 * the Courant number is fixed at {@code 0.45} for comfortable margin.
 *
 * <h2>Boundary conditions</h2>
 * <p>Fixed (Dirichlet) edges: {@code u = 0} on all four borders.
 *
 * <h2>Grid</h2>
 * <p>{@code 180 × 120} cells, {@code dx = dy = 1.0}.
 */
public class WaveMembrane {

    /** Number of rows in the grid. */
    public static final int ROWS = 120;

    /** Number of columns in the grid. */
    public static final int COLS = 180;

    /** Spatial step size (grid units). */
    public static final double DX = 1.0;

    /** Courant number used to derive {@code dt}; must be ≤ 1/√2 for 2-D stability. */
    public static final double COURANT = 0.45;

    private final double[][] u;
    private final double[][] uPrev;
    private final double[][] uNext;

    /**
     * Construct a membrane initially at rest with zero displacement everywhere.
     */
    public WaveMembrane() {
        u     = new double[ROWS][COLS];
        uPrev = new double[ROWS][COLS];
        uNext = new double[ROWS][COLS];
    }

    // -------------------------------------------------------------------------
    // Simulation
    // -------------------------------------------------------------------------

    /**
     * Advance the simulation by one time step.
     *
     * <p>Time step: {@code dt = COURANT · dx / c}.
     *
     * @param c       wave speed (grid units per second); must be &gt; 0
     * @param damping per-step energy loss fraction in [0, 1); 0 = no damping
     */
    public void step(double c, double damping) {
        double dt = COURANT * DX / c;
        double r  = c * dt / DX;
        double r2 = r * r;

        for (int i = 1; i < ROWS - 1; i++) {
            for (int j = 1; j < COLS - 1; j++) {
                double laplacian = u[i + 1][j] + u[i - 1][j]
                                 + u[i][j + 1] + u[i][j - 1]
                                 - 4.0 * u[i][j];
                uNext[i][j] = (2.0 * u[i][j] - uPrev[i][j] + r2 * laplacian)
                              * (1.0 - damping);
            }
        }

        // Rotate buffers
        for (int i = 0; i < ROWS; i++) {
            System.arraycopy(u[i],     0, uPrev[i], 0, COLS);
            System.arraycopy(uNext[i], 0, u[i],     0, COLS);
        }

        // Enforce zero boundary (edges stay at 0)
        for (int j = 0; j < COLS; j++) {
            u[0][j]        = 0.0;
            u[ROWS - 1][j] = 0.0;
            uPrev[0][j]        = 0.0;
            uPrev[ROWS - 1][j] = 0.0;
        }
        for (int i = 0; i < ROWS; i++) {
            u[i][0]        = 0.0;
            u[i][COLS - 1] = 0.0;
            uPrev[i][0]        = 0.0;
            uPrev[i][COLS - 1] = 0.0;
        }
    }

    /**
     * Excite the membrane with a Gaussian displacement bump centred at {@code (cx, cy)}.
     *
     * <pre>  u[i][j] += amplitude · exp(−(((i−cy)/sigma)² + ((j−cx)/sigma)²))</pre>
     *
     * <p>The previous-step array is synchronised to the current array so the
     * poke introduces no artificial velocity kick.
     *
     * @param cx        column index of the poke centre (0 … COLS−1)
     * @param cy        row index of the poke centre (0 … ROWS−1)
     * @param amplitude peak displacement (grid units)
     * @param sigma     standard deviation of the Gaussian (grid units)
     */
    public void poke(int cx, int cy, double amplitude, double sigma) {
        for (int i = 1; i < ROWS - 1; i++) {
            for (int j = 1; j < COLS - 1; j++) {
                double di = (i - cy) / sigma;
                double dj = (j - cx) / sigma;
                u[i][j]     += amplitude * Math.exp(-(di * di + dj * dj));
                uPrev[i][j]  = u[i][j];
            }
        }
        // Boundaries remain zero
        for (int j = 0; j < COLS; j++) { u[0][j] = 0; u[ROWS-1][j] = 0; }
        for (int i = 0; i < ROWS; i++) { u[i][0] = 0; u[i][COLS-1] = 0; }
    }

    /**
     * Reset the membrane to zero displacement and zero velocity everywhere.
     */
    public void reset() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                u[i][j]     = 0.0;
                uPrev[i][j] = 0.0;
                uNext[i][j] = 0.0;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    /**
     * Render the current displacement field to a {@link BufferedImage}.
     *
     * <p>Displacement values are mapped to colour:
     * <ul>
     *   <li>Negative → blue</li>
     *   <li>Zero → black</li>
     *   <li>Positive → red</li>
     * </ul>
     *
     * <p>The image is sized to match the grid ({@code COLS × ROWS}) and should
     * be scaled by the caller via {@code drawImage} with bilinear interpolation.
     *
     * @param panelW unused (reserved for future direct-to-panel rendering)
     * @param panelH unused
     * @return {@link BufferedImage} of type {@code TYPE_INT_RGB}
     */
    public BufferedImage renderToImage(int panelW, int panelH) {
        BufferedImage img = new BufferedImage(COLS, ROWS, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                double val = u[i][j];
                // Clamp to [-1, 1]
                val = Math.max(-1.0, Math.min(1.0, val));

                int r, g, b;
                if (val >= 0.0) {
                    // Black → Red
                    r = (int) (val * 255);
                    g = 0;
                    b = 0;
                } else {
                    // Black → Blue
                    r = 0;
                    g = 0;
                    b = (int) (-val * 255);
                }
                img.setRGB(j, i, (r << 16) | (g << 8) | b);
            }
        }
        return img;
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Return the current displacement at grid cell {@code (row, col)}.
     *
     * @param row row index (0 … ROWS−1)
     * @param col column index (0 … COLS−1)
     * @return transverse displacement
     */
    public double getDisplacement(int row, int col) {
        return u[row][col];
    }

    /**
     * Return the number of rows in the grid.
     *
     * @return {@link #ROWS}
     */
    public int getRows() { return ROWS; }

    /**
     * Return the number of columns in the grid.
     *
     * @return {@link #COLS}
     */
    public int getCols() { return COLS; }
}
