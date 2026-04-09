package com.combustionengine.ui;

import com.combustionengine.model.*;
import com.combustionengine.physics.EnginePhysics;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Right-side control panel for the combustion engine simulator.
 *
 * <p>Divided into four sections:
 * <ol>
 *   <li>Engine control buttons (Start, Pause, Reset)</li>
 *   <li>Engine preset and throttle</li>
 *   <li>Live engine metrics (RPM, power, torque, efficiency)</li>
 *   <li>Per-cylinder status display</li>
 * </ol>
 */
public final class ControlPanel extends JPanel {

    private final SimulationPanel sim;

    // ── Controls ──────────────────────────────────────────────────────────────
    private final JComboBox<EnginePreset> presetCombo   = new JComboBox<>(EnginePreset.values());
    private final JSlider                throttleSlider  = new JSlider(0, 100, 0);
    private final JLabel                 throttleVal     = new JLabel("0%");
    // Speed: 5–200, stored as ×10 of the actual multiplier (so 25 = 0.25×, 100 = 1.0×)
    private final JSlider                speedSlider     = new JSlider(5, 200, 25);
    private final JLabel                 speedVal        = new JLabel("0.25×");
    private       JButton                pauseBtn;

    // ── Live metrics ──────────────────────────────────────────────────────────
    private final JLabel rpmLabel    = metricLabel("0 RPM");
    private final JLabel powerLabel  = metricLabel("0.0 kW");
    private final JLabel torqueLabel = metricLabel("0 N·m");
    private final JLabel etaLabel    = metricLabel("0.0%");
    private final JLabel displLabel  = metricLabel("—");

    // ── Cylinder status labels (up to 4) ──────────────────────────────────────
    private final JLabel[] cylPhase = {
        metricLabel("—"), metricLabel("—"), metricLabel("—"), metricLabel("—")
    };
    private final JLabel[] cylPres = {
        metricLabel("—"), metricLabel("—"), metricLabel("—"), metricLabel("—")
    };
    private final JLabel[] cylTemp = {
        metricLabel("—"), metricLabel("—"), metricLabel("—"), metricLabel("—")
    };

    // ── Construction ──────────────────────────────────────────────────────────

    public ControlPanel(SimulationPanel sim) {
        this.sim = sim;
        setPreferredSize(new Dimension(300, SimulationPanel.CANVAS_H));
        setBackground(new Color(28, 32, 48));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildButtonPanel());
        add(Box.createVerticalStrut(6));
        add(buildEnginePanel());
        add(Box.createVerticalStrut(6));
        add(buildMetricsPanel());
        add(Box.createVerticalStrut(6));
        add(buildCylinderPanel());
        add(Box.createVerticalGlue());

