package com.buoyancy.pressure.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level window for the pressure-differential buoyancy simulator.
 *
 * <p>Lays out the simulation canvas ({@link PressurePanel}) in the centre and
 * the interactive control / readout panel ({@link ControlPanel}) on the right.
 * The simulation panel fires an {@code onTick} callback each frame so the
 * control panel can refresh its live physics readouts.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Buoyancy — Pressure Differential Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        PressurePanel sim  = new PressurePanel();
        ControlPanel  ctrl = new ControlPanel(sim);
        sim.setOnTick(ctrl::refresh);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(20, 24, 38));
        content.add(sim,  BorderLayout.CENTER);
        content.add(ctrl, BorderLayout.EAST);

        setContentPane(content);
        pack();
        setLocationRelativeTo(null);
    }
}
