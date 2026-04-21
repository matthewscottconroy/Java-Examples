package com.wizardrogue.ui;

import com.wizardrogue.core.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window for Wizard Rogue.
 *
 * <p>Layout:
 * <pre>
 *  ┌──────────────────────────────────┬────────────┐
 *  │   DungeonPanel                   │ StatsPanel │
 *  │   720 × 442  (dungeon)           │   220 px   │
 *  │   720 ×  88  (message log)       │            │
 *  │   ─────────────────────────────  │            │
 *  │   Total: 720 × 530               │            │
 *  └──────────────────────────────────┴────────────┘
 * </pre>
 */
public final class GameFrame extends JFrame {

    public GameFrame() {
        super("Wizard Rogue  —  Advanced Keyboard Input");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(8, 7, 12));

        GameController controller = new GameController();
        StatsPanel     stats      = new StatsPanel(controller);
        DungeonPanel   dungeon    = new DungeonPanel(controller, stats);

        setLayout(new BorderLayout(0, 0));
        add(dungeon, BorderLayout.CENTER);
        add(stats,   BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);

        // Keyboard focus must be on the dungeon panel, not the frame
        dungeon.requestFocusInWindow();
    }
}
