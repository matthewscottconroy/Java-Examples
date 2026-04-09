package com.combustionengine.ui;

import com.combustionengine.model.EnginePreset;

import javax.swing.*;
import java.awt.*;

/**
 * Application window for the Otto-cycle combustion engine simulator.
 *
 * <p>Layout: simulation canvas on the left, control panel on the right.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Combustion Engine — Otto-Cycle Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        SimulationPanel sim  = new SimulationPanel(EnginePreset.ECONOMY_4);
        ControlPanel    ctrl = new ControlPanel(sim);

        sim.setOnTick(ctrl::refresh);

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(new Color(20, 22, 30));
        content.add(sim,  BorderLayout.CENTER);
        content.add(ctrl, BorderLayout.EAST);

        setContentPane(content);
        pack();
        setLocationRelativeTo(null);
    }
}
