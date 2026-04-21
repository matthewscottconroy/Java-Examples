package com.bugblaster;

import com.bugblaster.ui.GameFrame;

import javax.swing.*;

/**
 * Entry point for Bug Blaster 3000 — the advanced mouse-input exterminator game.
 *
 * <p>Bugs are invading a home and marching toward the precious Snack Bowl in the
 * centre of the room. The player must eliminate them using every mouse technique
 * available before the bowl is overrun. Each bug type has a preferred weakness:
 *
 * <ul>
 *   <li><b>Ant</b>        – any spray or drag trail (1 HP)</li>
 *   <li><b>Cockroach</b>  – boot stomp (double-click) or charged blast (3 HP)</li>
 *   <li><b>Fly</b>        – precise single click; dodges the cursor (1 HP, fast)</li>
 *   <li><b>Spider</b>     – sticky trap; right-click to place (5 HP)</li>
 *   <li><b>Beetle</b>     – hold and release for a fully charged blast (8 HP)</li>
 * </ul>
 *
 * <p><b>Mouse controls:</b>
 * <pre>
 *   Left click             – Quick spray (small burst, 1 damage)
 *   Left double-click      – Boot stomp (big radius, 3 damage)
 *   Hold left + release    – Charged blast (hold ≥0.4 s; radius and damage grow with hold time)
 *   Left drag              – Continuous spray trail (good for lines of ants)
 *   Right click            – Place a sticky trap (catches and slowly kills bugs)
 *   Right drag on trap     – Reposition an existing trap
 *   Mouse movement         – Flies actively dodge the cursor
 * </pre>
 *
 * <p>Run with: {@code mvn exec:java}
 */
public final class Main {

    private Main() {}

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new GameFrame().setVisible(true));
    }
}
