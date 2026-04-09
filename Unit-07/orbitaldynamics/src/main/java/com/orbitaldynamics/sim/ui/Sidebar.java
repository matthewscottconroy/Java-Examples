package com.orbitaldynamics.sim.ui;

import com.orbitaldynamics.math.Vector2D;
import com.orbitaldynamics.sim.body.OrbitalBody;
import com.orbitaldynamics.sim.physics.PhysicsEngine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Right-hand sidebar showing system statistics and selected-body details.
 *
 * <h2>Editable Body Properties</h2>
 * <p>When a body is selected, the Properties section lets you change:
 * <ul>
 *   <li><b>Radius</b> — visual and physical size (mass scales as ρπr²)</li>
 *   <li><b>Density</b> — mass per unit area; mass = density × π × radius²</li>
 *   <li><b>Pinned</b> — if checked the body is held fixed but still attracts others</li>
 * </ul>
 * A <b>Delete</b> button removes the selected body from the simulation.
 *
 * <h2>Velocity Decomposition</h2>
 * <p>Velocity is decomposed relative to the center of mass:
 * <ul>
 *   <li><b>v_r</b> — radial component (positive = moving away from CoM)</li>
 *   <li><b>v_θ</b> — tangential component (the "orbital speed")</li>
 *   <li><b>h</b> — specific angular momentum = r × v_θ</li>
 * </ul>
 */
public final class Sidebar extends JPanel {

    // ── Colours / fonts ───────────────────────────────────────────────────────
    private static final Color BG     = new Color(15, 15, 25);
    private static final Color FG     = new Color(200, 210, 230);
    private static final Color ACCENT = new Color(80, 160, 255);
    private static final Color DIM    = new Color(100, 110, 130);
    private static final Color SEP    = new Color(40, 45, 60);
    private static final Font  MONO   = new Font(Font.MONOSPACED, Font.PLAIN, 11);
    private static final Font  HEADER = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    private static final Font  SMALL  = new Font(Font.MONOSPACED, Font.PLAIN, 10);

    // ── Components ───────────────────────────────────────────────────────────
    private final JTextArea  systemInfo = makeTextArea();
    private final JTextArea  bodyInfo   = makeTextArea();
    private final JList<String>              bodyList;
    private final DefaultListModel<String>   listModel  = new DefaultListModel<>();

    // Property-edit widgets
    private final JSpinner   radiusSpinner;
    private final JSpinner   densitySpinner;
    private final JCheckBox  pinnedBox;
    private final JButton    deleteBtn;
    private final JLabel     massLabel;

