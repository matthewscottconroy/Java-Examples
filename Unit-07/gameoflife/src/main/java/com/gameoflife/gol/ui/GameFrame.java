package com.gameoflife.gol.ui;

import com.gameoflife.gol.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Random;

/**
 * Main application window for Conway's Game of Life.
 *
 * <pre>
 *  ┌─────────────────────────────┬──────────┐
 *  │         GridPanel           │  Stats   │
 *  │         (scrollable)        │  Panel   │
 *  ├─────────────────────────────┴──────────┤
 *  │   Toolbar (playback + edit controls)   │
 *  └─────────────────────────────────────────┘
 * </pre>
 */
public final class GameFrame extends JFrame {

    private static final Color BG_DARK = new Color(10, 12, 20);
    private static final Color BG_TOOL = new Color(16, 20, 34);
    private static final Color FG      = new Color(190, 210, 235);

    // ── Core ─────────────────────────────────────────────────────────────────
    private GameController controller;
    private int targetFps = 10;
    private final javax.swing.Timer gameTimer;

    // ── UI ───────────────────────────────────────────────────────────────────
    private final GridPanel  gridPanel  = new GridPanel();
    private final StatsPanel statsPanel = new StatsPanel();

    // ── Playback buttons (kept as fields for state sync) ─────────────────────
    private JToggleButton playBtn;
    private JButton       backBtn;
    private JButton       rewindBtn;

    // ── Settings state ────────────────────────────────────────────────────────
    private Color aliveColor = new Color(80, 220, 130);
    private Color deadColor  = new Color(10,  13,  22);
    private Color gridColor  = new Color(22,  28,  44);

