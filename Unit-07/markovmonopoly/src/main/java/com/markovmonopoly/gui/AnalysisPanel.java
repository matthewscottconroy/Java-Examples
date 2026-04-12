package com.markovmonopoly.gui;

import com.markovmonopoly.core.MarkovAnalysis;
import com.markovmonopoly.core.MarkovChain;
import com.markovmonopoly.core.StateClass;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Analysis panel — shows stationary distribution, state classification,
 * convergence preview, and key chain properties.
 *
 * <h2>Sections</h2>
 * <ol>
 *   <li><b>Stationary distribution</b>: horizontal bar chart of π[i],
 *       updated whenever the active chain changes.</li>
 *   <li><b>Convergence</b>: table showing how fast the distribution from a
 *       pure starting state approaches π as steps increase.</li>
 *   <li><b>State classification</b>: RECURRENT / TRANSIENT / ABSORBING per state,
 *       plus ergodicity flags.</li>
 * </ol>
 */
public final class AnalysisPanel extends JPanel {

    private static final Color BG        = new Color(20, 25, 40);
    private static final Color BAR_COLOR = new Color(60, 130, 220);
    private static final Color BAR_HL    = new Color(255, 200, 50);

    // Split pane: bar chart (left) | text analysis (right)
    private final BarChartPanel barChart  = new BarChartPanel();
    private final JTextArea     textArea  = buildTextArea();
    private final JScrollPane   textScroll;

    // ── Construction ──────────────────────────────────────────────────────────

