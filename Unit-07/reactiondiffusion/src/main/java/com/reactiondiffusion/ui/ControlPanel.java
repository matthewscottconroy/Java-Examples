package com.reactiondiffusion.ui;

import com.reactiondiffusion.model.Preset;
import com.reactiondiffusion.model.ReactionDiffusionGrid;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Parameter control strip for the reaction-diffusion simulator.
 *
 * <p>Provides:
 * <ul>
 *   <li>A preset {@link JComboBox} that loads named (F, k) pairs.</li>
 *   <li>Sliders for Da (0.05–0.40, step 0.01), Db (0.05–0.40, step 0.01),
 *       F (0.010–0.080, step 0.001), and k (0.040–0.075, step 0.001).</li>
 *   <li>A speed slider controlling substeps per frame (1–30).</li>
 *   <li>A <em>Reset</em> button that restores the uniform rest state and seeds
 *       a single blob at the grid centre.</li>
 *   <li>A <em>Seed</em> button that places an additional B blob at a random
 *       grid location without clearing the current state.</li>
 * </ul>
 */
class ControlPanel extends JPanel {

    private static final Color BG = new Color(15, 17, 30);
    private static final Color FG = new Color(200, 210, 230);

    private final ReactionDiffusionGrid grid;
    private final SimulationPanel       simPanel;
    private final Random                rng = new Random();

    // Sliders kept as fields so the preset combo can update them
    private final JSlider daSlider;
    private final JSlider dbSlider;
    private final JSlider fSlider;
    private final JSlider kSlider;
    private final JLabel  daLabel;
    private final JLabel  dbLabel;
    private final JLabel  fLabel;
    private final JLabel  kLabel;

    ControlPanel(ReactionDiffusionGrid grid, SimulationPanel simPanel) {
        this.grid     = grid;
        this.simPanel = simPanel;

        setBackground(BG);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        // ---- Preset combo ----
        JPanel presetPanel = new JPanel();
        presetPanel.setBackground(BG);
        presetPanel.add(styledLabel("Preset:"));
        JComboBox<Preset> presetCombo = new JComboBox<>(Preset.PRESETS.toArray(new Preset[0]));
        presetCombo.setBackground(new Color(40, 50, 80));
        presetCombo.setForeground(FG);
        presetCombo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        presetPanel.add(presetCombo);
        add(presetPanel);

        // ---- Da slider (×0.01, range 5–40 integer → 0.05–0.40) ----
        int daInit = (int) Math.round(grid.getDa() * 100);
        daLabel  = styledLabel("Da (×0.01): " + daInit);
        daSlider = makeSlider(5, 40, daInit);
        daSlider.addChangeListener(e -> {
            double val = daSlider.getValue() / 100.0;
            daLabel.setText(String.format("Da (×0.01): %d", daSlider.getValue()));
            grid.setDa(val);
        });
        add(labeledSlider(daLabel, daSlider));

        // ---- Db slider (×0.01, range 5–40) ----
        int dbInit = (int) Math.round(grid.getDb() * 100);
        dbLabel  = styledLabel("Db (×0.01): " + dbInit);
        dbSlider = makeSlider(5, 40, dbInit);
        dbSlider.addChangeListener(e -> {
            double val = dbSlider.getValue() / 100.0;
            dbLabel.setText(String.format("Db (×0.01): %d", dbSlider.getValue()));
            grid.setDb(val);
        });
        add(labeledSlider(dbLabel, dbSlider));

        // ---- F slider (×0.001, range 10–80 integer → 0.010–0.080) ----
        int fInit = (int) Math.round(grid.getF() * 1000);
        fLabel  = styledLabel("F (×0.001): " + fInit);
        fSlider = makeSlider(10, 80, fInit);
        fSlider.addChangeListener(e -> {
            double val = fSlider.getValue() / 1000.0;
            fLabel.setText(String.format("F (×0.001): %d", fSlider.getValue()));
            grid.setF(val);
        });
        add(labeledSlider(fLabel, fSlider));

        // ---- k slider (×0.001, range 40–75 integer → 0.040–0.075) ----
        int kInit = (int) Math.round(grid.getK() * 1000);
        kLabel  = styledLabel("k (×0.001): " + kInit);
        kSlider = makeSlider(40, 75, kInit);
        kSlider.addChangeListener(e -> {
            double val = kSlider.getValue() / 1000.0;
            kLabel.setText(String.format("k (×0.001): %d", kSlider.getValue()));
            grid.setK(val);
        });
        add(labeledSlider(kLabel, kSlider));

        // ---- Speed slider (substeps per frame, 1–30) ----
        JLabel speedLabel = styledLabel("Speed: " + simPanel.getSubsteps());
        JSlider speedSlider = makeSlider(1, 30, simPanel.getSubsteps());
        speedSlider.addChangeListener(e -> {
            int v = speedSlider.getValue();
            speedLabel.setText("Speed: " + v);
            simPanel.setSubsteps(v);
        });
        add(labeledSlider(speedLabel, speedSlider));

        // ---- Preset combo action (after sliders are built) ----
        presetCombo.addActionListener(e -> {
            Preset p = (Preset) presetCombo.getSelectedItem();
            if (p == null) return;
            applyPreset(p);
        });

        // ---- Reset button ----
        JButton resetBtn = styledButton("Reset");
        resetBtn.addActionListener(e -> {
            grid.reset();
            grid.seed(grid.cols / 2, grid.rows / 2, 5, rng);
            simPanel.resetStepCount();
        });
        add(resetBtn);

        // ---- Seed button ----
        JButton seedBtn = styledButton("Seed");
        seedBtn.addActionListener(e -> {
            int cx = rng.nextInt(grid.cols);
            int cy = rng.nextInt(grid.rows);
            grid.seed(cx, cy, 5, rng);
        });
        add(seedBtn);
    }

    // -------------------------------------------------------------------------
    // Preset application
    // -------------------------------------------------------------------------

    private void applyPreset(Preset p) {
        grid.setF(p.F());
        grid.setK(p.k());
        // Update slider positions (will fire change listeners)
        fSlider.setValue((int) Math.round(p.F() * 1000));
        kSlider.setValue((int) Math.round(p.k() * 1000));
        // Reset grid to see the new pattern from scratch
        grid.reset();
        grid.seed(grid.cols / 2, grid.rows / 2, 5, rng);
        simPanel.resetStepCount();
    }

    // -------------------------------------------------------------------------
    // Widget helpers
    // -------------------------------------------------------------------------

    private JPanel labeledSlider(JLabel label, JSlider slider) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(BG);
        p.setPreferredSize(new Dimension(150, 58));
        p.add(label,  BorderLayout.NORTH);
        p.add(slider, BorderLayout.CENTER);
        return p;
    }

    private JSlider makeSlider(int min, int max, int value) {
        JSlider s = new JSlider(min, max, value);
        s.setBackground(BG);
        s.setForeground(FG);
        return s;
    }

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(FG);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return l;
    }

    private JButton styledButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(FG);
        b.setBackground(new Color(40, 50, 80));
        b.setFocusPainted(false);
        return b;
    }
}
