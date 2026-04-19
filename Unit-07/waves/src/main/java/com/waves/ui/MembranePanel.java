package com.waves.ui;

import com.waves.model.WaveMembrane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Simulation canvas for the 2D vibrating membrane.
 *
 * <p>Renders the membrane as a false-colour {@link BufferedImage} scaled to
 * fill the panel.  Positive displacements appear red; negative displacements
 * appear blue; zero displacement is black.
 *
 * <p>Ten physics substeps are performed per 16 ms frame to achieve reasonable
 * visual wave speed while maintaining numerical stability.
 *
 * <p>Click anywhere on the canvas to poke the membrane at that position.
 */
public class MembranePanel extends JPanel implements MouseListener {

    private static final int W = 720;
    private static final int H = 480;

    /** Number of physics substeps per rendered frame. */
    private static final int SUBSTEPS = 10;

    private static final Color BG        = new Color(8,  10, 22);
    private static final Color TEXT_COLOR = new Color(180, 190, 210);

    // Control values set by MembraneControls
    double waveSpeed = 200.0;
    double damping   = 0.001;
    double amplitude = 1.0;

    private final WaveMembrane model;
    private final Timer        gameLoop;

    /**
     * Construct a membrane panel bound to the given model.
     *
     * @param model the {@link WaveMembrane} physics model
     */
    public MembranePanel(WaveMembrane model) {
        this.model = model;
        setPreferredSize(new Dimension(W, H));
        setBackground(BG);
        addMouseListener(this);

        gameLoop = new Timer(16, e -> {
            for (int s = 0; s < SUBSTEPS; s++) {
                model.step(waveSpeed, damping);
            }
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

        // Render model to image and scale to panel
        BufferedImage img = model.renderToImage(W, H);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, 0, 0, W, H, null);

        drawHud(g2);
    }

    private void drawHud(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(TEXT_COLOR);
        int x = 10, y = 18;
        g2.drawString(String.format("Wave speed : %5.0f px/s", waveSpeed), x, y);
        g2.drawString(String.format("Damping    : %.4f / step", damping),  x, y + 16);
        g2.drawString(String.format("Grid       : %d × %d",
                                    WaveMembrane.COLS, WaveMembrane.ROWS), x, y + 32);
        g2.drawString("Click to poke membrane", x, y + 48);
    }

    // -------------------------------------------------------------------------
    // Mouse — click to poke
    // -------------------------------------------------------------------------

    @Override
    public void mouseClicked(MouseEvent e) {
        // Map panel pixel to grid coordinates
        int cx = (int) ((double) e.getX() / W * WaveMembrane.COLS);
        int cy = (int) ((double) e.getY() / H * WaveMembrane.ROWS);
        cx = Math.max(1, Math.min(WaveMembrane.COLS - 2, cx));
        cy = Math.max(1, Math.min(WaveMembrane.ROWS - 2, cy));
        model.poke(cx, cy, amplitude, 8.0);
    }

    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
}
