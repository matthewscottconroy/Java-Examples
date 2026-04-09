package com.gameoflife.wolfram.ui;

import com.gameoflife.wolfram.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * Main window for the Wolfram 1D elementary cellular automaton explorer.
 *
 * <pre>
 *  ┌──────────────────────────────────────────────────────┐
 *  │  Rule Library [preset ▼] [Load]  Width:[200]  Cell:[4px]  │ ← top bar
 *  ├──────────────────────────────────────────────────────┤
 *  │                                                       │
 *  │   WolframPanel (space-time diagram, scrollable)       │
 *  │                                                       │
 *  ├──────────────────────────────────────────────────────┤
 *  │ Rule:[110]  Init:[▼]  [⏮] [◀] [▶Step] [▶▶Run] [■]   │ ← bottom bar
 *  │ Gen: 0  Width: 200  Density: 0.50  [Save] [Load]      │
 *  └──────────────────────────────────────────────────────┘
 * </pre>
 */
public final class WolframFrame extends JFrame {

    private static final Color BG_DARK = new Color(10, 12, 22);
    private static final Color BG_TOOL = new Color(16, 20, 34);
    private static final Color FG      = new Color(190, 210, 235);
    private static final Color ACCENT  = new Color(80, 200, 140);
    private static final Font  MONO    = new Font(Font.MONOSPACED, Font.PLAIN, 11);

    // ── Core ──────────────────────────────────────────────────────────────────
    private CAHistory history;
    private final javax.swing.Timer runTimer;

    // ── UI ────────────────────────────────────────────────────────────────────
    private final WolframPanel    wPanel  = new WolframPanel();
    private final JScrollPane     scroll;
    private final JLabel          genLabel    = mono("Gen: 0");
    private final JLabel          densLabel   = mono("Density: —");
    private final JLabel          ruleInfoLabel = mono("Rule 110");
    private final JTextArea       descArea;
    private       JToggleButton   runBtn;

    // ── Controls state ────────────────────────────────────────────────────────
    private JSpinner ruleSpinner;
    private JComboBox<String> initBox;
    private int stepsPerFrame = 1;