    public GameFrame() {
        super("Conway's Game of Life");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(BG_DARK);

        controller = new GameController(
            GridState.empty(60, 80, false), RuleSet.CONWAY);

        // ── Game loop ────────────────────────────────────────────────────────
        gameTimer = new javax.swing.Timer(1000 / targetFps, e -> {
            controller.stepForward();
            refreshUI();
        });

        setupGrid();

        // ── Layout ───────────────────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG_DARK);
        center.add(new JScrollPane(gridPanel), BorderLayout.CENTER);
        center.add(statsPanel, BorderLayout.EAST);

        add(center,         BorderLayout.CENTER);
        add(buildToolbar(), BorderLayout.SOUTH);

        setupKeyBindings();

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 620));

        // Center the grid view after first layout
        SwingUtilities.invokeLater(gridPanel::centerView);
    }

    // -------------------------------------------------------------------------
    // Grid wiring
    // -------------------------------------------------------------------------

    private void setupGrid() {
        gridPanel.setPreferredSize(new Dimension(800, 600));
        gridPanel.setState(controller.getCurrent());
        gridPanel.setAliveColor(aliveColor);
        gridPanel.setDeadColor(deadColor);
        gridPanel.setGridColor(gridColor);

        // Left click = toggle
        gridPanel.setOnToggle(rc -> {
            controller.editToggle(rc[0], rc[1]);
            gridPanel.setState(controller.getCurrent());
            refreshStats();
        });
        // Drag alive
        gridPanel.setOnPaintAlive(rc -> {
            controller.editAlive(rc[0], rc[1]);
            gridPanel.setState(controller.getCurrent());
        });
        // Drag dead
        gridPanel.setOnPaintDead(rc -> {
            controller.editDead(rc[0], rc[1]);
            gridPanel.setState(controller.getCurrent());
        });
        // Pattern placed
        gridPanel.setOnPatternPlace(rc -> {
            // Pattern was already selected; place it centered at (rc[0], rc[1])
        });
    }

    // -------------------------------------------------------------------------
    // Toolbar
    // -------------------------------------------------------------------------

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bar.setBackground(BG_TOOL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(35, 45, 70)),
            new EmptyBorder(2, 4, 2, 4)));

        // ── Playback controls ─────────────────────────────────────────────────
        rewindBtn = toolBtn("⏮");  rewindBtn.setToolTipText("Rewind to start");
        backBtn   = toolBtn("◀");  backBtn.setToolTipText("Step backward  (← key)");
        JButton stopBtn  = toolBtn("■");  stopBtn.setToolTipText("Stop & rewind");
        playBtn = toolToggle("▶");  playBtn.setToolTipText("Play / Pause  (Space)");
        JButton fwdBtn   = toolBtn("▶");  fwdBtn.setToolTipText("Step forward  (→ key)");
        JButton ffBtn    = toolBtn("⏭");  ffBtn.setToolTipText("Skip 10 generations");

        // Color overrides for clarity
        stopBtn.setForeground(new Color(220, 100, 100));
        playBtn.setForeground(new Color(100, 220, 140));

        rewindBtn.addActionListener(e -> doRewind());
        backBtn.addActionListener(e -> doStepBack());
        stopBtn.addActionListener(e -> doStop());
        playBtn.addActionListener(e -> doPlayPause());
        fwdBtn.addActionListener(e -> doStepForward());
        ffBtn.addActionListener(e -> doFastForward(10));

        bar.add(rewindBtn);
        bar.add(backBtn);
        bar.add(stopBtn);
        bar.add(playBtn);
        bar.add(fwdBtn);
        bar.add(ffBtn);
        bar.add(toolSep());

        // ── Speed ─────────────────────────────────────────────────────────────
        bar.add(toolLabel("Rate"));
        JSpinner fpsSpin = new JSpinner(new SpinnerNumberModel(targetFps, 1, 60, 1));
        styleSpinner(fpsSpin);
        fpsSpin.setToolTipText("Generations per second");
        fpsSpin.addChangeListener(e -> {
            targetFps = ((Number) fpsSpin.getValue()).intValue();
            gameTimer.setDelay(1000 / targetFps);
        });
        bar.add(fpsSpin);
        bar.add(toolLabel("gen/s"));
        bar.add(toolSep());

        // ── Pattern ───────────────────────────────────────────────────────────
        JButton patternBtn = toolBtn("☰ Pattern");
        patternBtn.setToolTipText("Open pattern library");
        patternBtn.addActionListener(e -> openPatternDialog());
        bar.add(patternBtn);

        // ── Random fill ───────────────────────────────────────────────────────
        JButton randBtn = toolBtn("⋯ Random");
        randBtn.setToolTipText("Fill with random cells");
        randBtn.addActionListener(e -> {
            double density = 0.3;
            String input = JOptionPane.showInputDialog(this,
                "Fill density (0.0 – 1.0):", "0.3");
            if (input != null) {
                try { density = Double.parseDouble(input.trim()); } catch (NumberFormatException ignored) {}
                density = Math.max(0.0, Math.min(1.0, density));
            }
            controller.randomFill(density, new Random().nextLong());
            gridPanel.setState(controller.getCurrent());
            refreshUI();
        });
        bar.add(randBtn);

        // ── Clear ─────────────────────────────────────────────────────────────
        JButton clearBtn = toolBtn("✕ Clear");
        clearBtn.setForeground(new Color(220, 100, 100));
        clearBtn.addActionListener(e -> {
            if (confirmClear()) {
                gameTimer.stop(); playBtn.setSelected(false);
                controller.clearBoard();
                gridPanel.setState(controller.getCurrent());
                refreshUI();
            }
        });
        bar.add(clearBtn);
        bar.add(toolSep());

        // ── Settings ──────────────────────────────────────────────────────────
        JButton settingsBtn = toolBtn("⚙ Settings");
        settingsBtn.addActionListener(e -> openSettings());
        bar.add(settingsBtn);

        // ── Save / Load ───────────────────────────────────────────────────────
        JButton saveBtn = toolBtn("💾 Save");
        saveBtn.addActionListener(e -> doSave());
        JButton loadBtn = toolBtn("📂 Load");
        loadBtn.addActionListener(e -> doLoad());
        bar.add(toolSep());
        bar.add(saveBtn);
        bar.add(loadBtn);

        return bar;
    }

    // -------------------------------------------------------------------------
    // Playback actions
    // -------------------------------------------------------------------------

    private void doPlayPause() {
        if (playBtn.isSelected()) {
            gameTimer.start();
            gridPanel.setEditable(false);
            playBtn.setText("⏸");
        } else {
            gameTimer.stop();
            gridPanel.setEditable(true);
            playBtn.setText("▶");
        }
        updateButtonStates();
    }

    private void doStop() {
        gameTimer.stop();
        playBtn.setSelected(false);
        playBtn.setText("▶");
        gridPanel.setEditable(true);
        doRewind();
    }

    private void doStepForward() {
        if (gameTimer.isRunning()) return;
        controller.stepForward();
        gridPanel.setState(controller.getCurrent());
        refreshUI();
    }

    private void doStepBack() {
        if (gameTimer.isRunning()) return;
        controller.stepBackward();
        gridPanel.setState(controller.getCurrent());
        refreshUI();
    }

    private void doRewind() {
        boolean wasRunning = gameTimer.isRunning();
        gameTimer.stop();
        playBtn.setSelected(false);
        playBtn.setText("▶");
        gridPanel.setEditable(true);
        controller.rewind();
        gridPanel.setState(controller.getCurrent());
        refreshUI();
        if (wasRunning) { gameTimer.start(); playBtn.setSelected(true); playBtn.setText("⏸"); }
    }

    private void doFastForward(int n) {
        if (gameTimer.isRunning()) return;
        controller.fastForward(n);
        gridPanel.setState(controller.getCurrent());
        refreshUI();
    }

    private void updateButtonStates() {
        boolean running = gameTimer.isRunning();
        backBtn.setEnabled(!running && controller.canGoBack());
        rewindBtn.setEnabled(!running && controller.canGoBack());
    }

    // -------------------------------------------------------------------------
    // Pattern dialog
    // -------------------------------------------------------------------------

    private void openPatternDialog() {
        boolean wasRunning = gameTimer.isRunning();
        if (wasRunning) { gameTimer.stop(); playBtn.setSelected(false); playBtn.setText("▶"); }

        PatternDialog dlg = new PatternDialog(this);
        dlg.setVisible(true);

        Pattern p = dlg.getSelectedPattern();
        if (p != null) {
            // Wire the pattern-place callback
            gridPanel.setOnPatternPlace(rc -> {
                controller.placePattern(p, rc[0], rc[1], true);
                gridPanel.setState(controller.getCurrent());
                gridPanel.cancelPatternPlacement();
                refreshStats();
            });
            gridPanel.startPatternPlacement(p);
        }
    }

    // -------------------------------------------------------------------------
    // Settings
    // -------------------------------------------------------------------------

    private void openSettings() {
        boolean wasRunning = gameTimer.isRunning();
        if (wasRunning) { gameTimer.stop(); playBtn.setSelected(false); playBtn.setText("▶"); }

        SettingsDialog.Settings cur = new SettingsDialog.Settings(
            controller.getCurrent().rows(), controller.getCurrent().cols(),
            controller.getCurrent().isToroidal(),
            controller.getRuleSet(),
            aliveColor, deadColor, gridColor,
            gridPanel.getCellSize());

        SettingsDialog dlg = new SettingsDialog(this, cur);
        dlg.setVisible(true);
        SettingsDialog.Settings s = dlg.getResult();
        if (s == null) return;

        // Non-destructive changes
        controller.setRuleSet(s.ruleSet());
        aliveColor = s.aliveColor();
        deadColor  = s.deadColor();
        gridColor  = s.gridColor();
        gridPanel.setAliveColor(aliveColor);
        gridPanel.setDeadColor(deadColor);
        gridPanel.setGridColor(gridColor);
        gridPanel.setCellSize(s.cellSize());

        // Destructive board-size change
        GridState g = controller.getCurrent();
        if (s.rows() != g.rows() || s.cols() != g.cols() || s.toroidal() != g.isToroidal()) {
            if (JOptionPane.showConfirmDialog(this,
                    "Changing board size clears the current state. Continue?",
                    "Confirm Resize", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                controller.reset(GridState.empty(s.rows(), s.cols(), s.toroidal()));
                gridPanel.setState(controller.getCurrent());
                SwingUtilities.invokeLater(gridPanel::centerView);
            }
        }
        refreshUI();
    }

    // -------------------------------------------------------------------------
    // Save / Load
    // -------------------------------------------------------------------------

    private void doSave() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CGOL State (*.cgol)", "cgol"));
        fc.setSelectedFile(new File("state_gen" + controller.getGeneration() + ".cgol"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path path = fc.getSelectedFile().toPath();
        if (!path.toString().endsWith(".cgol")) path = Path.of(path + ".cgol");
        try {
            StateIO.save(path, controller.getCurrent(), controller.getRuleSet(), controller.getGeneration());
            JOptionPane.showMessageDialog(this, "Saved to " + path.getFileName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doLoad() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CGOL State (*.cgol)", "cgol"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        StateIO.LoadResult r = StateIO.load(fc.getSelectedFile().toPath());
        if (!r.ok()) {
            JOptionPane.showMessageDialog(this, r.error(), "Load Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        gameTimer.stop(); playBtn.setSelected(false); playBtn.setText("▶");
        controller = new GameController(r.state(), r.ruleSet());
        // Restore generation counter
        for (int i = 0; i < r.generation(); i++) {/* generation is cosmetic; not replayed */}
        gridPanel.setState(controller.getCurrent());
        SwingUtilities.invokeLater(gridPanel::centerView);
        refreshUI();
    }

    // -------------------------------------------------------------------------
    // UI refresh
    // -------------------------------------------------------------------------

    private void refreshUI() {
        gridPanel.setState(controller.getCurrent());
        refreshStats();
        updateButtonStates();
    }

    private void refreshStats() {
        statsPanel.update(controller, targetFps);
    }

    // -------------------------------------------------------------------------
    // Keyboard shortcuts
    // -------------------------------------------------------------------------

    private void setupKeyBindings() {
        InputMap im = gridPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = gridPanel.getActionMap();

        im.put(KeyStroke.getKeyStroke("SPACE"), "playPause");
        am.put("playPause", action(e -> {
            playBtn.setSelected(!playBtn.isSelected());
            doPlayPause();
        }));

        im.put(KeyStroke.getKeyStroke("RIGHT"), "stepFwd");
        am.put("stepFwd", action(e -> doStepForward()));

        im.put(KeyStroke.getKeyStroke("LEFT"), "stepBack");
        am.put("stepBack", action(e -> doStepBack()));

        im.put(KeyStroke.getKeyStroke("HOME"), "rewind");
        am.put("rewind", action(e -> doRewind()));

        im.put(KeyStroke.getKeyStroke("ESCAPE"), "cancelPattern");
        am.put("cancelPattern", action(e -> gridPanel.cancelPatternPlacement()));
    }

    private static AbstractAction action(ActionListener l) {
        return new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { l.actionPerformed(e); }
        };
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean confirmClear() {
        return JOptionPane.showConfirmDialog(this,
            "Clear all cells and reset generation?", "Clear Board",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private static JButton toolBtn(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(new Color(26, 32, 52));
        b.setForeground(new Color(180, 200, 240));
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(45, 58, 85), 1),
            new EmptyBorder(3, 8, 3, 8)));
        return b;
    }

    private static JToggleButton toolToggle(String text) {
        JToggleButton b = new JToggleButton(text);
        b.setFocusPainted(false);
        b.setBackground(new Color(26, 32, 52));
        b.setForeground(new Color(100, 220, 140));
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(45, 58, 85), 1),
            new EmptyBorder(3, 10, 3, 10)));
        return b;
    }

    private static JLabel toolLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(120, 140, 175));
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        return l;
    }

    private static JSeparator toolSep() {
        JSeparator s = new JSeparator(JSeparator.VERTICAL);
        s.setPreferredSize(new Dimension(1, 22));
        s.setForeground(new Color(45, 55, 80));
        return s;
    }

    private static void styleSpinner(JSpinner sp) {
        sp.setPreferredSize(new Dimension(55, 24));
        sp.setBackground(new Color(22, 28, 46));
        if (sp.getEditor() instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(new Color(22, 28, 46));
            de.getTextField().setForeground(new Color(190, 210, 240));
            de.getTextField().setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        }
    }
}
