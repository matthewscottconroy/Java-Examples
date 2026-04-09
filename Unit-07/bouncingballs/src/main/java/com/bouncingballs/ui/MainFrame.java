package com.bouncingballs.ui;

import com.bouncingballs.model.Ball;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level window for the glass-jar bouncing-balls simulator.
 *
 * <p>The window itself acts as the jar: dragging it around the screen causes
 * the balls to slosh and jostle in response to the window's inertia.
 * {@link BallPanel} polls {@code getLocation()} each frame to compute this.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Glass Jar — Bouncing Balls");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        BallPanel    sim  = new BallPanel(this);
        ControlPanel ctrl = new ControlPanel(sim);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(8, 10, 22));
        content.add(sim,  BorderLayout.CENTER);
        content.add(ctrl, BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setLocationRelativeTo(null);

        // Seed a few starter balls so the jar isn't empty
        SwingUtilities.invokeLater(() -> {
            sim.setPendingSize(Ball.Size.MEDIUM);
            sim.setPendingDensity(Ball.Density.MEDIUM);
            sim.addBall(160, 80);
            sim.addBall(250, 60);
            sim.addBall(340, 80);

            sim.setPendingSize(Ball.Size.LARGE);
            sim.setPendingDensity(Ball.Density.LIGHT);
            sim.addBall(200, 50);

            sim.setPendingSize(Ball.Size.SMALL);
            sim.setPendingDensity(Ball.Density.HEAVY);
            sim.addBall(300, 70);
            sim.addBall(270, 50);

            // Restore defaults
            sim.setPendingSize(Ball.Size.MEDIUM);
            sim.setPendingDensity(Ball.Density.MEDIUM);
        });
    }
}
