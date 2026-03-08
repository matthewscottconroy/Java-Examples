package com.examples.dilemma.gui;

import com.examples.dilemma.engine.TournamentResult;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A simple bar chart drawn with {@link Graphics2D}, showing each strategy's total score.
 * Data is set via {@link #setData(List)} after a tournament completes.
 */
final class BarChartPanel extends JPanel {

    private static final Color[] BAR_COLORS = {
        new Color(0x5B9BD5), new Color(0x70AD47), new Color(0xED7D31),
        new Color(0xFFC000), new Color(0x4472C4), new Color(0x9E480E),
        new Color(0x636363), new Color(0x997300), new Color(0x255E91)
    };

    private List<TournamentResult.Standing> standings = List.of();

    /** Replaces the displayed data and repaints. */
    void setData(List<TournamentResult.Standing> standings) {
        this.standings = List.copyOf(standings);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (standings.isEmpty()) {
            g.setColor(Color.GRAY);
            g.drawString("Run the tournament to see the chart.", 20, 30);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int padLeft = 65, padRight = 20, padTop = 40, padBottom = 90;
        int chartW = getWidth()  - padLeft - padRight;
        int chartH = getHeight() - padTop  - padBottom;
        int n      = standings.size();
        int maxScore = standings.stream().mapToInt(TournamentResult.Standing::totalScore)
                .max().orElse(1);

        // --- title ---
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        String title = "Tournament Scores";
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(title, (getWidth() - tfm.stringWidth(title)) / 2, padTop - 12);

        // --- axes ---
        g2.setColor(Color.DARK_GRAY);
        g2.drawLine(padLeft, padTop, padLeft, padTop + chartH);
        g2.drawLine(padLeft, padTop + chartH, padLeft + chartW, padTop + chartH);

        // --- y-axis gridlines and labels ---
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics yfm = g2.getFontMetrics();
        int ySteps = 5;
        for (int i = 0; i <= ySteps; i++) {
            int val = maxScore * i / ySteps;
            int y   = padTop + chartH - chartH * i / ySteps;
            g2.setColor(new Color(200, 200, 200));
            g2.drawLine(padLeft + 1, y, padLeft + chartW, y);
            g2.setColor(Color.DARK_GRAY);
            String label = String.valueOf(val);
            g2.drawString(label, padLeft - yfm.stringWidth(label) - 4, y + yfm.getAscent() / 2);
        }

        // --- bars ---
        int step = chartW / n;
        int barW = Math.max(10, step * 2 / 3);

        for (int i = 0; i < n; i++) {
            TournamentResult.Standing s = standings.get(i);
            int barH = chartH * s.totalScore() / maxScore;
            int x    = padLeft + i * step + (step - barW) / 2;
            int y    = padTop + chartH - barH;

            g2.setColor(BAR_COLORS[i % BAR_COLORS.length]);
            g2.fillRect(x, y, barW, barH);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(x, y, barW, barH);

            // score label above bar
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            FontMetrics sfm = g2.getFontMetrics();
            String score = String.valueOf(s.totalScore());
            g2.drawString(score, x + (barW - sfm.stringWidth(score)) / 2, y - 3);

            // strategy name below axis, rotated 45°
            Graphics2D gr = (Graphics2D) g2.create();
            gr.setFont(new Font("SansSerif", Font.PLAIN, 11));
            gr.setColor(Color.DARK_GRAY);
            gr.translate(x + barW / 2, padTop + chartH + 6);
            gr.rotate(Math.PI / 4);
            gr.drawString(s.strategyName(), 0, 0);
            gr.dispose();
        }

        g2.dispose();
    }
}
