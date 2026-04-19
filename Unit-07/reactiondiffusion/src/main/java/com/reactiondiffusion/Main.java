package com.reactiondiffusion;

import com.reactiondiffusion.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the Reaction-Diffusion simulator.
 *
 * <p>Launches an interactive window running the Gray-Scott model, which
 * produces emergent Turing patterns — spots, stripes, worms, spirals and more
 * depending on the feed and kill rate parameters.
 */
public final class Main {

    private Main() {}

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
