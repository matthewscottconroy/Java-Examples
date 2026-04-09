package com.buoyancy.pressure.ui;

import com.buoyancy.pressure.model.FluidMedium;
import com.buoyancy.pressure.model.PressureBody;
import com.buoyancy.pressure.physics.PressurePhysics;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Control panel for the pressure-differential buoyancy simulator.
 *
 * <p>Sections:
 * <ol>
 *   <li>Object management buttons (Add, Delete, Physics toggle)</li>
 *   <li>Environment settings (gravity, fluid density, surface, grid size)</li>
 *   <li>Selected object settings</li>
 *   <li>Pressure force breakdown (the key educational panel showing
 *       face pressures, individual face forces, net buoyancy, and
 *       Archimedes confirmation)</li>
 * </ol>
 */
public class ControlPanel extends JPanel {

    private final PressurePanel panel;

    // ── Environment sliders ───────────────────────────────────────────────────
    private final JSlider gSlider    = new JSlider(10, 250, 98);
    private final JLabel  gVal       = new JLabel("9.8 m/s²");
    private final JSlider rhofSlider = new JSlider(100, 14000, 1000);
    private final JLabel  rhofVal    = new JLabel("1000 kg/m³");
    private final JSlider surfSlider = new JSlider(60, 580, PressurePanel.DEFAULT_SURF_Y);
    private final JLabel  surfVal    = new JLabel(PressurePanel.DEFAULT_SURF_Y + " px");
    private final JSlider cellSlider = new JSlider(8, 60, 20);
    private final JLabel  cellVal    = new JLabel("20 px");

    // ── Object sliders ────────────────────────────────────────────────────────
    private final JSlider rhoObjSlider = new JSlider(50, 14000, 530);
    private final JLabel  rhoObjVal   = new JLabel("530 kg/m³");
    private final JSlider radiusSlider = new JSlider(5, 55, 22);
    private final JLabel  radiusVal   = new JLabel("0.22 m");
    private final JSlider heightSlider = new JSlider(5, 90, 40);
    private final JLabel  heightVal   = new JLabel("0.40 m");
    private final JButton deleteBtn   = new JButton("Delete Object");
    private final JLabel  objTitle    = new JLabel("No selection");

    // ── Pressure breakdown labels ─────────────────────────────────────────────
    private final JLabel depthBotLbl   = monoLabel("—");
    private final JLabel pBotLbl       = monoLabel("—");
    private final JLabel areaLbl       = monoLabel("—");
    private final JLabel fBotUpLbl     = monoLabel("—");
    private final JLabel depthTopLbl   = monoLabel("—");
    private final JLabel pTopLbl       = monoLabel("—");
    private final JLabel fTopDownLbl   = monoLabel("—");
    private final JLabel netPresLbl    = monoLabel("—");
    private final JLabel archLbl       = monoLabel("—");
    private final JLabel matchLbl      = monoLabel("—");
    private final JLabel fgravLbl      = monoLabel("—");
    private final JLabel netDownLbl    = monoLabel("—");
    private final JLabel statusLbl     = monoLabel("—");
    private final JLabel subFracLbl    = monoLabel("—");

    private boolean updatingFromCode = false;

    // ── Construction ──────────────────────────────────────────────────────────

    public ControlPanel(PressurePanel panel) {
        this.panel = panel;
        setPreferredSize(new Dimension(320, PressurePanel.CANVAS_H));
        setBackground(new Color(28, 32, 48));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildButtonRow());
        add(Box.createVerticalStrut(6));
        add(buildEnvironmentPanel());
        add(Box.createVerticalStrut(6));
        add(buildObjectPanel());
        add(Box.createVerticalStrut(6));
        add(buildBreakdownPanel());
        add(Box.createVerticalGlue());

