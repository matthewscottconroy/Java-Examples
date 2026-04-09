package com.buoyancy.equation.ui;

import com.buoyancy.equation.model.BuoyancyObject;
import com.buoyancy.equation.model.Fluid;
import com.buoyancy.equation.model.ObjectPreset;
import com.buoyancy.equation.physics.BuoyancyPhysics;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Right-side control panel for the equation-based buoyancy simulator.
 *
 * <p>Divided into four sections:
 * <ol>
 *   <li>Object management buttons</li>
 *   <li>Global environment settings (gravity, fluid density, fluid level)</li>
 *   <li>Selected-object settings (material, density, radius, height)</li>
 *   <li>Live metrics panel (forces, velocity, submersion, status)</li>
 * </ol>
 */
public class ControlPanel extends JPanel {

    private final SimulationPanel sim;

    // ── Global controls ───────────────────────────────────────────────────────
    private final JSlider gSlider   = new JSlider(10, 250, 98);       // ×0.1 m/s²
    private final JLabel  gVal      = new JLabel("9.8 m/s²");
    private final JSlider rhofSlider= new JSlider(100, 14000, 1000);  // kg/m³
    private final JLabel  rhofVal   = new JLabel("1000 kg/m³");
    private final JSlider surfSlider= new JSlider(80, 600, SimulationPanel.DEFAULT_SURF_Y);
    private final JLabel  surfVal   = new JLabel(SimulationPanel.DEFAULT_SURF_Y + " px");

    // ── Object controls ───────────────────────────────────────────────────────
    private final JComboBox<ObjectPreset> presetCombo = new JComboBox<>(ObjectPreset.values());
    private final JSlider rhoObjSlider = new JSlider(50, 14000, 530);   // kg/m³
    private final JLabel  rhoObjVal   = new JLabel("530 kg/m³");
    private final JSlider radiusSlider = new JSlider(5, 60, 22);        // ×0.01 m
    private final JLabel  radiusVal   = new JLabel("0.22 m");
    private final JSlider heightSlider = new JSlider(5, 100, 44);       // ×0.01 m
    private final JLabel  heightVal   = new JLabel("0.44 m");
    private final JButton deleteBtn   = new JButton("Delete Object");
    private final JLabel  objTitle    = new JLabel("No object selected");
    private JPanel objPanel;

    // ── Live metrics ──────────────────────────────────────────────────────────
    private final JLabel fbLabel     = metricLabel("—");
    private final JLabel fgLabel     = metricLabel("—");
    private final JLabel fnLabel     = metricLabel("—");
    private final JLabel velLabel    = metricLabel("—");
    private final JLabel subLabel    = metricLabel("—");
    private final JLabel statusLabel = metricLabel("—");
    private final JLabel massLabel   = metricLabel("—");
    private final JLabel volLabel    = metricLabel("—");
    private final JLabel densRatioLbl= metricLabel("—");

    private boolean updatingFromCode = false;

    // ── Construction ──────────────────────────────────────────────────────────

    public ControlPanel(SimulationPanel sim) {
        this.sim = sim;
        setPreferredSize(new Dimension(320, SimulationPanel.CANVAS_H));
        setBackground(new Color(28, 32, 48));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildButtonPanel());
        add(Box.createVerticalStrut(6));
        add(buildGlobalPanel());
        add(Box.createVerticalStrut(6));
        objPanel = buildObjectPanel();
        add(objPanel);
        add(Box.createVerticalStrut(6));
        add(buildMetricsPanel());
        add(Box.createVerticalGlue());

