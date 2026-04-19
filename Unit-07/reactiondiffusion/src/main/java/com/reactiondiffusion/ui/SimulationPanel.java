package com.reactiondiffusion.ui;

import com.reactiondiffusion.model.ReactionDiffusionGrid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Simulation canvas for the Gray-Scott reaction-diffusion model.
 *
 * <p>Renders the grid at native resolution ({@code cols × rows} pixels) into a
 * {@link BufferedImage}, then scales it to fill the panel with
 * {@link Graphics2D#drawImage}.  A HUD in the top-left corner displays the
 * current F, k, Da, Db values and the step count.
 *
 * <h2>Interaction</h2>
 * <ul>
 *   <li>Click anywhere on the canvas to seed additional B concentration
 *       centred on the clicked cell.</li>
 * </ul>
 *
 * <h2>Timing</h2>
 * <p>A {@link Timer} fires every 16 ms (~60 fps).  Each tick runs
 * {@code substeps} simulation steps before repainting.
 */
public class SimulationPanel extends JPanel implements MouseListener {

    /** Canvas width in pixels (grid cols × scale factor). */
    static final int W = 700;

    /** Canvas height in pixels (grid rows × scale factor). */
    static final int H = 525;

    private static final Color BG        = new Color(8,  10, 22);
    private static final Color TEXT_COLOR = new Color(180, 190, 210);

    private final ReactionDiffusionGrid grid;
    private final BufferedImage         image;
    private final int[]                 pixelBuffer;
    private final Timer                 gameLoop;
    private final Random                rng = new Random();

    private int substeps = 15;
    private long stepCount = 0;

    /**
     * Construct the simulation panel and start the animation loop.
     *
     * @param grid the reaction-diffusion model to visualise
     */
    public SimulationPanel(ReactionDiffusionGrid grid) {
        this.grid        = grid;
        this.image       = new BufferedImage(grid.cols, grid.rows, BufferedImage.TYPE_INT_RGB);
        this.pixelBuffer = new int[grid.cols * grid.rows];

        setPreferredSize(new Dimension(W, H));
        setBackground(BG);
        addMouseListener(this);

        gameLoop = new Timer(16, e -> {
            grid.step(substeps);
            stepCount += substeps;
            grid.renderToImage(pixelBuffer, grid.cols, grid.rows);
            image.setRGB(0, 0, grid.cols, grid.rows, pixelBuffer, 0, grid.cols);
            repaint();
        });
        gameLoop.start();
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        drawHud(g2);
    }

    private void drawHud(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRect(0, 0, 230, 88);
        g2.setColor(TEXT_COLOR);
        int x = 8, y = 18;
        g2.drawString(String.format("F  = %.4f", grid.getF()),   x, y);
        g2.drawString(String.format("k  = %.4f", grid.getK()),   x, y + 16);
        g2.drawString(String.format("Da = %.3f",  grid.getDa()), x, y + 32);
        g2.drawString(String.format("Db = %.3f",  grid.getDb()), x, y + 48);
        g2.drawString(String.format("step %,d  (%d/frame)", stepCount, substeps), x, y + 64);
    }

    // -------------------------------------------------------------------------
    // Mouse — click to seed B concentration
    // -------------------------------------------------------------------------

    @Override
    public void mouseClicked(MouseEvent e) {
        // Map panel pixel → grid cell
        int cx = (int) ((double) e.getX() / getWidth()  * grid.cols);
        int cy = (int) ((double) e.getY() / getHeight() * grid.rows);
        grid.seed(cx, cy, 5, rng);
    }

    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}

    // -------------------------------------------------------------------------
    // Control surface API
    // -------------------------------------------------------------------------

    /**
     * Set the number of simulation substeps executed per animation frame.
     *
     * @param substeps value in [1, 30]
     */
    public void setSubsteps(int substeps) { this.substeps = substeps; }

    /** @return current substeps-per-frame value */
    public int getSubsteps() { return substeps; }

    /** Reset the step counter displayed in the HUD. */
    public void resetStepCount() { stepCount = 0; }
}
