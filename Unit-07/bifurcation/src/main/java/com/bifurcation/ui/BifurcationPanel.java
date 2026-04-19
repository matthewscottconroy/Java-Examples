package com.bifurcation.ui;

import com.bifurcation.model.LogisticMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulation canvas for the logistic map bifurcation diagram.
 *
 * <h2>Bifurcation Diagram</h2>
 * <p>The diagram is pre-rendered into a {@link BufferedImage}. For each pixel
 * column {@code x} corresponding to an r value, the map is iterated for a
 * configurable number of transient steps (to let the orbit settle onto its
 * attractor), then the next {@code attractorSteps} iterates are plotted as
 * individual pixels.
 *
 * <h2>Staircase Inset</h2>
 * <p>When the user clicks on the diagram a specific r value is selected. A small
 * inset in the bottom-right corner draws the orbit as a staircase on the parabola
 * {@code y = r·x·(1−x)}, showing the last 20 iterates. This is the classic
 * cobweb diagram — a powerful visual for understanding orbit structure.
 *
 * <h2>Recomputation</h2>
 * <p>The image is recomputed whenever {@link #recompute()} is called, typically
 * triggered by the Reset/Recompute button or when sliders change significantly.
 */
public class BifurcationPanel extends JPanel implements MouseListener {

    static final int W = 700;
    static final int H = 500;

    private static final Color BG         = new Color(8, 10, 22);
    private static final Color DOT_COLOR  = new Color(180, 220, 100, 150);
    private static final Color TEXT_COLOR = new Color(180, 190, 210);
    private static final Color AXIS_COLOR = new Color(60, 70, 100);
    private static final Color ORBIT_COLOR = new Color(255, 100, 60, 200);
    private static final Color PARABOLA_COLOR = new Color(60, 160, 220, 200);
    private static final Color DIAGONAL_COLOR = new Color(80, 80, 100, 180);

    private final LogisticMap model;

    // Diagram parameters (updated by controls)
    double rMin = 2.4;
    double rMax = 4.0;
    int    transientSteps  = 500;
    int    attractorSteps  = 200;

    private BufferedImage diagramImage;
    private boolean       imageDirty = true;

    // Selected r for the staircase inset (-1 = none selected)
    private double selectedR = -1;

    public BifurcationPanel(LogisticMap model) {
        this.model = model;
        setPreferredSize(new Dimension(W, H));
        setBackground(BG);
        addMouseListener(this);
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Mark the pre-rendered image as stale so it will be recomputed on next paint. */
    public void recompute() {
        imageDirty = true;
        repaint();
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (imageDirty) {
            buildDiagramImage();
            imageDirty = false;
        }

        g2.drawImage(diagramImage, 0, 0, null);
        drawAxes(g2);
        drawHud(g2);

        if (selectedR >= rMin && selectedR <= rMax) {
            drawOrbitInset(g2);
        }
    }

    /**
     * Pre-render the full bifurcation diagram into a {@link BufferedImage}.
     *
     * <p>For each pixel column {@code px} the corresponding r value is computed,
     * the map is run for {@code transientSteps} (discarded), then
     * {@code attractorSteps} iterates are plotted as single-pixel dots at the
     * y position corresponding to the attractor x value.
     */
    private void buildDiagramImage() {
        diagramImage = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = diagramImage.createGraphics();
        g2.setColor(BG);
        g2.fillRect(0, 0, W, H);

        int dotRgba = DOT_COLOR.getRGB();

        for (int px = 0; px < W; px++) {
            double rVal = rMin + px * (rMax - rMin) / (W - 1.0);
            double x = 0.5;

            // Discard transients
            for (int t = 0; t < transientSteps; t++) {
                x = rVal * x * (1.0 - x);
            }

            // Plot attractor points
            for (int a = 0; a < attractorSteps; a++) {
                x = rVal * x * (1.0 - x);
                int py = H - 1 - (int)(x * (H - 1));
                py = Math.max(0, Math.min(H - 1, py));
                // Blend with existing pixel for alpha compositing
                int existing = diagramImage.getRGB(px, py);
                int blended  = alphaBlend(existing, dotRgba);
                diagramImage.setRGB(px, py, blended);
            }
        }

        g2.dispose();
    }

    /** Simple source-over alpha blend of two ARGB packed ints. */
    private int alphaBlend(int dst, int src) {
        int sa = (src >> 24) & 0xFF;
        if (sa == 0) return dst;
        if (sa == 255) return src;
        int da = (dst >> 24) & 0xFF;
        int sr = (src >> 16) & 0xFF;
        int sg = (src >>  8) & 0xFF;
        int sb =  src        & 0xFF;
        int dr = (dst >> 16) & 0xFF;
        int dg = (dst >>  8) & 0xFF;
        int db =  dst        & 0xFF;
        int oa = sa + da * (255 - sa) / 255;
        if (oa == 0) return 0;
        int or_ = (sr * sa + dr * da * (255 - sa) / 255) / oa;
        int og  = (sg * sa + dg * da * (255 - sa) / 255) / oa;
        int ob  = (sb * sa + db * da * (255 - sa) / 255) / oa;
        return (oa << 24) | (clamp(or_) << 16) | (clamp(og) << 8) | clamp(ob);
    }

    private int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    private void drawAxes(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(AXIS_COLOR);

        // r-axis labels (bottom)
        int numLabels = 8;
        for (int i = 0; i <= numLabels; i++) {
            double rVal = rMin + i * (rMax - rMin) / numLabels;
            int px = (int)(i * (W - 1.0) / numLabels);
            g2.drawLine(px, H - 14, px, H - 8);
            g2.setColor(TEXT_COLOR);
            String label = String.format("%.2f", rVal);
            g2.drawString(label, px - 12, H - 2);
            g2.setColor(AXIS_COLOR);
        }

        // x-axis labels (left side)
        for (int i = 0; i <= 4; i++) {
            double xVal = i * 0.25;
            int py = H - 1 - (int)(xVal * (H - 1));
            g2.drawLine(0, py, 6, py);
            g2.setColor(TEXT_COLOR);
            g2.drawString(String.format("%.2f", xVal), 8, py + 4);
            g2.setColor(AXIS_COLOR);
        }

        // Axis labels
        g2.setColor(TEXT_COLOR);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString("r →", W - 30, H - 2);
        g2.drawString("x", 1, 14);
    }

    private void drawHud(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(TEXT_COLOR);
        int x = W - 280, y = 18;
        g2.drawString(String.format("r range: [%.3f, %.3f]", rMin, rMax), x, y);
        g2.drawString(String.format("Transient: %d   Attractor: %d", transientSteps, attractorSteps), x, y + 16);
        g2.drawString(String.format("Feigenbaum δ = %.7f", LogisticMap.FEIGENBAUM_DELTA), x, y + 32);
        g2.drawString("r≈3.000: period 2", x, y + 52);
        g2.drawString("r≈3.449: period 4", x, y + 68);
        g2.drawString("r≈3.544: period 8", x, y + 84);
        g2.drawString("r≈3.569: chaos", x, y + 100);

        if (selectedR >= 0) {
            g2.setColor(new Color(255, 200, 60));
            g2.drawString(String.format("Selected r = %.4f", selectedR), x, y + 120);
            model.setR(selectedR);
            boolean chaotic = model.isChaotic(selectedR, 2000);
            g2.setColor(chaotic ? new Color(255, 100, 60) : new Color(100, 220, 100));
            g2.drawString(chaotic ? "CHAOTIC" : "periodic / fixed", x, y + 136);
        }

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(140, 150, 170));
        g2.drawString("Click diagram to select r and show cobweb inset", 12, H - 20);
    }

    /**
     * Draw the cobweb (staircase) inset for the selected r value.
     *
     * <p>The inset is a square region in the bottom-right corner showing the
     * parabola {@code y = r·x·(1−x)}, the diagonal {@code y = x}, and the
     * orbit trajectory as a staircase (vertical then horizontal segments),
     * which is the standard visual for tracing successive iterates.
     */
    private void drawOrbitInset(Graphics2D g2) {
        int IW = 180, IH = 180;
        int IX = W - IW - 10, IY = H - IH - 30;

        // Background
        g2.setColor(new Color(10, 12, 28, 230));
        g2.fillRect(IX, IY, IW, IH);
        g2.setColor(new Color(60, 70, 100));
        g2.drawRect(IX, IY, IW, IH);

        // Label
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(TEXT_COLOR);
        g2.drawString(String.format("Cobweb  r=%.3f", selectedR), IX + 4, IY + 12);

        // Coordinate transform: map [0,1] × [0,1] → inset pixels
        int margin = 16;
        int inner  = IW - 2 * margin;

        // Draw diagonal y=x
        g2.setColor(DIAGONAL_COLOR);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawLine(IX + margin, IY + margin + inner,
                    IX + margin + inner, IY + margin);

        // Draw parabola y = r*x*(1-x)
        g2.setColor(PARABOLA_COLOR);
        g2.setStroke(new BasicStroke(1.5f));
        int prevPX = -1, prevPY = -1;
        int pts = 100;
        for (int i = 0; i <= pts; i++) {
            double xv = i / (double) pts;
            double yv = selectedR * xv * (1.0 - xv);
            int px = IX + margin + (int)(xv * inner);
            int py = IY + margin + inner - (int)(yv * inner);
            py = Math.max(IY + margin, Math.min(IY + margin + inner, py));
            if (prevPX >= 0) g2.drawLine(prevPX, prevPY, px, py);
            prevPX = px; prevPY = py;
        }

        // Draw cobweb staircase: warm up, then trace 20 orbit steps
        double x = 0.5;
        for (int i = 0; i < 200; i++) x = selectedR * x * (1.0 - x);

        List<double[]> orbitPts = new ArrayList<>();
        orbitPts.add(new double[]{x, 0});  // start on diagonal at (x, x) via (x,0)

        for (int i = 0; i < 20; i++) {
            double fx = selectedR * x * (1.0 - x);
            orbitPts.add(new double[]{x, fx});  // vertical: to (x, f(x)) on parabola
            orbitPts.add(new double[]{fx, fx}); // horizontal: to (f(x), f(x)) on diagonal
            x = fx;
        }

        g2.setColor(ORBIT_COLOR);
        g2.setStroke(new BasicStroke(1.2f));
        for (int i = 1; i < orbitPts.size(); i++) {
            double[] a = orbitPts.get(i - 1);
            double[] b = orbitPts.get(i);
            int ax = IX + margin + (int)(a[0] * inner);
            int ay = IY + margin + inner - (int)(a[1] * inner);
            int bx = IX + margin + (int)(b[0] * inner);
            int by = IY + margin + inner - (int)(b[1] * inner);
            ay = Math.max(IY, Math.min(IY + IH, ay));
            by = Math.max(IY, Math.min(IY + IH, by));
            g2.drawLine(ax, ay, bx, by);
        }

        g2.setStroke(new BasicStroke());
    }

    // -------------------------------------------------------------------------
    // Mouse — click to select r value
    // -------------------------------------------------------------------------

    @Override
    public void mouseClicked(MouseEvent e) {
        int px = e.getX();
        selectedR = rMin + px * (rMax - rMin) / (W - 1.0);
        selectedR = Math.max(rMin, Math.min(rMax, selectedR));
        repaint();
    }

    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
}
