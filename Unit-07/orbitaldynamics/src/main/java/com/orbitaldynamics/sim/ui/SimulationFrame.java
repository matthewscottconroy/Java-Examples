package com.orbitaldynamics.sim.ui;

import com.orbitaldynamics.sim.body.OrbitalBody;
import com.orbitaldynamics.sim.physics.PhysicsEngine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Main application window for the N-body orbital simulator.
 *
 * <pre>
 *  ┌─────────────────────────────────┬──────────┐
 *  │         SimulationPanel         │ Sidebar  │
 *  │         (space + bodies)        │ (info)   │
 *  ├─────────────────────────────────┴──────────┤
 *  │    Toolbar (controls)                       │
 *  └─────────────────────────────────────────────┘
 * </pre>
 *
 * <h2>Toolbar reference</h2>
 * <ul>
 *   <li><b>Place Body</b> — toggle; click+drag on canvas to create a body</li>
 *   <li><b>Radius</b> — size of new bodies (simulation units = pixels at zoom 1×)</li>
 *   <li><b>Density</b> — mass-per-unit-area of new bodies; mass = density × π × r².
 *       Higher density → heavier body for the same visual size.</li>
 *   <li><b>Speed</b> — simulation speed multiplier</li>
 *   <li><b>G</b> — gravitational constant (replaces the real 6.674×10⁻¹¹ with a
 *       simulation-scale value). Default 5000.  All bodies attract each other with
 *       force F = G·m₁·m₂/r².  Increase G to strengthen gravity.</li>
 *   <li><b>Elasticity</b> — collision coefficient of restitution.
 *       1.0 = perfectly elastic (energy conserved), 0.0 = perfectly inelastic
 *       (bodies stick at the same point but don't merge into one).</li>
 *   <li><b>Merge</b> — when active, colliding bodies absorb each other: momentum,
 *       area, and spin are all conserved.  Overrides the Elasticity setting.</li>
 * </ul>
 */
public final class SimulationFrame extends JFrame {

    private static final Color BG_DARK = new Color(18, 20, 32);
    private static final Color BG_TOOL = new Color(25, 28, 42);

    private final PhysicsEngine   engine;
    private final SimulationPanel simPanel;
    private final Sidebar         sidebar;

    public SimulationFrame() {
        super("Orbital Dynamics — N-Body Simulator");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(BG_DARK);

        engine   = new PhysicsEngine();
        simPanel = new SimulationPanel(engine);
        sidebar  = new Sidebar();
        sidebar.setEngine(engine);
        sidebar.wireListSelection(() -> simPanel.repaint());
        sidebar.setOnDeleteBody(body -> {
            simPanel.removeBody(body);
            simPanel.repaint();
        });

        simPanel.setSidebarRefreshCallback(() ->
            SwingUtilities.invokeLater(() -> sidebar.update(simPanel.getBodies())));

        JPanel center = new JPanel(new BorderLayout());
        center.add(simPanel, BorderLayout.CENTER);
        center.add(sidebar,  BorderLayout.EAST);

        add(center,         BorderLayout.CENTER);
        add(buildToolbar(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(950, 620));
    }

    // -------------------------------------------------------------------------
    // Toolbar
    // -------------------------------------------------------------------------

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.setBackground(BG_TOOL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 50, 75)),
            new EmptyBorder(2, 4, 2, 4)));

        // ── Place Body ───────────────────────────────────────────────────────
        JToggleButton placeBtn = toolToggle("☄  Place Body");
        placeBtn.setToolTipText("Toggle place-body mode: click+drag on the canvas to create a body");
        placeBtn.addActionListener(e -> simPanel.setPlaceMode(placeBtn.isSelected()));
        bar.add(placeBtn);

        bar.add(toolSep());

        // ── Radius ───────────────────────────────────────────────────────────
        JLabel rLabel = toolLabel("Radius");
        rLabel.setToolTipText(
            "<html>Radius of new bodies (simulation units = screen pixels at 1× zoom).<br>" +
            "Mass = density × π × radius²</html>");
        bar.add(rLabel);
        JSpinner radiusSpinner = new JSpinner(new SpinnerNumberModel(20, 5, 120, 5));
        styleSpinner(radiusSpinner);
        radiusSpinner.setToolTipText(rLabel.getToolTipText());
        radiusSpinner.addChangeListener(e ->
            simPanel.setNewBodyRadius(((Number) radiusSpinner.getValue()).doubleValue()));
        bar.add(radiusSpinner);

        // ── Density ──────────────────────────────────────────────────────────
        JLabel dLabel = toolLabel("Density");
        dLabel.setToolTipText(
            "<html><b>Mass density</b> of new bodies (arbitrary units).<br>" +
            "Mass = density × π × radius²<br>" +
            "A larger density makes a body heavier for the same visual size.<br>" +
            "Not the same as surface gravity g = G·M/r².</html>");
        bar.add(dLabel);
        JSpinner densSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 20.0, 0.5));
        styleSpinner(densSpinner);
        densSpinner.setToolTipText(dLabel.getToolTipText());
        densSpinner.addChangeListener(e ->
            simPanel.setNewBodyDensity(((Number) densSpinner.getValue()).doubleValue()));
        bar.add(densSpinner);

        bar.add(toolSep());

        // ── Speed ────────────────────────────────────────────────────────────
        bar.add(toolLabel("Speed"));
        String[] speeds = {"0.1×", "0.25×", "0.5×", "1×", "2×", "5×", "10×"};
        double[] mults  = {0.1, 0.25, 0.5, 1.0, 2.0, 5.0, 10.0};
        JComboBox<String> speedBox = new JComboBox<>(speeds);
        speedBox.setSelectedIndex(3);
        styleCombo(speedBox);
        speedBox.addActionListener(e ->
            simPanel.setSpeedMultiplier(mults[speedBox.getSelectedIndex()]));
        bar.add(speedBox);

        // ── G constant ───────────────────────────────────────────────────────
        JLabel gLabel = toolLabel("G");
        gLabel.setToolTipText(
            "<html><b>Gravitational constant</b> (simulation units, default 5000).<br>" +
            "All bodies attract with F = G · m₁ · m₂ / r².<br>" +
            "This is <i>not</i> surface gravity (g = G·M/r²) — it is the universal<br>" +
            "constant that scales all gravitational interactions equally.<br>" +
            "Increase G to make gravity stronger; decrease to weaken it.</html>");
        bar.add(gLabel);
        JSpinner gSpinner = new JSpinner(new SpinnerNumberModel(
            (int) PhysicsEngine.DEFAULT_G, 100, 500_000, 500));
        styleSpinner(gSpinner);
        gSpinner.setToolTipText(gLabel.getToolTipText());
        gSpinner.addChangeListener(e ->
            engine.setG(((Number) gSpinner.getValue()).doubleValue()));
        bar.add(gSpinner);

        bar.add(toolSep());

        // ── Elasticity ───────────────────────────────────────────────────────
        JLabel eLabel = toolLabel("Elasticity");
        eLabel.setToolTipText(
            "<html><b>Collision coefficient of restitution</b> [0 – 1].<br>" +
            "1.0 = perfectly elastic: kinetic energy is conserved.<br>" +
            "0.0 = perfectly inelastic: bodies stop relative motion on contact.<br>" +
            "Ignored when Merge mode is active.</html>");
        bar.add(eLabel);
        JSpinner elasticitySpinner = new JSpinner(
            new SpinnerNumberModel(1.0, 0.0, 1.0, 0.1));
        styleSpinner(elasticitySpinner);
        elasticitySpinner.setToolTipText(eLabel.getToolTipText());
        elasticitySpinner.addChangeListener(e -> {
            double val = ((Number) elasticitySpinner.getValue()).doubleValue();
            engine.setRestitution(val);
            // Ensure collision detection is on when elasticity is set
            if (!engine.isMergeOnCollision()) engine.setElasticCollisions(val > 0 || true);
        });
        bar.add(elasticitySpinner);

        // ── Merge toggle ─────────────────────────────────────────────────────
        JToggleButton mergeBtn = toolToggle("⊕ Merge");
        mergeBtn.setToolTipText(
            "<html><b>Merge on collision</b> — when active, bodies absorb each other<br>" +
            "instead of bouncing. Momentum, area (r² conserved), and spin angular<br>" +
            "momentum are all conserved. The absorbed body is removed.</html>");
        mergeBtn.addActionListener(e -> {
            boolean on = mergeBtn.isSelected();
            engine.setMergeOnCollision(on);
            // Merge mode drives its own collision handling; elastic toggle is secondary
            engine.setElasticCollisions(!on);
        });
        bar.add(mergeBtn);

        bar.add(toolSep());

        // ── Pause / Resume ───────────────────────────────────────────────────
        JToggleButton pauseBtn = toolToggle("⏸  Pause");
        pauseBtn.addActionListener(e -> simPanel.setRunning(!pauseBtn.isSelected()));
        bar.add(pauseBtn);

        JCheckBox trailsCb = toolCheckbox("Trails", true);
        trailsCb.addActionListener(e -> simPanel.setShowTrails(trailsCb.isSelected()));
        bar.add(trailsCb);

        JCheckBox arrowsCb = toolCheckbox("Vel arrows", true);
        arrowsCb.addActionListener(e -> simPanel.setShowVelocityArrows(arrowsCb.isSelected()));
        bar.add(arrowsCb);

        bar.add(toolSep());

        // ── Presets ──────────────────────────────────────────────────────────
        bar.add(toolLabel("Preset:"));
        String[] presets = {"Binary Stars", "Star + Planets", "Figure Eight"};
        JComboBox<String> presetBox = new JComboBox<>(presets);
        styleCombo(presetBox);
        JButton loadPreset = toolButton("Load");
        loadPreset.addActionListener(e -> {
            double G = engine.getG();
            var preset = switch (presetBox.getSelectedIndex()) {
                case 0 -> PhysicsEngine.presetBinaryStars(G);
                case 1 -> PhysicsEngine.presetStarAndPlanets(G);
                case 2 -> PhysicsEngine.presetFigureEight(G);
                default -> List.<OrbitalBody>of();
            };
            simPanel.loadPreset(preset);
        });
        bar.add(presetBox);
        bar.add(loadPreset);

        bar.add(toolSep());

        // ── Clear ────────────────────────────────────────────────────────────
        JButton clearBtn = toolButton("🗑  Clear");
        clearBtn.setForeground(new Color(255, 100, 100));
        clearBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this,
                "Remove all bodies?", "Clear", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                simPanel.clearBodies();
        });
        bar.add(clearBtn);

        return bar;
    }

    // -------------------------------------------------------------------------
    // Widget helpers
    // -------------------------------------------------------------------------

    private static JToggleButton toolToggle(String text) {
        JToggleButton b = new JToggleButton(text);
        b.setFocusPainted(false);
        b.setBackground(new Color(35, 40, 60));
        b.setForeground(new Color(180, 200, 240));
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 100), 1),
            new EmptyBorder(3, 8, 3, 8)));
        return b;
    }

    private static JButton toolButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(new Color(35, 40, 60));
        b.setForeground(new Color(180, 200, 240));
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 100), 1),
            new EmptyBorder(3, 8, 3, 8)));
        return b;
    }

    private static JCheckBox toolCheckbox(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setFocusPainted(false);
        cb.setBackground(new Color(25, 28, 42));
        cb.setForeground(new Color(160, 180, 220));
        cb.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        return cb;
    }

    private static JLabel toolLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(130, 150, 190));
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        return l;
    }

    private static JSeparator toolSep() {
        JSeparator s = new JSeparator(JSeparator.VERTICAL);
        s.setPreferredSize(new Dimension(1, 22));
        s.setForeground(new Color(50, 60, 90));
        return s;
    }

    private static void styleSpinner(JSpinner sp) {
        sp.setPreferredSize(new Dimension(70, 24));
        sp.setBackground(new Color(30, 35, 55));
        if (sp.getEditor() instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(new Color(30, 35, 55));
            de.getTextField().setForeground(new Color(190, 210, 240));
            de.getTextField().setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        }
    }

    private static void styleCombo(JComboBox<?> cb) {
        cb.setBackground(new Color(30, 35, 55));
        cb.setForeground(new Color(190, 210, 240));
        cb.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
    }
}
