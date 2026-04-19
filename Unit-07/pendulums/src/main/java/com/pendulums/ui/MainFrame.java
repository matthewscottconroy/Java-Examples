package com.pendulums.ui;

import com.pendulums.model.DoublePendulum;
import com.pendulums.model.NewtonsCradle;
import com.pendulums.model.SimplePendulum;
import com.pendulums.physics.Integrator;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level application window.
 *
 * <p>Hosts three simulation tabs in a {@link JTabbedPane}; each tab holds a
 * simulation canvas (CENTER) and a parameter-control strip (SOUTH).
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Pendulum Dynamics");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(20, 22, 36));
        tabs.setForeground(Color.WHITE);

        tabs.addTab("Simple Pendulum",  buildSimpleTab());
        tabs.addTab("Double Pendulum",  buildDoubleTab());
        tabs.addTab("Newton's Cradle",  buildCradleTab());

        setContentPane(tabs);
        pack();
        setLocationRelativeTo(null);
    }

    // -------------------------------------------------------------------------
    // Tab builders
    // -------------------------------------------------------------------------

    private JComponent buildSimpleTab() {
        SimplePendulum model = new SimplePendulum(
                Math.toRadians(60), 200.0, 980.0, 0.0, Integrator.Method.RK4);

        SimplePendulumPanel  canvas   = new SimplePendulumPanel(model);
        SimplePendulumControls ctrl   = new SimplePendulumControls(model, canvas);

        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(new Color(8, 10, 22));
        tab.add(canvas, BorderLayout.CENTER);
        tab.add(ctrl,   BorderLayout.SOUTH);
        return tab;
    }

    private JComponent buildDoubleTab() {
        DoublePendulum model = new DoublePendulum(
                Math.toRadians(120), Math.toRadians(90),
                140.0, 140.0,
                1.0, 1.0,
                980.0);

        DoublePendulumPanel    canvas = new DoublePendulumPanel(model);
        DoublePendulumControls ctrl   = new DoublePendulumControls(model, canvas);

        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(new Color(8, 10, 22));
        tab.add(canvas, BorderLayout.CENTER);
        tab.add(ctrl,   BorderLayout.SOUTH);
        return tab;
    }

    private JComponent buildCradleTab() {
        NewtonsCradle model = new NewtonsCradle(210.0, 22.0, 980.0, 1.0);
        model.reset(1, Math.toRadians(55));

        NewtonsCradlePanel    canvas = new NewtonsCradlePanel(model);
        NewtonsCradleControls ctrl   = new NewtonsCradleControls(model, canvas);

        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(new Color(8, 10, 22));
        tab.add(canvas, BorderLayout.CENTER);
        tab.add(ctrl,   BorderLayout.SOUTH);
        return tab;
    }
}