    public WolframFrame() {
        super("Wolfram 1D Elementary Cellular Automata");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(BG_DARK);

        descArea = makeDescArea();
        scroll   = new JScrollPane(wPanel);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(new Color(10, 12, 22));
        scroll.setBorder(null);

        runTimer = new javax.swing.Timer(30, e -> {
            history.step(stepsPerFrame);
            wPanel.revalidate();
            wPanel.repaint();
            wPanel.scrollToBottom(scroll);
            updateStatus();
        });

        history = new CAHistory(200, new WolframRule(110), CAHistory.InitCondition.SINGLE_CENTER);
        history.step(100);
        wPanel.setHistory(history);

        setLayout(new BorderLayout(0, 0));
        add(buildTopBar(),    BorderLayout.NORTH);
        add(scroll,           BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 640));
        SwingUtilities.invokeLater(() -> wPanel.scrollToBottom(scroll));
        updateStatus();
        updateRuleDescription(110);
    }

    // -------------------------------------------------------------------------
    // Top bar: library and display settings
    // -------------------------------------------------------------------------

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.setBackground(BG_TOOL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(35, 45, 70)),
            new EmptyBorder(2, 4, 2, 4)));

        // ── Preset library ────────────────────────────────────────────────────
        bar.add(label("Library:"));
        JComboBox<String> presetBox = new JComboBox<>();
        for (RuleLibrary.RuleEntry e : RuleLibrary.ENTRIES) presetBox.addItem(e.shortName());
        styleCombo(presetBox);
        presetBox.setPreferredSize(new Dimension(220, 24));
        bar.add(presetBox);

        JButton loadPreset = toolBtn("Load");
        loadPreset.addActionListener(e -> {
            int idx = presetBox.getSelectedIndex();
            if (idx >= 0 && idx < RuleLibrary.ENTRIES.size()) {
                RuleLibrary.RuleEntry entry = RuleLibrary.ENTRIES.get(idx);
                loadRule(entry.rule().number(), entry.defaultInit(), 0L);
            }
        });
        bar.add(loadPreset);
        bar.add(toolSep());

        // ── Width ─────────────────────────────────────────────────────────────
        bar.add(label("Width:"));
        JSpinner widthSpin = new JSpinner(new SpinnerNumberModel(200, 10, 2000, 10));
        styleSpinner(widthSpin, 75);
        widthSpin.addChangeListener(ev -> {
            int w = ((Number) widthSpin.getValue()).intValue();
            CAHistory.InitCondition ic = selectedInit();
            history = new CAHistory(w, history.getRule(), ic);
            history.step(100);
            wPanel.setHistory(history);
            wPanel.revalidate();
            wPanel.scrollToBottom(scroll);
            updateStatus();
        });
        bar.add(widthSpin);
        bar.add(toolSep());

        // ── Cell size ─────────────────────────────────────────────────────────
        bar.add(label("Cell size:"));
        JSpinner cellSpin = new JSpinner(new SpinnerNumberModel(4, 1, 20, 1));
        styleSpinner(cellSpin, 55);
        cellSpin.addChangeListener(ev -> {
            wPanel.setCellSize(((Number) cellSpin.getValue()).intValue());
            wPanel.revalidate();
        });
        bar.add(cellSpin);
        bar.add(toolSep());

        // ── Steps per frame ───────────────────────────────────────────────────
        bar.add(label("Steps/frame:"));
        JSpinner stepsSpin = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        styleSpinner(stepsSpin, 55);
        stepsSpin.addChangeListener(ev -> stepsPerFrame = ((Number) stepsSpin.getValue()).intValue());
        bar.add(stepsSpin);

        return bar;
    }

    // -------------------------------------------------------------------------
    // Bottom bar: rule, init condition, playback
    // -------------------------------------------------------------------------

    private JPanel buildBottomBar() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG_TOOL);
        outer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(35, 45, 70)));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        controls.setBackground(BG_TOOL);

        // ── Rule ──────────────────────────────────────────────────────────────
        controls.add(label("Rule:"));
        ruleSpinner = new JSpinner(new SpinnerNumberModel(110, 0, 255, 1));
        styleSpinner(ruleSpinner, 65);
        ruleSpinner.addChangeListener(e -> {
            int n = ((Number) ruleSpinner.getValue()).intValue();
            updateRuleDescription(n);
        });
        controls.add(ruleSpinner);
        controls.add(toolSep());

        // ── Init condition ────────────────────────────────────────────────────
        controls.add(label("Init:"));
        initBox = new JComboBox<>();
        for (CAHistory.InitCondition ic : CAHistory.InitCondition.values()) initBox.addItem(ic.label);
        styleCombo(initBox);
        controls.add(initBox);
        controls.add(toolSep());

        // ── Apply ─────────────────────────────────────────────────────────────
        JButton applyBtn = toolBtn("Apply & Reset");
        applyBtn.addActionListener(e -> {
            int n = ((Number) ruleSpinner.getValue()).intValue();
            loadRule(n, selectedInit(), new Random().nextLong());
        });
        controls.add(applyBtn);
        controls.add(toolSep());

        // ── Playback ──────────────────────────────────────────────────────────
        JButton rewindBtn = toolBtn("⏮");
        rewindBtn.setToolTipText("Rewind to initial condition");
        rewindBtn.addActionListener(e -> doRewind());
        controls.add(rewindBtn);

        JButton backBtn = toolBtn("◀");
        backBtn.setToolTipText("Step backward");
        backBtn.addActionListener(e -> doBack());
        controls.add(backBtn);

        JButton stepBtn = toolBtn("▶ Step");
        stepBtn.setToolTipText("Compute one more generation");
        stepBtn.addActionListener(e -> doStep(stepsPerFrame));
        controls.add(stepBtn);

        runBtn = new JToggleButton("▶▶ Run");
        runBtn.setFocusPainted(false);
        runBtn.setBackground(new Color(26, 32, 52));
        runBtn.setForeground(ACCENT);
        runBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        runBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(45, 58, 85), 1),
            new EmptyBorder(3, 8, 3, 8)));
        runBtn.addActionListener(e -> {
            if (runBtn.isSelected()) { runTimer.start(); runBtn.setText("■ Stop"); }
            else { runTimer.stop(); runBtn.setText("▶▶ Run"); }
        });
        controls.add(runBtn);
        controls.add(toolSep());

        // ── Save / Load ───────────────────────────────────────────────────────
        JButton saveBtn = toolBtn("💾 Save");
        saveBtn.addActionListener(e -> doSave());
        JButton loadBtn = toolBtn("📂 Load");
        loadBtn.addActionListener(e -> doLoad());
        controls.add(saveBtn);
        controls.add(loadBtn);

        // ── Status ────────────────────────────────────────────────────────────
        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        status.setBackground(BG_TOOL);
        status.add(ruleInfoLabel);
        status.add(genLabel);
        status.add(densLabel);

        outer.add(controls, BorderLayout.NORTH);
        outer.add(status,   BorderLayout.CENTER);

        // ── Rule description panel ────────────────────────────────────────────
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(30, 40, 60)));
        descScroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, 72));
        descScroll.setBackground(BG_TOOL);
        outer.add(descScroll, BorderLayout.SOUTH);

        return outer;
    }

    // -------------------------------------------------------------------------
    // Playback actions
    // -------------------------------------------------------------------------

    private void doStep(int n) {
        history.step(n);
        wPanel.revalidate();
        wPanel.repaint();
        wPanel.scrollToBottom(scroll);
        updateStatus();
    }

    private void doBack() {
        runTimer.stop(); runBtn.setSelected(false); runBtn.setText("▶▶ Run");
        for (int i = 0; i < stepsPerFrame; i++) history.stepBack();
        wPanel.revalidate();
        wPanel.repaint();
        updateStatus();
    }

    private void doRewind() {
        runTimer.stop(); runBtn.setSelected(false); runBtn.setText("▶▶ Run");
        history.rewind();
        wPanel.revalidate();
        wPanel.repaint();
        wPanel.scrollToTop(scroll);
        updateStatus();
    }

    // -------------------------------------------------------------------------
    // Rule loading
    // -------------------------------------------------------------------------

    private void loadRule(int ruleNum, CAHistory.InitCondition ic, long seed) {
        runTimer.stop(); runBtn.setSelected(false); runBtn.setText("▶▶ Run");
        WolframRule rule = new WolframRule(ruleNum);
        history = new CAHistory(history.width(), rule, ic, seed);
        history.step(100);
        wPanel.setHistory(history);
        wPanel.revalidate();
        wPanel.scrollToBottom(scroll);
        ruleSpinner.setValue(ruleNum);
        // Select init condition in combo
        for (int i = 0; i < CAHistory.InitCondition.values().length; i++) {
            if (CAHistory.InitCondition.values()[i] == ic) { initBox.setSelectedIndex(i); break; }
        }
        updateStatus();
        updateRuleDescription(ruleNum);
    }

    private void updateRuleDescription(int ruleNum) {
        RuleLibrary.RuleEntry entry = RuleLibrary.find(ruleNum);
        if (entry != null) {
            ruleInfoLabel.setText("Rule " + ruleNum + " (" + entry.shortName() + ")");
            descArea.setText(new WolframRule(ruleNum).ruleTable() + "\n\n" + entry.description());
        } else {
            ruleInfoLabel.setText("Rule " + ruleNum + " (custom)");
            descArea.setText(new WolframRule(ruleNum).ruleTable());
        }
    }

    // -------------------------------------------------------------------------
    // Save / Load
    // -------------------------------------------------------------------------

    private void doSave() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Wolfram CA (*.wca)", "wca"));
        fc.setSelectedFile(new File("rule" + history.getRule().number() +
            "_gen" + history.generations() + ".wca"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path path = fc.getSelectedFile().toPath();
        if (!path.toString().endsWith(".wca")) path = Path.of(path + ".wca");
        try {
            Files.writeString(path, history.toSaveString());
            JOptionPane.showMessageDialog(this, "Saved to " + path.getFileName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doLoad() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Wolfram CA (*.wca)", "wca"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            String text = Files.readString(fc.getSelectedFile().toPath());
            CAHistory loaded = CAHistory.fromSaveString(text);
            if (loaded == null) {
                JOptionPane.showMessageDialog(this, "Could not parse file.",
                    "Load Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            runTimer.stop(); runBtn.setSelected(false); runBtn.setText("▶▶ Run");
            history = loaded;
            wPanel.setHistory(history);
            wPanel.revalidate();
            wPanel.scrollToBottom(scroll);
            ruleSpinner.setValue(history.getRule().number());
            updateStatus();
            updateRuleDescription(history.getRule().number());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Cannot read file: " + ex.getMessage(),
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------------------
    // Status update
    // -------------------------------------------------------------------------

    private void updateStatus() {
        genLabel.setText(String.format("  Gen: %,d", history.generations() - 1));
        densLabel.setText(String.format("  Density: %.3f", history.currentDensity()));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CAHistory.InitCondition selectedInit() {
        return CAHistory.InitCondition.values()[initBox.getSelectedIndex()];
    }

    private JTextArea makeDescArea() {
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setBackground(new Color(14, 16, 28));
        ta.setForeground(new Color(170, 190, 215));
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        ta.setBorder(new EmptyBorder(4, 8, 4, 8));
        ta.setLineWrap(false);
        return ta;
    }

    private static JLabel mono(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        l.setForeground(new Color(160, 185, 215));
        return l;
    }

    private static JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        l.setForeground(new Color(120, 140, 175));
        return l;
    }

    private static JButton toolBtn(String t) {
        JButton b = new JButton(t);
        b.setFocusPainted(false);
        b.setBackground(new Color(26, 32, 52));
        b.setForeground(FG);
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(45, 58, 85), 1),
            new EmptyBorder(3, 8, 3, 8)));
        return b;
    }

    private static JSeparator toolSep() {
        JSeparator s = new JSeparator(JSeparator.VERTICAL);
        s.setPreferredSize(new Dimension(1, 22));
        s.setForeground(new Color(40, 52, 75));
        return s;
    }

    private static void styleSpinner(JSpinner sp, int w) {
        sp.setPreferredSize(new Dimension(w, 24));
        sp.setBackground(new Color(20, 25, 42));
        if (sp.getEditor() instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(new Color(20, 25, 42));
            de.getTextField().setForeground(FG);
            de.getTextField().setFont(MONO);
        }
    }

    private static void styleCombo(JComboBox<?> cb) {
        cb.setBackground(new Color(20, 25, 42));
        cb.setForeground(FG);
        cb.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
    }
}
