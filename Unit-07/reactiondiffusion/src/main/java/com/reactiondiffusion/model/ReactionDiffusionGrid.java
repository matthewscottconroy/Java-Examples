package com.reactiondiffusion.model;

import java.awt.Color;
import java.util.Random;

/**
 * Numerical simulation of the Gray-Scott reaction-diffusion model on a
 * periodic (toroidal) 2-D grid.
 *
 * <h2>Governing equations</h2>
 * <pre>
 *   ∂A/∂t = Dₐ·∇²A − A·B² + F·(1−A)
 *   ∂B/∂t = D_b·∇²B + A·B² − (F+k)·B
 * </pre>
 * where A and B are chemical concentrations in [0, 1], Dₐ and D_b are
 * diffusion coefficients, F is the feed rate (replenishes A from an external
 * reservoir), and k is the kill rate (removes B).
 *
 * <h2>Discretisation</h2>
 * <p>The Laplacian at cell (i, j) is approximated with the 5-point stencil
 * <pre>  ∇²u ≈ u[i+1][j] + u[i-1][j] + u[i][j+1] + u[i][j-1] − 4·u[i][j]</pre>
 * using periodic (wrap-around) boundary conditions.  Integration uses the
 * explicit forward-Euler scheme with dt = 1.0 (dimensionless).  A double
 * buffer (current + next arrays) eliminates the read-write hazard that would
 * arise from updating cells in-place.
 *
 * <h2>Thread safety</h2>
 * <p>This class is <em>not</em> thread-safe.  All calls should originate from
 * the Swing Event Dispatch Thread or be externally synchronised.
 */
public class ReactionDiffusionGrid {

    /** Number of grid rows. */
    public final int rows;

    /** Number of grid columns. */
    public final int cols;

    // Current concentration arrays
    private double[][] a;
    private double[][] b;

    // Next-step scratch arrays (double buffer)
    private double[][] aN;
    private double[][] bN;

    // Model parameters
    private double Da;
    private double Db;
    private double F;
    private double k;

    /** Time step used for explicit Euler integration (dimensionless). */
    private static final double DT = 1.0;