    public AnalysisPanel() {
        setBackground(BG);
        setLayout(new BorderLayout(4, 0));

        textScroll = new JScrollPane(textArea);
        textScroll.setBorder(BorderFactory.createLineBorder(new Color(50, 65, 110)));
        textScroll.setBackground(BG);
        textScroll.getViewport().setBackground(new Color(24, 30, 48));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, barChart, textScroll);
        split.setDividerLocation(400);
        split.setResizeWeight(0.55);
        split.setBackground(BG);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);
        add(buildLegend(), BorderLayout.SOUTH);
    }

    public void onChainChanged(MarkovChain chain) {
        barChart.setChain(chain);
        textArea.setText(buildAnalysisText(chain));
        textArea.setCaretPosition(0);
    }

    // ── Text analysis ─────────────────────────────────────────────────────────

    private String buildAnalysisText(MarkovChain chain) {
        if (chain == null) return "";
        StringBuilder sb = new StringBuilder();
        int n = chain.size();

        // ── Chain overview ────────────────────────────────────────────────────
        sb.append("══ ").append(chain.getName()).append(" ══\n");
        if (!chain.getDescription().isEmpty())
            sb.append(chain.getDescription()).append("\n");
        sb.append("States: ").append(n).append("\n\n");

        // ── Stationary distribution ───────────────────────────────────────────
        sb.append("── Stationary Distribution π ──\n");
        sb.append("  π satisfies π·P = π  (long-run visit frequencies)\n\n");
        double[] pi;
        try {
            pi = MarkovAnalysis.stationaryDistribution(chain);
        } catch (Exception e) {
            sb.append("  [Could not compute: ").append(e.getMessage()).append("]\n");
            return sb.toString();
        }

        // Find max for formatting
        double maxPi = 0;
        for (double v : pi) maxPi = Math.max(maxPi, v);
        sb.append(String.format("  %-14s  %-8s  %s%n", "State", "π[i]", "Bar"));
        sb.append("  " + "─".repeat(55) + "\n");
        for (int i = 0; i < n && i < 50; i++) {
            int barLen = maxPi > 0 ? (int) Math.round(pi[i] / maxPi * 24) : 0;
            sb.append(String.format("  %-14s  %-8.5f  %s%n",
                truncate(chain.getLabel(i), 13), pi[i], "█".repeat(barLen)));
        }
        if (n > 50) sb.append("  … (").append(n - 50).append(" more states)\n");

        // ── Properties ────────────────────────────────────────────────────────
        sb.append("\n── Chain Properties ──\n");
        try {
            boolean irred   = MarkovAnalysis.isIrreducible(chain);
            boolean aperiod = MarkovAnalysis.isAperiodic(chain);
            boolean ergodic = MarkovAnalysis.isErgodic(chain);
            sb.append(String.format("  Irreducible : %s%n", irred   ? "YES — every state reachable from every other" : "NO — some states are unreachable from others"));
            sb.append(String.format("  Aperiodic   : %s%n", aperiod ? "YES — no periodic cycling" : "NO — chain has periodic behavior"));
            sb.append(String.format("  Ergodic     : %s%n", ergodic ? "YES — unique stationary distribution, guaranteed convergence" : "NO"));
        } catch (Exception e) {
            sb.append("  [Properties unavailable]\n");
        }

        // ── State classification ──────────────────────────────────────────────
        sb.append("\n── State Classification ──\n");
        sb.append("  RECURRENT: chain is guaranteed to return infinitely often\n");
        sb.append("  TRANSIENT:  chain may never return; eventually absorbed\n");
        sb.append("  ABSORBING:  once entered, never leaves (self-loop prob = 1)\n\n");
        try {
            Map<Integer, StateClass> classes = MarkovAnalysis.classifyAllStates(chain);
            sb.append(String.format("  %-14s  %s%n", "State", "Classification"));
            sb.append("  " + "─".repeat(35) + "\n");
            for (int i = 0; i < n && i < 50; i++) {
                StateClass sc = classes.getOrDefault(i, StateClass.RECURRENT);
                sb.append(String.format("  %-14s  %s%n",
                    truncate(chain.getLabel(i), 13), sc));
            }
            if (n > 50) sb.append("  … (").append(n - 50).append(" more)\n");
        } catch (Exception e) {
            sb.append("  [Classification unavailable]\n");
        }

        // ── Convergence preview ───────────────────────────────────────────────
        sb.append("\n── Convergence from State 0 ──\n");
        sb.append("  TV distance = max deviation from stationary distribution\n\n");
        try {
            int[] steps = {0, 1, 2, 5, 10, 20, 50, 100};
            sb.append(String.format("  %-8s  %-12s%n", "Steps", "TV Distance"));
            sb.append("  " + "─".repeat(22) + "\n");
            for (int s : steps) {
                double[] dist = MarkovAnalysis.distributionAfterSteps(chain, 0, s);
                double tv     = MarkovAnalysis.totalVariationDistance(dist, pi);
                sb.append(String.format("  %-8d  %.8f%n", s, tv));
            }
        } catch (Exception e) {
            sb.append("  [Convergence data unavailable]\n");
        }

        return sb.toString();
    }

    private static String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private static JTextArea buildTextArea() {
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setBackground(new Color(24, 30, 48));
        ta.setForeground(new Color(195, 215, 250));
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        ta.setLineWrap(false);
        ta.setTabSize(2);
        ta.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return ta;
    }

    private JPanel buildLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        p.setBackground(new Color(26, 32, 52));
        JLabel l1 = new JLabel("█ Stationary probability bars (left) — taller bar = higher long-run frequency");
        l1.setForeground(new Color(130, 160, 210));
        l1.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        p.add(l1);
        return p;
    }

    // ── Bar chart ─────────────────────────────────────────────────────────────

    private static final class BarChartPanel extends JPanel {
        private double[]    pi;
        private String[]    labels;
        private int         n;
        private int         hovered = -1;

        BarChartPanel() {
            setBackground(BG);
            setMinimumSize(new Dimension(200, 200));
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    hovered = barAt(e.getX());
                    repaint();
                }
            });
        }

        void setChain(MarkovChain chain) {
            if (chain == null) { pi = null; n = 0; repaint(); return; }
            n      = chain.size();
            labels = new String[n];
            for (int i = 0; i < n; i++) labels[i] = chain.getLabel(i);
            try {
                pi = MarkovAnalysis.stationaryDistribution(chain);
            } catch (Exception e) {
                pi = null;
            }
            repaint();
        }

        private int barAt(int mouseX) {
            if (n == 0) return -1;
            int W = getWidth();
            int pad = 10;
            double barW = (double)(W - 2 * pad) / n;
            int idx = (int)((mouseX - pad) / barW);
            return (idx >= 0 && idx < n) ? idx : -1;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            if (pi == null || n == 0) {
                g0.setColor(new Color(90, 110, 160));
                g0.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                g0.drawString("No data", getWidth() / 2 - 30, getHeight() / 2);
                return;
            }

            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int padL = 45, padR = 10, padT = 30, padB = 50;
            int chartW = W - padL - padR;
            int chartH = H - padT - padB;

            // Title
            g.setColor(new Color(180, 200, 240));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g.drawString("Stationary Distribution  π", padL, 18);

            // Axes
            g.setColor(new Color(70, 90, 140));
            g.drawLine(padL, padT, padL, padT + chartH);
            g.drawLine(padL, padT + chartH, padL + chartW, padT + chartH);

            if (n == 0) return;
            double maxPi = 0;
            for (double v : pi) maxPi = Math.max(maxPi, v);
            if (maxPi < 1e-12) maxPi = 1.0;

            // Y-axis tick
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
            g.setColor(new Color(100, 120, 170));
            for (int t = 0; t <= 4; t++) {
                double val = maxPi * t / 4.0;
                int y = padT + chartH - (int)(chartH * t / 4.0);
                g.drawLine(padL - 3, y, padL, y);
                g.drawString(String.format("%.3f", val), 2, y + 4);
            }

            // Bars
            double barW = (double) chartW / n;
            for (int i = 0; i < n; i++) {
                int barH   = (int)(chartH * pi[i] / maxPi);
                int x      = padL + (int)(i * barW);
                int y      = padT + chartH - barH;
                int bw     = Math.max(1, (int)barW - 2);

                Color fill = (i == hovered) ? BAR_HL : BAR_COLOR;
                g.setColor(fill);
                g.fillRect(x + 1, y, bw, barH);
                g.setColor(fill.darker());
                g.drawRect(x + 1, y, bw, barH);

                // State label (rotate if many states)
                g.setColor(new Color(160, 180, 220));
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(7, 10 - n / 8)));
                String lbl = labels[i].length() > 6 ? labels[i].substring(0, 5) + "." : labels[i];
                int lx = x + (int)(barW / 2);
                int ly = padT + chartH + 12;
                // Rotate label if many states
                if (n > 12) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.translate(lx, ly);
                    g2.rotate(Math.toRadians(-45));
                    g2.drawString(lbl, 0, 0);
                    g2.dispose();
                } else {
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(lbl, lx - fm.stringWidth(lbl) / 2, ly);
                }

                // Tooltip-style value on hover
                if (i == hovered) {
                    g.setColor(new Color(255, 235, 100));
                    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                    String val = String.format("%.4f", pi[i]);
                    FontMetrics fm = g.getFontMetrics();
                    int tx = Math.min(lx, W - fm.stringWidth(val) - 4);
                    g.drawString(val, tx, Math.max(y - 4, padT + 12));
                }
            }
        }
    }
}