        wireSliders();
    }

    // ── Buttons ───────────────────────────────────────────────────────────────

    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JButton addBtn     = styledBtn("+ Add",      new Color(60, 130, 60));
        JButton physBtn    = styledBtn("▶ Physics",  new Color(60, 80, 140));
        JButton gridBtn    = styledBtn("⊞ Grid",     new Color(70, 70, 100));
        JButton arrowsBtn  = styledBtn("↕ Arrows",   new Color(80, 70, 100));

        addBtn.addActionListener(e -> { panel.addBody(); refresh(); });
        physBtn.addActionListener(e -> {
            panel.physicsOn = !panel.physicsOn;
            if (!panel.physicsOn) panel.resetPhysics();
            physBtn.setText(panel.physicsOn ? "⏸ Pause" : "▶ Physics");
        });
        gridBtn.addActionListener(e -> { panel.showGrid = !panel.showGrid; });
        arrowsBtn.addActionListener(e -> { panel.showFaceArrows = !panel.showFaceArrows; });

        p.add(addBtn); p.add(physBtn); p.add(gridBtn); p.add(arrowsBtn);
        return p;
    }

    // ── Environment panel ─────────────────────────────────────────────────────

    private JPanel buildEnvironmentPanel() {
        JPanel p = section("Fluid Environment");
        p.add(sliderRow("Gravity (g)", gSlider, gVal));
        p.add(Box.createVerticalStrut(4));
        p.add(sliderRow("Fluid density (ρ_f)  kg/m³", rhofSlider, rhofVal));
        p.add(Box.createVerticalStrut(4));
        p.add(sliderRow("Surface level (y px)", surfSlider, surfVal));
        p.add(Box.createVerticalStrut(4));
        p.add(sliderRow("Arrow cell size (px)", cellSlider, cellVal));

        JPanel presets = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        presets.setOpaque(false);
        presets.add(small("Fresh Water",  () -> rhofSlider.setValue(1000)));
        presets.add(small("Sea Water",    () -> rhofSlider.setValue(1025)));
        presets.add(small("Oil",          () -> rhofSlider.setValue(850)));
        presets.add(small("Mercury",      () -> rhofSlider.setValue(13534)));
        presets.add(small("Moon g=1.6",   () -> gSlider.setValue(16)));
        presets.add(small("Jupiter g=25", () -> gSlider.setValue(250)));
        p.add(presets);
        return p;
    }

    // ── Object panel ─────────────────────────────────────────────────────────

    private JPanel buildObjectPanel() {
        JPanel p = section("Selected Object");
        objTitle.setForeground(new Color(180, 200, 255));
        objTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        p.add(objTitle);
        p.add(Box.createVerticalStrut(4));
        p.add(sliderRow("Density (ρ_o)  kg/m³", rhoObjSlider, rhoObjVal));
        p.add(Box.createVerticalStrut(4));
        p.add(sliderRow("Radius (r)  m", radiusSlider, radiusVal));
        p.add(Box.createVerticalStrut(4));
        p.add(sliderRow("Height (h)  m", heightSlider, heightVal));
        p.add(Box.createVerticalStrut(4));
        // Material presets
        JPanel mp = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        mp.setOpaque(false);
        mp.add(small("Cork 240",       () -> rhoObjSlider.setValue(240)));
        mp.add(small("Pine 530",       () -> rhoObjSlider.setValue(530)));
        mp.add(small("Ice 917",        () -> rhoObjSlider.setValue(917)));
        mp.add(small("Concrete 2400",  () -> rhoObjSlider.setValue(2400)));
        mp.add(small("Steel 7850",     () -> rhoObjSlider.setValue(7850)));
        p.add(mp);
        p.add(Box.createVerticalStrut(4));
        deleteBtn.setBackground(new Color(140, 50, 50));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.addActionListener(e -> { panel.removeSelected(); refresh(); });
        p.add(deleteBtn);
        return p;
    }

    // ── Pressure breakdown panel ──────────────────────────────────────────────

    private JPanel buildBreakdownPanel() {
        JPanel p = section("Pressure Force Breakdown");
        p.setLayout(new GridLayout(0, 2, 4, 2));

        addRow(p, "Bottom face depth:",  depthBotLbl);
        addRow(p, "P_bottom (Pa):",      pBotLbl);
        addRow(p, "Face area (m²):",     areaLbl);
        addRow(p, "F_up ↑ (N):",         fBotUpLbl);
        addRow(p, "Top face depth:",      depthTopLbl);
        addRow(p, "P_top (Pa):",          pTopLbl);
        addRow(p, "F_down ↓ (N):",        fTopDownLbl);
        addRow(p, "Side forces:",         monoLabel("cancel → 0"));
        addSeparator(p);
        addRow(p, "Net buoyancy (Σ):",    netPresLbl);
        addRow(p, "Archimedes (ρgV):",    archLbl);
        addRow(p, "Match:",               matchLbl);
        addSeparator(p);
        addRow(p, "F_gravity ↓ (N):",     fgravLbl);
        addRow(p, "Net force (N):",        netDownLbl);
        addRow(p, "Submerged %:",          subFracLbl);
        addRow(p, "Status:",               statusLbl);
        return p;
    }

    private void addRow(JPanel p, String name, JLabel val) {
        JLabel lbl = new JLabel(name);
        lbl.setForeground(new Color(170, 190, 220));
        lbl.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        p.add(lbl); p.add(val);
    }

    private void addSeparator(JPanel p) {
        JLabel sep1 = new JLabel("──────────────────");
        JLabel sep2 = new JLabel("──────────────────");
        sep1.setForeground(new Color(80, 90, 110));
        sep2.setForeground(new Color(80, 90, 110));
        sep1.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 9));
        sep2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 9));
        p.add(sep1); p.add(sep2);
    }

    // ── Wiring ────────────────────────────────────────────────────────────────

    private void wireSliders() {
        gSlider.addChangeListener(e -> {
            double g = gSlider.getValue() * 0.1;
            panel.setGravity(g);
            gVal.setText(String.format("%.1f m/s²", g));
        });
        rhofSlider.addChangeListener(e -> {
            panel.getFluid().setDensityKgM3(rhofSlider.getValue());
            rhofVal.setText(rhofSlider.getValue() + " kg/m³");
            panel.markHeatmapDirty();
        });
        surfSlider.addChangeListener(e -> {
            panel.getFluid().setSurfaceY(surfSlider.getValue());
            surfVal.setText(surfSlider.getValue() + " px");
            panel.markHeatmapDirty();
        });
        cellSlider.addChangeListener(e -> {
            panel.cellSizePx = cellSlider.getValue();
            cellVal.setText(cellSlider.getValue() + " px");
        });
        rhoObjSlider.addChangeListener(e -> { applyToSelected(); rhoObjVal.setText(rhoObjSlider.getValue() + " kg/m³"); });
        radiusSlider.addChangeListener(e -> { applyToSelected(); radiusVal.setText(String.format("%.2f m", radiusSlider.getValue() * 0.01)); });
        heightSlider.addChangeListener(e -> { applyToSelected(); heightVal.setText(String.format("%.2f m", heightSlider.getValue() * 0.01)); });
    }

    private void applyToSelected() {
        if (updatingFromCode) return;
        PressureBody b = panel.getSelectedBody();
        if (b == null) return;
        b.setDensityKgM3(rhoObjSlider.getValue());
        b.setRadiusM(radiusSlider.getValue() * 0.01);
        b.setHeightM(heightSlider.getValue() * 0.01);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    /** Called every tick to update the breakdown panel and sync sliders. */
    public void refresh() {
        PressureBody b = panel.getSelectedBody();
        boolean has = b != null;
        objTitle.setText(has ? "Object: " + b.getName() : "No selection");
        rhoObjSlider.setEnabled(has);
        radiusSlider.setEnabled(has);
        heightSlider.setEnabled(has);
        deleteBtn.setEnabled(has);

        if (has) {
            updatingFromCode = true;
            rhoObjSlider.setValue((int) b.getDensityKgM3());
            radiusSlider.setValue((int) Math.round(b.getRadiusM() * 100));
            heightSlider.setValue((int) Math.round(b.getHeightM() * 100));
            rhoObjVal.setText(String.format("%.0f kg/m³", b.getDensityKgM3()));
            radiusVal.setText(String.format("%.2f m", b.getRadiusM()));
            heightVal.setText(String.format("%.2f m", b.getHeightM()));
            updatingFromCode = false;
            updateBreakdown(b);
        } else {
            clearBreakdown();
        }
    }

    private void updateBreakdown(PressureBody b) {
        FluidMedium fluid = panel.getFluid();
        double g = panel.getGravity();

        double dBot  = PressurePhysics.bottomFaceDepthM(b, fluid);
        double dTop  = PressurePhysics.topFaceDepthM(b, fluid);
        double pBot  = PressurePhysics.bottomFacePressure(b, fluid, g);
        double pTop  = PressurePhysics.topFacePressure(b, fluid, g);
        double fBotUp  = PressurePhysics.forceOnBottomFace(b, fluid, g);
        double fTopDn  = PressurePhysics.forceOnTopFace(b, fluid, g);
        double netPres = PressurePhysics.netBuoyancyPressure(b, fluid, g);
        double arch    = PressurePhysics.netBuoyancyArchimedes(b, fluid, g);
        double fg      = PressurePhysics.gravitationalForce(b, g);
        double netDown = fg - arch;
        double sub     = PressurePhysics.submergedFraction(b, fluid);

        depthBotLbl.setText(String.format("%.3f m", dBot));
        pBotLbl.setText(String.format("%.1f Pa", pBot));
        areaLbl.setText(String.format("%.4f m²", b.getBottomFaceAreaM2()));
        fBotUpLbl.setText(String.format("%.2f N", fBotUp));
        fBotUpLbl.setForeground(new Color(70, 220, 90));

        depthTopLbl.setText(String.format("%.3f m", dTop));
        pTopLbl.setText(String.format("%.1f Pa", pTop));
        fTopDownLbl.setText(String.format("%.2f N", fTopDn));
        fTopDownLbl.setForeground(new Color(220, 80, 80));

        netPresLbl.setText(String.format("%.2f N ↑", netPres));
        netPresLbl.setForeground(new Color(80, 200, 220));
        archLbl.setText(String.format("%.2f N ↑", arch));
        archLbl.setForeground(new Color(80, 200, 220));

        double diff = Math.abs(netPres - arch);
        boolean match = diff < 0.01 + 0.001 * Math.max(netPres, arch);
        matchLbl.setText(match ? "✓ Agree" : String.format("Δ=%.2f N", diff));
        matchLbl.setForeground(match ? new Color(80, 220, 80) : new Color(255, 120, 60));

        fgravLbl.setText(String.format("%.2f N", fg));
        netDownLbl.setText(netDown > 0.1 ? String.format("%.2f N ↓ (sinks)", netDown)
                          : netDown < -0.1 ? String.format("%.2f N ↑ (rises)", -netDown)
                          : "≈ 0 (equilibrium)");
        Color nc = netDown > 0.1 ? new Color(220, 80, 80) : netDown < -0.1 ? new Color(80, 220, 100) : new Color(220, 220, 80);
        netDownLbl.setForeground(nc);

        subFracLbl.setText(String.format("%.1f%%", sub * 100));

        String status = netDown > 0.5 ? "SINKING" : netDown < -0.5 ? "RISING" : "EQUILIBRIUM";
        statusLbl.setText(status);
        statusLbl.setForeground(nc);
    }

    private void clearBreakdown() {
        for (JLabel l : new JLabel[]{depthBotLbl, pBotLbl, areaLbl, fBotUpLbl,
                depthTopLbl, pTopLbl, fTopDownLbl, netPresLbl, archLbl,
                matchLbl, fgravLbl, netDownLbl, subFracLbl, statusLbl}) {
            l.setText("—"); l.setForeground(Color.LIGHT_GRAY);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JPanel section(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(38, 44, 62));
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 90, 130), 1),
            title, TitledBorder.LEFT, TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 11), new Color(160, 190, 230)));
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
        top.add(lbl, BorderLayout.WEST); top.add(valLbl, BorderLayout.EAST);
        slider.setBackground(new Color(38, 44, 62));
        row.add(top, BorderLayout.NORTH); row.add(slider, BorderLayout.CENTER);
        return row;
    }

    private JButton styledBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        b.setFocusPainted(false);
        return b;
    }

    private JButton small(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        b.setBackground(new Color(50, 60, 85)); b.setForeground(new Color(200, 220, 255));
        b.setFocusPainted(false);
        b.addActionListener(e -> action.run());
        return b;
    }

    private static JLabel monoLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.LIGHT_GRAY);
        l.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
        return l;
    }
}