    /**
     * Construct a grid with the given dimensions and default Gray-Scott parameters.
     *
     * <p>The grid is initialised to A = 1, B = 0 everywhere.
     *
     * @param rows number of grid rows (height)
     * @param cols number of grid columns (width)
     * @param Da   diffusion coefficient for chemical A (dimensionless; typically 0.2)
     * @param Db   diffusion coefficient for chemical B (dimensionless; typically 0.1)
     * @param F    feed rate (dimensionless; typically 0.01–0.08)
     * @param k    kill rate (dimensionless; typically 0.04–0.075)
     */
    public ReactionDiffusionGrid(int rows, int cols, double Da, double Db, double F, double k) {
        this.rows = rows;
        this.cols = cols;
        this.Da   = Da;
        this.Db   = Db;
        this.F    = F;
        this.k    = k;

        a  = new double[rows][cols];
        b  = new double[rows][cols];
        aN = new double[rows][cols];
        bN = new double[rows][cols];

        reset();
    }

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    /**
     * Reset the grid to the uniform rest state: A = 1, B = 0 everywhere.
     *
     * <p>Call this before seeding a new pattern, or when the user clicks Reset.
     */
    public void reset() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                a[i][j] = 1.0;
                b[i][j] = 0.0;
            }
        }
    }

    /**
     * Introduce a square blob of chemical B centred at ({@code cx}, {@code cy}).
     *
     * <p>Within the square of side {@code 2·radius} cells, A is perturbed to
     * {@code 0.5 − noise} and B to {@code 0.5 + noise}, where noise is a
     * uniform random value in [−0.05, 0.05].  Cells outside the blob are
     * unchanged.  The blob wraps with periodic boundaries.
     *
     * @param cx     column index of the blob centre
     * @param cy     row index of the blob centre
     * @param radius half-side of the seeded square (cells)
     * @param rng    random-number generator for the perturbation noise
     */
    public void seed(int cx, int cy, int radius, Random rng) {
        for (int di = -radius; di <= radius; di++) {
            for (int dj = -radius; dj <= radius; dj++) {
                int ri = ((cy + di) % rows + rows) % rows;
                int ci = ((cx + dj) % cols + cols) % cols;
                double noise = (rng.nextDouble() - 0.5) * 0.1; // ±0.05
                a[ri][ci] = clamp(0.5 - noise);
                b[ri][ci] = clamp(0.5 + noise);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Simulation
    // -------------------------------------------------------------------------

    /**
     * Advance the simulation by the given number of Euler substeps.
     *
     * <p>Each substep applies the Gray-Scott update to every grid cell using
     * the 5-point Laplacian stencil and periodic boundary conditions.  The
     * double buffer ensures that all reads in a step use the state from the
     * previous step.
     *
     * @param substeps number of Euler steps to perform; must be &ge; 1
     */
    public void step(int substeps) {
        for (int s = 0; s < substeps; s++) {
            singleStep();
        }
    }

    private void singleStep() {
        for (int i = 0; i < rows; i++) {
            int ip = (i + 1) % rows;
            int im = (i - 1 + rows) % rows;
            for (int j = 0; j < cols; j++) {
                int jp = (j + 1) % cols;
                int jm = (j - 1 + cols) % cols;

                double aij = a[i][j];
                double bij = b[i][j];

                double lapA = a[ip][j] + a[im][j] + a[i][jp] + a[i][jm] - 4.0 * aij;
                double lapB = b[ip][j] + b[im][j] + b[i][jp] + b[i][jm] - 4.0 * bij;

                double reaction = aij * bij * bij;

                aN[i][j] = clamp(aij + DT * (Da * lapA - reaction + F * (1.0 - aij)));
                bN[i][j] = clamp(bij + DT * (Db * lapB + reaction - (F + k) * bij));
            }
        }

        // Swap buffers
        double[][] tmpA = a; a = aN; aN = tmpA;
        double[][] tmpB = b; b = bN; bN = tmpB;
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    /**
     * Fill a packed-ARGB pixel buffer suitable for use with
     * {@link java.awt.image.BufferedImage#setRGB(int, int, int[], int, int)}.
     *
     * <p>Each grid cell maps to one pixel in a {@code cols × rows} image.
     * The B concentration is mapped to a perceptual black-blue-cyan-white
     * colour ramp:
     * <ul>
     *   <li>B ≈ 0 → near-black</li>
     *   <li>B ≈ 0.33 → deep blue</li>
     *   <li>B ≈ 0.66 → bright cyan</li>
     *   <li>B ≈ 1   → white</li>
     * </ul>
     *
     * @param pixelBuffer  destination array of length {@code imageWidth * imageHeight};
     *                     only cells corresponding to grid cells are written
     * @param imageWidth   width of the target image in pixels (should equal {@code cols})
     * @param imageHeight  height of the target image in pixels (should equal {@code rows})
     */
    public void renderToImage(int[] pixelBuffer, int imageWidth, int imageHeight) {
        for (int i = 0; i < rows && i < imageHeight; i++) {
            for (int j = 0; j < cols && j < imageWidth; j++) {
                pixelBuffer[i * imageWidth + j] = bToRGB(b[i][j]);
            }
        }
    }

    /**
     * Map a B concentration value to a packed ARGB integer using a
     * black-blue-cyan-white colour ramp.
     *
     * @param bVal B concentration in [0, 1]
     * @return packed ARGB colour (alpha = 0xFF)
     */
    private static int bToRGB(double bVal) {
        // Three-stop ramp: [0,0.5] → black→blue, [0.5,1] → blue→cyan→white
        int r = (int) (Math.min(1.0, Math.max(0.0, bVal * 3.0 - 1.5)) * 255);
        int g = (int) (Math.min(1.0, Math.max(0.0, bVal * 3.0 - 1.0)) * 255);
        int b = (int) (Math.min(1.0, Math.max(0.0, bVal * 2.0))        * 255);
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Return the A concentration at grid cell (row i, column j).
     *
     * @param i row index in [0, rows)
     * @param j column index in [0, cols)
     * @return A concentration in [0, 1]
     */
    public double getA(int i, int j) { return a[i][j]; }

    /**
     * Return the B concentration at grid cell (row i, column j).
     *
     * @param i row index in [0, rows)
     * @param j column index in [0, cols)
     * @return B concentration in [0, 1]
     */
    public double getB(int i, int j) { return b[i][j]; }

    /** @return diffusion coefficient for chemical A */
    public double getDa() { return Da; }

    /** @return diffusion coefficient for chemical B */
    public double getDb() { return Db; }

    /** @return feed rate F */
    public double getF()  { return F; }

    /** @return kill rate k */
    public double getK()  { return k; }

    /**
     * Set the diffusion coefficient for chemical A.
     *
     * @param Da new value; should be positive and larger than {@link #getDb()}
     */
    public void setDa(double Da) { this.Da = Da; }

    /**
     * Set the diffusion coefficient for chemical B.
     *
     * @param Db new value; should be positive and smaller than {@link #getDa()}
     */
    public void setDb(double Db) { this.Db = Db; }

    /**
     * Set the feed rate.
     *
     * @param F new value; dimensionless, typically 0.01–0.08
     */
    public void setF(double F) { this.F = F; }

    /**
     * Set the kill rate.
     *
     * @param k new value; dimensionless, typically 0.04–0.075
     */
    public void setK(double k) { this.k = k; }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    private static double clamp(double v) {
        return v < 0.0 ? 0.0 : (v > 1.0 ? 1.0 : v);
    }
}