        wireGlobalSliders();
        wireObjectControls();
        updateObjectPanel();
    }

    // ── Buttons ───────────────────────────────────────────────────────────────

    private JPanel buildButtonPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);

        JButton addBtn  = styledBtn("+ Add Object", new Color(70, 140, 70));
        JButton pauseBtn = styledBtn("⏸ Pause", new Color(80, 80, 130));
        JButton resetBtn = styledBtn("↺ Reset Physics", new Color(100, 70, 30));

        addBtn.addActionListener(e -> { sim.addObject(); refresh(); });
        pauseBtn.addActionListener(e -> {
            sim.togglePause();
            pauseBtn.setText(sim.isPaused() ? "▶ Resume" : "⏸ Pause");
        });
        resetBtn.addActionListener(e -> sim.resetPhysics());

        p.add(addBtn); p.add(pauseBtn); p.add(resetBtn);
        return p;
    }

    // ── Global settings ───────────────────────────────────────────────────────

    private JPanel buildGlobalPanel() {
        JPanel p = section("Environment");
        p.add(sliderRow("Gravity (g)", gSlider, gVal));
        p.add(Box.createVerticalStrut(4));
        p.add(sliderRow("Fluid density (ρ_f)", rhofSlider, rhofVal));
        p.add(Box.createVerticalStrut(4));
        p.add(sliderRow("Fluid surface level", surfSlider, surfVal));

        // Fluid preset buttons
        JPanel presets = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        presets.setOpaque(false);
        presets.add(small("Fresh Water", () -> { rhofSlider.setValue(1000); }));
        presets.add(small("Sea Water",   () -> { rhofSlider.setValue(1025); }));
        presets.add(small("Oil",         () -> { rhofSlider.setValue(850); }));
        presets.add(small("Mercury",     () -> { rhofSlider.setValue(13534); }));
        p.add(presets);
        return p;
    }

    // ── Object settings ───────────────────────────────────────────────────────

    private JPanel buildObjectPanel() {
        JPanel p = section("Selected Object");

        objTitle.setForeground(new Color(180, 200, 255));
        objTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        p.add(objTitle);
        p.add(Box.createVerticalStrut(4));

        JPanel presetRow = new JPanel(new BorderLayout(6, 0));
        presetRow.setOpaque(false);
        JLabel presetLbl = new JLabel("Material:");
        presetLbl.setForeground(Color.LIGHT_GRAY);
        presetRow.add(presetLbl, BorderLayout.WEST);
        presetCombo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        presetCombo.setBackground(new Color(45, 50, 70));
        presetCombo.setForeground(Color.WHITE);
        presetRow.add(presetCombo, BorderLayout.CENTER);
        p.add(presetRow);
        p.add(Box.createVerticalStrut(4));
        p.add(sliderRow("Object density (ρ_o)  kg/m³", rhoObjSlider, rhoObjVal));
        p.add(Box.createVerticalStrut(4));
        p.add(sliderRow("Radius (r)   m", radiusSlider, radiusVal));
        p.add(Box.createVerticalStrut(4));
        p.add(sliderRow("Height (h)   m", heightSlider, heightVal));
        p.add(Box.createVerticalStrut(6));
        deleteBtn.setBackground(new Color(140, 50, 50));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.addActionListener(e -> { sim.removeSelected(); refresh(); });
        p.add(deleteBtn);
        return p;
    }

    // ── Live metrics ──────────────────────────────────────────────────────────

    private JPanel buildMetricsPanel() {
        JPanel p = section("Live Metrics");
        p.setLayout(new GridLayout(0, 2, 4, 3));

        addMetricRow(p, "F_buoyancy ↑:",   fbLabel);
        addMetricRow(p, "F_gravity ↓:",    fgLabel);
        addMetricRow(p, "F_net:",          fnLabel);
        addMetricRow(p, "Velocity:",        velLabel);
        addMetricRow(p, "Submersion:",      subLabel);
        addMetricRow(p, "Mass:",            massLabel);
        addMetricRow(p, "Volume:",          volLabel);
        addMetricRow(p, "ρ_obj / ρ_fluid:", densRatioLbl);
        addMetricRow(p, "Status:",          statusLabel);
        return p;
    }

    private void addMetricRow(JPanel p, String name, JLabel val) {
        JLabel lbl = new JLabel(name);
        lbl.setForeground(new Color(170, 190, 220));
        lbl.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        val.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        p.add(lbl);
        p.add(val);
    }

    // ── Wiring ────────────────────────────────────────────────────────────────

    private void wireGlobalSliders() {
        gSlider.addChangeListener(e -> {
            double g = gSlider.getValue() * 0.1;
            sim.setGravity(g);
            gVal.setText(String.format("%.1f m/s²", g));
        });

        rhofSlider.addChangeListener(e -> {
            sim.getFluid().setDensityKgM3(rhofSlider.getValue());
            rhofVal.setText(rhofSlider.getValue() + " kg/m³");
        });

        surfSlider.addChangeListener(e -> {
            sim.getFluid().setSurfaceY(surfSlider.getValue());
            surfVal.setText(surfSlider.getValue() + " px");
        });
    }

    private void wireObjectControls() {
        presetCombo.addActionListener(e -> {
            if (updatingFromCode) return;
            ObjectPreset p = (ObjectPreset) presetCombo.getSelectedItem();
            if (p == null) return;
            rhoObjSlider.setValue((int) p.getDensityKgM3());
            applyToSelected();
        });
        rhoObjSlider.addChangeListener(e -> { applyToSelected(); rhoObjVal.setText(rhoObjSlider.getValue() + " kg/m³"); });
        radiusSlider.addChangeListener(e -> { applyToSelected(); radiusVal.setText(String.format("%.2f m", radiusSlider.getValue() * 0.01)); });
        heightSlider.addChangeListener(e -> { applyToSelected(); heightVal.setText(String.format("%.2f m", heightSlider.getValue() * 0.01)); });
    }

    private void applyToSelected() {
        BuoyancyObject obj = sim.getSelectedObject();
        if (obj == null) return;
        obj.setDensityKgM3(rhoObjSlider.getValue());
        obj.setRadiusM(radiusSlider.getValue() * 0.01);
        obj.setHeightM(heightSlider.getValue() * 0.01);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    /** Called every tick by SimulationPanel to update live metrics. */
    public void refresh() {
        BuoyancyObject obj = sim.getSelectedObject();
        updateObjectPanel();

        if (obj == null) {
            clearMetrics();
            return;
        }

        Fluid fluid = sim.getFluid();
        double g    = sim.getGravity();

        double fb   = BuoyancyPhysics.buoyantForce(obj, fluid, g);
        double fg   = BuoyancyPhysics.gravitationalForce(obj, g);
        double fn   = fg - fb;
        double frac = BuoyancyPhysics.submergedFraction(obj, fluid);
        String st   = BuoyancyPhysics.statusString(obj, fluid, g);

        fbLabel.setText(String.format("%.1f N", fb));
        fgLabel.setText(String.format("%.1f N", fg));

        if (Math.abs(fn) < 0.1) {
            fnLabel.setText("≈ 0 N");
            fnLabel.setForeground(new Color(100, 230, 100));
        } else if (fn > 0) {
            fnLabel.setText(String.format("%.1f N ↓", fn));
            fnLabel.setForeground(new Color(230, 90, 90));
        } else {
            fnLabel.setText(String.format("%.1f N ↑", -fn));
            fnLabel.setForeground(new Color(90, 200, 100));
        }

        double vy = obj.getVy();
        velLabel.setText(vy > 0.01 ? String.format("%.2f m/s ↓", vy)
                        : vy < -0.01 ? String.format("%.2f m/s ↑", -vy)
                        : "≈ 0");
        subLabel.setText(String.format("%.1f%%", frac * 100));
        massLabel.setText(String.format("%.3f kg", obj.getMass()));
        volLabel.setText(String.format("%.4f m³", obj.getVolume()));
        densRatioLbl.setText(String.format("%.3f", obj.getDensityKgM3() / fluid.getDensityKgM3()));

        statusLabel.setText(st);
        statusLabel.setForeground(statusColor(st));
    }

    private void updateObjectPanel() {
        BuoyancyObject obj = sim.getSelectedObject();
        boolean hasObj = obj != null;
        objTitle.setText(hasObj ? "Object: " + obj.getName() : "No object selected");
        presetCombo.setEnabled(hasObj);
        rhoObjSlider.setEnabled(hasObj);
        radiusSlider.setEnabled(hasObj);
        heightSlider.setEnabled(hasObj);
        deleteBtn.setEnabled(hasObj);

        if (hasObj) {
            updatingFromCode = true;
            rhoObjSlider.setValue((int) obj.getDensityKgM3());
            radiusSlider.setValue((int) Math.round(obj.getRadiusM() * 100));
            heightSlider.setValue((int) Math.round(obj.getHeightM() * 100));
            rhoObjVal.setText(String.format("%.0f kg/m³", obj.getDensityKgM3()));
            radiusVal.setText(String.format("%.2f m", obj.getRadiusM()));
            heightVal.setText(String.format("%.2f m", obj.getHeightM()));
            updatingFromCode = false;
        }
    }

    private void clearMetrics() {
        for (JLabel l : new JLabel[]{fbLabel, fgLabel, fnLabel, velLabel,
                                     subLabel, massLabel, volLabel, densRatioLbl}) {
            l.setText("—");
            l.setForeground(Color.LIGHT_GRAY);
        }
        statusLabel.setText("—");
        statusLabel.setForeground(Color.LIGHT_GRAY);
    }

    private Color statusColor(String st) {
        return switch (st) {
            case "FLOATING"          -> new Color(80, 220, 255);
            case "SINKING"           -> new Color(255, 100, 80);
            case "RISING"            -> new Color(80, 220, 100);
            case "RESTING ON FLOOR"  -> new Color(180, 160, 100);
            default                  -> new Color(220, 220, 100);
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JPanel section(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(38, 44, 62));
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 90, 130), 1),
            title, TitledBorder.LEFT, TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 11),
            new Color(160, 190, 230)));
        return p;
    }

    private JPanel sliderRow(String title, JSlider slider, JLabel valLbl) {
        JPanel row = new JPanel(new BorderLayout(0, 1));
        row.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setForeground(Color.LIGHT_GRAY);
        lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        valLbl.setForeground(new Color(200, 220, 255));
        valLbl.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
        top.add(lbl, BorderLayout.WEST);
        top.add(valLbl, BorderLayout.EAST);
        slider.setBackground(new Color(38, 44, 62));
        slider.setForeground(new Color(120, 160, 220));
        slider.setPaintTicks(false);
        row.add(top, BorderLayout.NORTH);
        row.add(slider, BorderLayout.CENTER);
        return row;
    }

    private JButton styledBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        b.setFocusPainted(false);
        return b;
    }

    private JButton small(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        b.setBackground(new Color(50, 60, 85));
        b.setForeground(new Color(200, 220, 255));
        b.setFocusPainted(false);
        b.addActionListener(e -> action.run());
        return b;
    }

    private static JLabel metricLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.LIGHT_GRAY);
        return l;
    }
}
