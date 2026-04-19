package com.reactiondiffusion.ui;

import com.reactiondiffusion.model.Preset;
import com.reactiondiffusion.model.ReactionDiffusionGrid;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Top-level application window for the Reaction-Diffusion simulator.
 *
 * <p>Hosts a single simulation canvas ({@link SimulationPanel}) in the
 * CENTER of a {@link BorderLayout}, with a parameter control strip
 * ({@link ControlPanel}) along the SOUTH edge.
 *
 * <p>The grid is constructed with 200 columns and 150 rows using the
 * "Spots" preset as the default parameter set, and is seeded with a single
 * central B blob before the animation loop starts.
 */
public class MainFrame extends JFrame {

    /** Default grid width in cells. */
    private static final int COLS = 200;

    /** Default grid height in cells. */
    private static final int ROWS = 150;

    public MainFrame() {
        super("Reaction-Diffusion — Gray-Scott Model");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Use the Spots preset as the default
        Preset defaultPreset = Preset.PRESETS.get(0);
        ReactionDiffusionGrid grid = new ReactionDiffusionGrid(
            ROWS, COLS, 0.2, 0.1, defaultPreset.F(), defaultPreset.k());

        // Seed an initial B blob at the grid centre
        grid.seed(COLS / 2, ROWS / 2, 5, new Random());

        SimulationPanel simPanel  = new SimulationPanel(grid);
        ControlPanel    ctrlPanel = new ControlPanel(grid, simPanel);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(8, 10, 22));
        content.add(simPanel,  BorderLayout.CENTER);
        content.add(ctrlPanel, BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setLocationRelativeTo(null);
    }
}
