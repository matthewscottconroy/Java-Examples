package com.markovmonopoly;

import com.markovmonopoly.gui.MarkovAppFrame;

import javax.swing.*;

/**
 * Entry point — launches the interactive Markov Chain Explorer GUI.
 *
 * <p>The application opens a window with four tabs:
 * <ul>
 *   <li><b>Graph</b> — animated directed-graph visualization with a random walker.</li>
 *   <li><b>Matrix</b> — editable transition-probability table.</li>
 *   <li><b>Analysis</b> — stationary distribution bar chart, state classification,
 *       convergence profile.</li>
 *   <li><b>Board</b> — Monopoly board heat-map (enabled for the Monopoly chain).</li>
 * </ul>
 *
 * <p>Use the chain selector in the toolbar to switch between:
 * Weather Model, Gambler's Ruin, PageRank, Ehrenfest Urn, and Monopoly.
 */
public final class Main {

    private Main() {}

    public static void main(String[] args) {
        // Apply a dark look-and-feel if available; fall back to system L&F
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    // Dark Nimbus overrides
                    UIManager.put("control",        new java.awt.Color(40, 48, 70));
                    UIManager.put("info",           new java.awt.Color(40, 48, 70));
                    UIManager.put("nimbusBase",     new java.awt.Color(18, 30, 66));
                    UIManager.put("nimbusAlertYellow", new java.awt.Color(248, 187, 0));
                    UIManager.put("nimbusDisabledText", new java.awt.Color(128, 128, 128));
                    UIManager.put("nimbusFocus",    new java.awt.Color(115, 164, 209));
                    UIManager.put("nimbusGreen",    new java.awt.Color(176, 179, 50));
                    UIManager.put("nimbusInfoBlue", new java.awt.Color(66, 139, 221));
                    UIManager.put("nimbusLightBackground", new java.awt.Color(18, 30, 66));
                    UIManager.put("nimbusOrange",   new java.awt.Color(191, 98, 4));
                    UIManager.put("nimbusRed",      new java.awt.Color(169, 46, 34));
                    UIManager.put("nimbusSelectedText", java.awt.Color.WHITE);
                    UIManager.put("nimbusSelectionBackground", new java.awt.Color(57, 105, 138));
                    UIManager.put("text",           new java.awt.Color(230, 230, 230));
                    break;
                }
            }
        } catch (Exception ignored) {
            // If Nimbus is unavailable, fall back to the default L&F
        }

        SwingUtilities.invokeLater(() -> new MarkovAppFrame().setVisible(true));
    }
}
