package com.wizardrogue;

import com.wizardrogue.ui.GameFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Entry point for Wizard Rogue — a real-time ASCII roguelike demonstrating
 * advanced keyboard input processing.
 *
 * <p>The game explores three distinct keyboard-input techniques:
 * <ol>
 *   <li><b>Key-state tracking</b> — a {@code Set<Integer>} of currently-held
 *       keys drives smooth, real-time player movement independent of the OS
 *       key-repeat rate.</li>
 *   <li><b>Sequence detection with a timing window</b> — every non-movement
 *       key press is pushed into an {@link com.wizardrogue.core.InputBuffer}
 *       with a timestamp. Spell casting checks whether the last N presses
 *       match a spell's pattern <em>and</em> occurred within a 2.5-second
 *       combo window, exactly like a fighting-game move system.</li>
 *   <li><b>Immediate single-key actions</b> — Space to pick up items, comma
 *       to descend stairs, and Escape to pause are handled directly in
 *       {@code keyPressed} without any buffering.</li>
 * </ol>
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
                    UIManager.put("control",              new Color(30, 28, 40));
                    UIManager.put("nimbusBase",           new Color(18, 20, 36));
                    UIManager.put("nimbusLightBackground",new Color(18, 20, 36));
                    UIManager.put("text",                 new Color(210, 220, 235));
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new GameFrame().setVisible(true));
    }
}
