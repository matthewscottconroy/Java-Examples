package com.orbitaldynamics.twobody.ui;

import com.orbitaldynamics.twobody.OrbitalElements;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Left panel for the two-body analytical app.
 *
 * <p>Contains three sections:
 * <ol>
 *   <li>Initial conditions (positions and velocities)</li>
 *   <li>System parameters (masses, G)</li>
 *   <li>Orbital elements display (computed, read-only)</li>
 * </ol>
 *
 * <p>Notifies a listener via {@link #setOnParametersChanged(Consumer)} whenever
 * the user changes any input.
 */
public final class ParameterPanel extends JPanel {

    private static final Color BG       = new Color(15, 17, 28);
    private static final Color BG_INPUT = new Color(22, 25, 40);
    private static final Color FG       = new Color(190, 210, 235);
    private static final Color ACCENT   = new Color(80, 160, 255);
    private static final Color DIM      = new Color(110, 125, 150);
    private static final Font  MONO     = new Font(Font.MONOSPACED, Font.PLAIN, 11);
    private static final Font  LABEL_F  = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    private static final Font  HEADER_F = new Font(Font.SANS_SERIF, Font.BOLD, 11);

    // ── Body 1 ──────────────────────────────────────────────────────────────
    private final JSpinner b1x  = spin(-250, -5000, 5000, 10);
    private final JSpinner b1y  = spin(0,    -5000, 5000, 10);
    private final JSpinner b1vx = spin(0,    -2000, 2000, 10);
    private final JSpinner b1vy = spin(-80,  -2000, 2000, 10);
    private final JSpinner m1   = spin(5000,    1, 200000, 500);

    // ── Body 2 ──────────────────────────────────────────────────────────────
    private final JSpinner b2x  = spin(250,  -5000, 5000, 10);
    private final JSpinner b2y  = spin(0,    -5000, 5000, 10);
    private final JSpinner b2vx = spin(0,    -2000, 2000, 10);
    private final JSpinner b2vy = spin(80,   -2000, 2000, 10);
    private final JSpinner m2   = spin(5000,    1, 200000, 500);

    // ── G ───────────────────────────────────────────────────────────────────
    private final JSpinner gSpin = spin(5000, 100, 500000, 500);

    // ── Elements display ─────────────────────────────────────────────────────
    private final JTextArea elementsArea = makeTextArea();

    private Consumer<Void> onChanged;

    public ParameterPanel() {
        setBackground(BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(6, 6, 6, 6));
        setPreferredSize(new Dimension(250, 600));

        add(section("Body 1", buildBodyGrid(b1x, b1y, b1vx, b1vy, m1)));
        add(Box.createVerticalStrut(4));
        add(section("Body 2", buildBodyGrid(b2x, b2y, b2vx, b2vy, m2)));
        add(Box.createVerticalStrut(4));
        add(gSection());
        add(Box.createVerticalStrut(4));
        add(elementsSection());
        add(Box.createVerticalGlue());

        // Attach change listeners to all spinners
        for (JSpinner s : new JSpinner[]{b1x, b1y, b1vx, b1vy, m1, b2x, b2y, b2vx, b2vy, m2, gSpin}) {
            s.addChangeListener(e -> { if (onChanged != null) onChanged.accept(null); });
        }
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public double getB1x()  { return num(b1x);  }
    public double getB1y()  { return num(b1y);  }
    public double getB1vx() { return num(b1vx); }
    public double getB1vy() { return num(b1vy); }
    public double getM1()   { return num(m1);   }
    public double getB2x()  { return num(b2x);  }
    public double getB2y()  { return num(b2y);  }
    public double getB2vx() { return num(b2vx); }
    public double getB2vy() { return num(b2vy); }
    public double getM2()   { return num(m2);   }
    public double getG()    { return num(gSpin); }

    public void setOnParametersChanged(Consumer<Void> listener) { this.onChanged = listener; }

    /** Updates the orbital elements display. */
    public void showElements(OrbitalElements el) {
        if (el == null) {
            elementsArea.setText("  (invalid)");
            return;
        }
        String bound = el.isBound()
            ? String.format("  T: %12.2f s%n", el.period())
            : "  T:   (unbound)%n";
        String apStr = Double.isInfinite(el.apoapsis())
            ? "  ra: ∞%n"
            : String.format("  ra: %11.1f px%n", el.apoapsis());

        elementsArea.setText(String.format(
            "  Type: %s%n%n" +
            "  μ:  %12.2f%n" +
            "  h:  %12.2e%n" +
            "  e:  %12.6f%n" +
            "  E:  %12.4e%n%n" +
            "  a:  %11.1f px%n" +
            "  rp: %11.1f px%n" +
            apStr +
            bound +
            "  ω:  %11.1f °%n" +
            "  f₀: %11.1f °",
            el.orbitType().label,
            el.mu(), el.h(), el.eccentricity(), el.specificEnergy(),
            el.semiMajorAxis(), el.periapsis(),
            Math.toDegrees(el.periapsisAngle()),
            Math.toDegrees(el.trueAnomalyAtEpoch())
        ));
    }

    // -------------------------------------------------------------------------
    // Layout helpers
    // -------------------------------------------------------------------------

    private JPanel buildBodyGrid(JSpinner px, JSpinner py,
                                  JSpinner vx, JSpinner vy, JSpinner mass) {
        JPanel g = new JPanel(new GridLayout(5, 2, 4, 3));
        g.setBackground(BG);
        addRow(g, "x₀ (px):", px);
        addRow(g, "y₀ (px):", py);
        addRow(g, "vx (px/s):", vx);
        addRow(g, "vy (px/s):", vy);
        addRow(g, "mass:", mass);
        return g;
    }

    private void addRow(JPanel grid, String labelText, JSpinner spinner) {
        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(DIM);
        lbl.setFont(LABEL_F);
        grid.add(lbl);
        styleSpinner(spinner);
        grid.add(spinner);
    }

    private JPanel gSection() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        p.setBackground(BG);
        JLabel lbl = new JLabel("G =");
        lbl.setForeground(DIM);
        lbl.setFont(LABEL_F);
        styleSpinner(gSpin);
        p.add(lbl);
        p.add(gSpin);
        return wrapSection("System", p);
    }

    private JPanel elementsSection() {
        elementsArea.setRows(14);
        JScrollPane sp = new JScrollPane(elementsArea);
        sp.setBorder(null);
        sp.setBackground(BG);
        return wrapSection("Orbital Elements", sp);
    }

    private JPanel section(String title, JComponent content) {
        return wrapSection(title, content);
    }

    private JPanel wrapSection(String title, JComponent content) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG);
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(40, 50, 75), 1), title);
        tb.setTitleColor(ACCENT);
        tb.setTitleFont(HEADER_F);
        outer.setBorder(BorderFactory.createCompoundBorder(tb, new EmptyBorder(4, 4, 4, 4)));
        outer.add(content, BorderLayout.CENTER);
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, outer.getPreferredSize().height + 40));
        return outer;
    }

    // -------------------------------------------------------------------------
    // Widget factories
    // -------------------------------------------------------------------------

    private static JSpinner spin(double val, double min, double max, double step) {
        return new JSpinner(new SpinnerNumberModel(val, min, max, step));
    }

    private static void styleSpinner(JSpinner sp) {
        sp.setPreferredSize(new Dimension(90, 22));
        sp.setBackground(BG_INPUT);
        if (sp.getEditor() instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(BG_INPUT);
            de.getTextField().setForeground(FG);
            de.getTextField().setFont(MONO);
        }
    }

    private static JTextArea makeTextArea() {
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setBackground(new Color(20, 22, 35));
        ta.setForeground(new Color(180, 200, 220));
        ta.setFont(MONO);
        ta.setBorder(new EmptyBorder(2, 4, 2, 4));
        return ta;
    }

    private static double num(JSpinner sp) {
        return ((Number) sp.getValue()).doubleValue();
    }
}