        wireControls();
        refresh();
    }

    // ── Button panel ──────────────────────────────────────────────────────────

    private JPanel buildButtonPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);

        JButton startBtn = styledBtn("▶ Start", new Color(50, 130, 60));
        pauseBtn          = styledBtn("⏸ Pause", new Color(70, 80, 140));
        JButton resetBtn  = styledBtn("↺ Reset", new Color(110, 70, 30));

        startBtn.addActionListener(e -> sim.startEngine());
        pauseBtn.addActionListener(e -> {
            sim.togglePause();
            pauseBtn.setText(sim.isPaused() ? "▶ Resume" : "⏸ Pause");
        });
        resetBtn.addActionListener(e -> {
            sim.reset((EnginePreset) presetCombo.getSelectedItem());
            throttleSlider.setValue(0);
        });

        p.add(startBtn); p.add(pauseBtn); p.add(resetBtn);
        return p;
    }

    // ── Engine preset + throttle ──────────────────────────────────────────────

    private JPanel buildEnginePanel() {
        JPanel p = section("Engine Setup");

        // Preset combo
        JPanel presetRow = new JPanel(new BorderLayout(6, 0));
        presetRow.setOpaque(false);
        JLabel pLbl = new JLabel("Preset:");
        pLbl.setForeground(Color.LIGHT_GRAY);
        pLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        presetCombo.setBackground(new Color(45, 52, 70));
        presetCombo.setForeground(Color.WHITE);
        presetCombo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        presetRow.add(pLbl,        BorderLayout.WEST);
        presetRow.add(presetCombo, BorderLayout.CENTER);
        p.add(presetRow);
        p.add(Box.createVerticalStrut(6));

        // Throttle slider
        p.add(sliderRow("Throttle", throttleSlider, throttleVal));
        throttleSlider.setMajorTickSpacing(25);
        throttleSlider.setPaintTicks(true);
        throttleSlider.setPaintLabels(false);
        p.add(Box.createVerticalStrut(6));

        // Sim speed slider
        p.add(sliderRow("Sim speed", speedSlider, speedVal));
        speedSlider.setMajorTickSpacing(25);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(false);

        return p;
    }

    // ── Engine metrics ────────────────────────────────────────────────────────

    private JPanel buildMetricsPanel() {
        JPanel p = section("Engine Metrics");
        p.setLayout(new GridLayout(0, 2, 4, 4));

        addRow(p, "RPM:",         rpmLabel);
        addRow(p, "Power:",       powerLabel);
        addRow(p, "Torque:",      torqueLabel);
        addRow(p, "η_thermal:",   etaLabel);
        addRow(p, "Displacement:", displLabel);
        return p;
    }

    // ── Per-cylinder status ───────────────────────────────────────────────────

    private JPanel buildCylinderPanel() {
        JPanel p = section("Cylinder Status");
        p.setLayout(new GridLayout(0, 4, 4, 3));

        // Header
        for (int i = 0; i < 4; i++) {
            JLabel h = new JLabel("Cyl " + (i + 1), SwingConstants.CENTER);
            h.setForeground(new Color(140, 155, 185));
            h.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            p.add(h);
        }

        // Phase row
        for (JLabel l : cylPhase) p.add(l);

        // Pressure row
        JLabel pHdr = new JLabel("P (kPa):", SwingConstants.CENTER);
        pHdr.setForeground(new Color(130, 145, 170));
        pHdr.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        // Fill 4 columns for pressure labels
        for (JLabel l : cylPres) p.add(l);

        // Temperature row
        for (JLabel l : cylTemp) p.add(l);

        return p;
    }

    // ── Wiring ────────────────────────────────────────────────────────────────

    private void wireControls() {
        throttleSlider.addChangeListener(e -> {
            double t = throttleSlider.getValue() / 100.0;
            sim.setThrottle(t);
            throttleVal.setText(throttleSlider.getValue() + "%");
        });

        speedSlider.addChangeListener(e -> {
            double s = speedSlider.getValue() / 100.0;
            sim.setSimSpeed(s);
            speedVal.setText(String.format("%.2f×", s));
        });

        presetCombo.addActionListener(e -> {
            EnginePreset preset = (EnginePreset) presetCombo.getSelectedItem();
            if (preset != null) {
                sim.reset(preset);
                throttleSlider.setValue(0);
            }
        });
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    /** Called each physics tick to update displayed metrics. */
    public void refresh() {
        EngineState state = sim.getState();
        EngineConfig cfg  = state.config;

        rpmLabel.setText(String.format("%.0f RPM", state.rpm()));
        rpmLabel.setForeground(state.omegaRadS > 1 ? new Color(80, 210, 255) : Color.LIGHT_GRAY);

        powerLabel.setText(String.format("%.2f kW", state.powerW / 1000.0));
        torqueLabel.setText(String.format("%.1f N·m", state.combustionTorqueNm));
        etaLabel.setText(String.format("%.1f%%", state.thermalEfficiency * 100));
        displLabel.setText(String.format("%.0f cc  (×%d)", cfg.displacementM3() * 1e6, cfg.numCylinders()));

        // Per-cylinder update
        int n = state.cylinders.size();
        for (int i = 0; i < 4; i++) {
            if (i < n) {
                CylinderState cyl = state.cylinders.get(i);
                CyclePhase ph = cyl.currentPhase();

                cylPhase[i].setText(ph.name().substring(0, 3));
                cylPhase[i].setForeground(phaseColor(ph));

                cylPres[i].setText(String.format("%.0f", cyl.pressurePa / 1000.0));
                cylPres[i].setForeground(new Color(180, 190, 210));

                cylTemp[i].setText(String.format("%.0fK", cyl.temperatureK));
                cylTemp[i].setForeground(tempColor(cyl.temperatureK));
            } else {
                cylPhase[i].setText("—");
                cylPres[i].setText("—");
                cylTemp[i].setText("—");
                cylPhase[i].setForeground(Color.DARK_GRAY);
                cylPres[i].setForeground(Color.DARK_GRAY);
                cylTemp[i].setForeground(Color.DARK_GRAY);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Color phaseColor(CyclePhase ph) {
        return switch (ph) {
            case INTAKE      -> new Color(70, 160, 255);
            case COMPRESSION -> new Color(240, 200, 40);
            case POWER       -> new Color(255, 100, 40);
            case EXHAUST     -> new Color(150, 150, 155);
        };
    }

    private Color tempColor(double T) {
        if (T > 1200) return new Color(255, 100, 50);
        if (T > 700)  return new Color(255, 180, 60);
        return new Color(120, 190, 255);
    }

    private JPanel section(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(36, 42, 60));
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(65, 85, 130), 1),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                new Font(Font.SANS_SERIF, Font.BOLD, 11),
                new Color(155, 185, 230)));
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
        slider.setBackground(new Color(36, 42, 60));
        slider.setForeground(new Color(100, 155, 220));
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

    private void addRow(JPanel p, String name, JLabel val) {
        JLabel lbl = new JLabel(name);
        lbl.setForeground(new Color(165, 185, 215));
        lbl.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        val.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        p.add(lbl);
        p.add(val);
    }

    private static JLabel metricLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setForeground(Color.LIGHT_GRAY);
        l.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        return l;
    }
}