    // ── State ────────────────────────────────────────────────────────────────
    private List<OrbitalBody> bodiesRef;
    private OrbitalBody       selectedBody;
    private PhysicsEngine     engine;
    private boolean           updatingProps = false;   // prevent listener feedback
    private Consumer<OrbitalBody> onDeleteBody;        // called when Delete clicked

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public Sidebar() {
        setPreferredSize(new Dimension(230, 600));
        setBackground(BG);
        setLayout(new BorderLayout(0, 4));
        setBorder(new EmptyBorder(6, 6, 6, 6));

        // ── System info ──────────────────────────────────────────────────────
        JLabel sysLabel = makeLabel("SYSTEM", HEADER, ACCENT);
        systemInfo.setRows(5);

        // ── Body list ─────────────────────────────────────────────────────────
        JLabel listLabel = makeLabel("BODIES  (click to select)", HEADER, ACCENT);
        bodyList = new JList<>(listModel);
        bodyList.setBackground(new Color(20, 22, 35));
        bodyList.setForeground(FG);
        bodyList.setFont(MONO);
        bodyList.setSelectionBackground(new Color(50, 80, 130));
        bodyList.setSelectionForeground(Color.WHITE);
        bodyList.setFixedCellHeight(18);
        JScrollPane listScroll = new JScrollPane(bodyList);
        listScroll.setBackground(BG);
        listScroll.setBorder(BorderFactory.createLineBorder(SEP));
        listScroll.setPreferredSize(new Dimension(210, 90));

        // ── Selected body info ────────────────────────────────────────────────
        JLabel bodyLabel = makeLabel("SELECTED BODY", HEADER, ACCENT);
        bodyInfo.setRows(12);

        // ── Editable properties ───────────────────────────────────────────────
        radiusSpinner  = makeSpinner(new SpinnerNumberModel(20.0, 2.0, 300.0, 1.0));
        densitySpinner = makeSpinner(new SpinnerNumberModel(1.0,  0.05, 50.0,  0.1));
        pinnedBox      = new JCheckBox("Pinned");
        pinnedBox.setBackground(BG);
        pinnedBox.setForeground(DIM);
        pinnedBox.setFont(SMALL);

        massLabel = makeLabel("mass: —", SMALL, DIM);

        deleteBtn = new JButton("Delete");
        deleteBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        deleteBtn.setBackground(new Color(120, 35, 35));
        deleteBtn.setForeground(new Color(255, 160, 160));
        deleteBtn.setFocusPainted(false);
        deleteBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(160, 60, 60), 1),
            new EmptyBorder(2, 8, 2, 8)));

        JPanel propsPanel = buildPropsPanel();

        // ── Assemble ──────────────────────────────────────────────────────────
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(BG);
        top.add(sysLabel);
        top.add(Box.createVerticalStrut(2));
        top.add(systemInfo);
        top.add(Box.createVerticalStrut(6));
        top.add(separator());
        top.add(Box.createVerticalStrut(4));
        top.add(listLabel);
        top.add(Box.createVerticalStrut(2));
        top.add(listScroll);
        add(top, BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel(new BorderLayout(0, 2));
        bodyPanel.setBackground(BG);
        bodyPanel.add(bodyLabel,             BorderLayout.NORTH);
        bodyPanel.add(new JScrollPane(bodyInfo), BorderLayout.CENTER);
        bodyPanel.add(propsPanel,            BorderLayout.SOUTH);
        add(bodyPanel, BorderLayout.CENTER);

        JLabel hint = makeLabel(
            "Drag: move  RClick: delete  Del: delete selected  Scroll: zoom",
            SMALL, DIM);
        hint.setBorder(new EmptyBorder(4, 0, 0, 0));
        add(hint, BorderLayout.SOUTH);

        // ── Spinner listeners ─────────────────────────────────────────────────
        radiusSpinner.addChangeListener(e -> {
            if (!updatingProps && selectedBody != null) {
                selectedBody.setRadius(((Number) radiusSpinner.getValue()).doubleValue());
                updateMassLabel();
            }
        });
        densitySpinner.addChangeListener(e -> {
            if (!updatingProps && selectedBody != null) {
                selectedBody.setDensity(((Number) densitySpinner.getValue()).doubleValue());
                updateMassLabel();
            }
        });
        pinnedBox.addActionListener(e -> {
            if (!updatingProps && selectedBody != null)
                selectedBody.setPinned(pinnedBox.isSelected());
        });
        deleteBtn.addActionListener(e -> {
            if (selectedBody != null && onDeleteBody != null)
                onDeleteBody.accept(selectedBody);
        });

        setPropsEnabled(false);
    }

    // -------------------------------------------------------------------------
    // Properties sub-panel
    // -------------------------------------------------------------------------

    private JPanel buildPropsPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, SEP),
            new EmptyBorder(4, 0, 2, 0)));

        JLabel title = makeLabel("PROPERTIES", new Font(Font.SANS_SERIF, Font.BOLD, 10), ACCENT);
        p.add(title);
        p.add(Box.createVerticalStrut(4));

        // Row 1: radius + density
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row1.setBackground(BG);
        row1.add(makeLabel("Radius:", SMALL, DIM));
        row1.add(radiusSpinner);
        row1.add(Box.createHorizontalStrut(6));
        row1.add(makeLabel("Density:", SMALL, DIM));
        row1.add(densitySpinner);
        row1.setAlignmentX(LEFT_ALIGNMENT);
        p.add(row1);

        // Mass readout
        massLabel.setBorder(new EmptyBorder(1, 4, 1, 0));
        massLabel.setAlignmentX(LEFT_ALIGNMENT);
        p.add(massLabel);
        p.add(Box.createVerticalStrut(2));

        // Row 2: pinned + delete
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row2.setBackground(BG);
        row2.add(pinnedBox);
        row2.add(deleteBtn);
        row2.setAlignmentX(LEFT_ALIGNMENT);
        p.add(row2);

        return p;
    }

    // -------------------------------------------------------------------------
    // Data binding
    // -------------------------------------------------------------------------

    public void setEngine(PhysicsEngine engine)                  { this.engine = engine; }
    public void setOnDeleteBody(Consumer<OrbitalBody> callback)  { this.onDeleteBody = callback; }

    public void update(List<OrbitalBody> bodies) {
        this.bodiesRef  = bodies;
        selectedBody    = bodies.stream().filter(OrbitalBody::isSelected).findFirst().orElse(null);
        refreshBodyList(bodies);
        refreshSystemInfo(bodies);
        refreshBodyInfo(selectedBody, bodies);
        refreshProps(selectedBody);
    }

    /** Wires the body list's selection to the body selection model. */
    public void wireListSelection(Runnable onSelectionChange) {
        bodyList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || bodiesRef == null) return;
            int idx = bodyList.getSelectedIndex();
            for (int i = 0; i < bodiesRef.size(); i++)
                bodiesRef.get(i).setSelected(i == idx);
            onSelectionChange.run();
        });
    }

    // -------------------------------------------------------------------------
    // Refresh helpers
    // -------------------------------------------------------------------------

    private void refreshBodyList(List<OrbitalBody> bodies) {
        int selectedIdx = bodyList.getSelectedIndex();
        listModel.clear();
        for (OrbitalBody b : bodies)
            listModel.addElement(String.format("%-12s  r=%.0f", b.getName(), b.getRadius()));
        if (selectedBody != null) {
            int idx = bodies.indexOf(selectedBody);
            if (idx >= 0) bodyList.setSelectedIndex(idx);
        } else if (selectedIdx >= 0 && selectedIdx < listModel.size()) {
            bodyList.setSelectedIndex(selectedIdx);
        }
    }

    private void refreshSystemInfo(List<OrbitalBody> bodies) {
        if (engine == null || bodies.isEmpty()) {
            systemInfo.setText("  (no bodies)");
            return;
        }
        double   E  = engine.totalEnergy(bodies);
        double   L  = engine.totalAngularMomentum(bodies);
        Vector2D cm = engine.centerOfMass(bodies);
        double   tm = bodies.stream().mapToDouble(OrbitalBody::getMass).sum();
        systemInfo.setText(String.format(
            "  Bodies:    %d%n" +
            "  Tot. mass: %.1f%n" +
            "  Energy:    %.2e%n" +
            "  Ang. mom:  %.2e%n" +
            "  CoM:       (%.0f, %.0f)",
            bodies.size(), tm, E, L, cm.x(), cm.y()));
    }

    private void refreshBodyInfo(OrbitalBody b, List<OrbitalBody> bodies) {
        if (b == null) {
            bodyInfo.setText("  Click a body to\n  inspect it.");
            return;
        }
        Vector2D pos = b.getPosition();
        Vector2D vel = b.getVelocity();
        double   spd = vel.magnitude();
        double   dir = Math.toDegrees(Math.atan2(vel.y(), vel.x()));

        Vector2D com   = (engine != null && !bodies.isEmpty())
                         ? engine.centerOfMass(bodies) : Vector2D.ZERO;
        Vector2D r_vec = pos.sub(com);
        double   r_mag = r_vec.magnitude();
        Vector2D e_r   = r_mag > 1e-9 ? r_vec.scale(1.0 / r_mag) : new Vector2D(1, 0);
        Vector2D e_t   = e_r.perpendicular();
        double   vr    = vel.dot(e_r);
        double   vt    = vel.dot(e_t);
        double   h     = r_mag * vt;

        bodyInfo.setText(String.format(
            "  %s%n" +
            "  id:  %d%n%n" +
            "  ── Position ──%n" +
            "  x:    %8.1f%n" +
            "  y:    %8.1f%n" +
            "  r_cm: %8.1f%n%n" +
            "  ── Velocity ──%n" +
            "  vx:   %8.2f%n" +
            "  vy:   %8.2f%n" +
            "  |v|:  %8.2f%n" +
            "  dir:  %8.1f °%n%n" +
            "  ── wrt CoM ──%n" +
            "  v_r:  %8.2f%n" +
            "  v_θ:  %8.2f%n" +
            "  h:    %8.2e%n%n" +
            "  ── Rotation ──%n" +
            "  ω:    %8.3f rad/s%n" +
            "  θ:    %8.1f °%n" +
            "  KE:   %8.3e",
            b.getName(), b.getId(),
            pos.x(), pos.y(), r_mag,
            vel.x(), vel.y(), spd, dir,
            vr, vt, h,
            b.getOmega(), Math.toDegrees(b.getAngle()),
            b.kineticEnergy()));
    }

    private void refreshProps(OrbitalBody b) {
        updatingProps = true;
        try {
            boolean has = (b != null);
            setPropsEnabled(has);
            if (has) {
                radiusSpinner .setValue(b.getRadius());
                densitySpinner.setValue(b.getDensity());
                pinnedBox     .setSelected(b.isPinned());
                updateMassLabel();
            } else {
                massLabel.setText("mass: —");
            }
        } finally {
            updatingProps = false;
        }
    }

    private void updateMassLabel() {
        if (selectedBody == null) { massLabel.setText("mass: —"); return; }
        massLabel.setText(String.format("mass = ρ·π·r² = %.1f", selectedBody.getMass()));
    }

    private void setPropsEnabled(boolean enabled) {
        radiusSpinner .setEnabled(enabled);
        densitySpinner.setEnabled(enabled);
        pinnedBox     .setEnabled(enabled);
        deleteBtn     .setEnabled(enabled);
    }

    // -------------------------------------------------------------------------
    // Widget helpers
    // -------------------------------------------------------------------------

    private static JTextArea makeTextArea() {
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setBackground(new Color(20, 22, 35));
        ta.setForeground(new Color(180, 200, 220));
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        ta.setBorder(new EmptyBorder(2, 4, 2, 4));
        return ta;
    }

    private static JLabel makeLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(40, 50, 70));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private static JSpinner makeSpinner(SpinnerNumberModel model) {
        JSpinner sp = new JSpinner(model);
        sp.setPreferredSize(new Dimension(64, 20));
        sp.setMaximumSize(new Dimension(64, 20));
        sp.setBackground(new Color(30, 35, 55));
        if (sp.getEditor() instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(new Color(30, 35, 55));
            de.getTextField().setForeground(new Color(190, 210, 240));
            de.getTextField().setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
            de.getTextField().setColumns(5);
        }
        return sp;
    }
}
