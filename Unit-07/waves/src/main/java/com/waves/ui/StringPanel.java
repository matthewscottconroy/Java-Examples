package com.waves.ui;

import com.waves.model.WaveString;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Simulation canvas for the 1D vibrating string.
 *
 * <p>Renders the string as a coloured polyline centred vertically in the panel.
 * Each segment is coloured from blue (negative displacement) through white
 * (zero) to red (positive displacement) based on the local displacement value.
 *
 * <p>Click anywhere on the canvas to pluck the string at that x-position with
 * a Gaussian bump.
 */
public class StringPanel extends JPanel implements MouseListener {

    private static final int W = 800;
    private static final int H = 400;

    private static final Color BG        = new Color(8,  10, 22);
    private static final Color TEXT_COLOR = new Color(180, 190, 210);
    private static final Color AXIS_COLOR = new Color(40, 50, 80);

    /** Pixel scale: displacement value 1.0 maps to this many pixels. */
    private static final double DEFAULT_SCALE = 120.0;

    // Control values set by StringControls
    double waveSpeed  = 200.0;
    double damping    = 0.001;
    double amplitude  = 0.6;

    private double scale = DEFAULT_SCALE;

    private final WaveString model;
    private final Timer      gameLoop;

    /**
     * Construct a string panel bound to the given model.
     *
     * @param model the {@link WaveString} physics model
     */
    public StringPanel(WaveString model) {
        this.model = model;
        setPreferredSize(new Dimension(W, H));
        setBackground(BG);
        addMouseListener(this);

        gameLoop = new Timer(16, e -> {
            model.step(waveSpeed, damping);
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);

        drawGrid(g2);
        drawString(g2);
        drawHud(g2);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(AXIS_COLOR);
        // Horizontal centre axis
        g2.drawLine(0, H / 2, W, H / 2);
        // Light vertical grid lines
        g2.setColor(new Color(25, 28, 45));
        for (int x = 0; x < W; x += 80) g2.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 50) g2.drawLine(0, y, W, y);
    }

    private void drawString(Graphics2D g2) {
        int n = WaveString.N;
        // Build polyline coordinates
        int[] px = new int[n];
        int[] py = new int[n];
        for (int i = 0; i < n; i++) {
            px[i] = (int) ((double) i / (n - 1) * W);
            py[i] = (int) (H / 2.0 - model.getDisplacement(i) * scale);
        }

        // Draw coloured segments
        Stroke prev = g2.getStroke();
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 1; i < n; i++) {
            double val = model.getDisplacement(i);
            g2.setColor(displacementColor(val));
            g2.drawLine(px[i - 1], py[i - 1], px[i], py[i]);
        }
        g2.setStroke(prev);

        // Endpoint markers
        g2.setColor(new Color(160, 170, 190));
        g2.fillOval(px[0] - 4, py[0] - 4, 8, 8);
        g2.fillOval(px[n - 1] - 4, py[n - 1] - 4, 8, 8);
    }

    /**
     * Map a displacement value to a display colour.
     *
     * <ul>
     *   <li>Positive → red family</li>
     *   <li>Near zero → white</li>
     *   <li>Negative → blue family</li>
     * </ul>
     */
    private Color displacementColor(double val) {
        // Normalise to [-1, 1] using expected range ~0.8
        double norm = Math.max(-1.0, Math.min(1.0, val / 0.8));
        if (norm >= 0.0) {
            int r = 255;
            int g = (int) (255 * (1.0 - norm));
            int b = (int) (255 * (1.0 - norm));
            return new Color(r, g, b);
        } else {
            int r = (int) (255 * (1.0 + norm));
            int g = (int) (255 * (1.0 + norm));
            int b = 255;
            return new Color(r, g, b);
        }
    }

    private void drawHud(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(TEXT_COLOR);
        int x = 12, y = 20;
        g2.drawString(String.format("Wave speed : %5.0f px/s", waveSpeed), x, y);
        g2.drawString(String.format("Damping    : %.4f / step",  damping),  x, y + 16);
        g2.drawString(String.format("Energy     : %10.3e",       model.energy()), x, y + 32);
        g2.drawString("Click to pluck", x, y + 48);
    }

    // -------------------------------------------------------------------------
    // Mouse — click to pluck
    // -------------------------------------------------------------------------

    @Override
    public void mouseClicked(MouseEvent e) {
        int gridX = (int) ((double) e.getX() / W * (WaveString.N - 1));
        gridX = Math.max(1, Math.min(WaveString.N - 2, gridX));
        model.pluck(gridX, amplitude, 18.0);
    }

    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
}
