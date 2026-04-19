package com.buffon.ui;

import com.buffon.model.BuffonExperiment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Control strip for the Buffon's Needle simulation.
 *
 * <p>Provides sliders for line spacing and needle length, a drop-rate
 * selector, a pause/resume toggle, and a reset button.  Changing either
 * geometry parameter resets the experiment because the old crossings are
 * no longer valid for the new geometry.
 */
class ControlPanel extends JPanel {

    private static final Color BG = new Color(15, 17, 30);
    private static final Color FG = new Color(200, 210, 230);

    private static final int[] SPEEDS       = {1, 5, 25, 150, 1000};
    private static final String[] SPEED_LBL = {"1", "5", "25", "150", "1 000"};

    ControlPanel(BuffonExperiment experiment, SimulationPanel panel) {
        setBackground(BG);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setLayout(new FlowLayout(FlowLayout.LEFT, 14, 4));

        // Line spacing
        add(namedSlider("Line spacing (px)", 40, 200, (int) experiment.getLineSpacing(),
                v -> experiment.setLineSpacing(v)));

        // Needle length
        add(namedSlider("Needle length (px)", 5, 200, (int) experiment.getNeedleLength(),
                v -> experiment.setNeedleLength(v)));

        // Drop speed
        JPanel speedPanel = new JPanel(new BorderLayout(0, 2));
        speedPanel.setBackground(BG);
        speedPanel.setPreferredSize(new Dimension(160, 58));
        speedPanel.add(styledLabel("Needles / frame:"), BorderLayout.NORTH);
        JComboBox<String> speedBox = new JComboBox<>(SPEED_LBL);
        speedBox.setBackground(new Color(30, 35, 55));
        speedBox.setForeground(FG);
        speedBox.setFont(new Font("Monospaced", Font.PLAIN, 12));
        speedBox.addActionListener(e -> panel.needlesPerFrame = SPEEDS[speedBox.getSelectedIndex()]);
        speedPanel.add(speedBox, BorderLayout.CENTER);
        add(speedPanel);

        // Pause / resume
        JButton pauseBtn = styledButton("Pause");
        pauseBtn.addActionListener(e -> {
            panel.paused = !panel.paused;
            pauseBtn.setText(panel.paused ? "Resume" : "Pause");
        });
        add(pauseBtn);

        // Burst — drop a large batch immediately
        JButton burstBtn = styledButton("Burst (100k)");
        burstBtn.addActionListener(e -> {
            experiment.drop(100_000, SimulationPanel.W, SimulationPanel.H);
            panel.repaint();
        });
        add(burstBtn);

        // Reset
        JButton resetBtn = styledButton("Reset");
        resetBtn.addActionListener(e -> experiment.reset());
        add(resetBtn);

        // Legend
        JPanel legend = new JPanel();
        legend.setBackground(BG);
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.add(legendSwatch(new Color(220, 75, 55), "crosses line"));
        legend.add(legendSwatch(new Color(70, 110, 170), "misses line"));
        add(legend);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private JPanel namedSlider(String label, int min, int max, int value, Consumer<Integer> onChange) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(BG);
        p.setPreferredSize(new Dimension(165, 58));

        JLabel lbl    = styledLabel(label + ": " + value);
        JSlider slider = new JSlider(min, max, value);
        slider.setBackground(BG);
        slider.setForeground(FG);

        slider.addChangeListener(e -> {
            lbl.setText(label + ": " + slider.getValue());
            onChange.accept(slider.getValue());
        });
        p.add(lbl,    BorderLayout.NORTH);
        p.add(slider, BorderLayout.CENTER);
        return p;
    }

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(FG);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return l;
    }

    private JButton styledButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(FG);
        b.setBackground(new Color(40, 50, 80));
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return b;
    }

    private JPanel legendSwatch(Color color, String label) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.setBackground(BG);
        JPanel swatch = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(color);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        swatch.setPreferredSize(new Dimension(24, 4));
        swatch.setOpaque(false);
        JLabel lbl = styledLabel(label);
        row.add(swatch);
        row.add(lbl);
        return row;
    }
}
