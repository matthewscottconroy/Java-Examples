package com.bugblaster.ui;

import com.bugblaster.core.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window for Bug Blaster 3000.
 *
 * <p>Layout:
 * <pre>
 *  ┌──────────────────────────────────────────┐
 *  │            GamePanel (900 × 620)         │
 *  │        (game world + mouse input)        │
 *  ├──────────────────────────────────────────┤
 *  │            HUDPanel  (900 × 72)          │
 *  │   score · wave · lives · charge meter    │
 *  └──────────────────────────────────────────┘
 * </pre>
 */
public final class GameFrame extends JFrame {

    public GameFrame() {
        super("Bug Blaster 3000 — Exterminator Edition");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(14, 12, 22));

        GameController controller = new GameController();
        HUDPanel       hud        = new HUDPanel(controller.getState());
        GamePanel      game       = new GamePanel(controller, hud);

        setLayout(new BorderLayout(0, 0));
        add(game, BorderLayout.CENTER);
        add(hud,  BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(getSize());

        // Ensure the game panel receives keyboard events immediately
        game.requestFocusInWindow();
    }
}
