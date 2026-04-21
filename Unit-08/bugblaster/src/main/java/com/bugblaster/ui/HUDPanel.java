package com.bugblaster.ui;

import com.bugblaster.core.GameState;

import javax.swing.*;
import java.awt.*;

/**
 * Heads-up display panel shown below the game area.
 *
 * <p>Displays score, lives (as tiny bug-spray cans), current wave number, and
 * the charge-meter while the player holds the left mouse button.
 *
 * <p>The panel exposes {@link #setChargeProgress(float)} so the game panel can
 * push the current charge level each tick without the HUD needing a direct
 * reference to the controller.
 */
public final class HUDPanel extends JPanel {

    private static final Color BG       = new Color(14, 12, 22);
    private static final Color FG       = new Color(190, 210, 235);
    private static final Color ACCENT   = new Color(80,  220, 130);
    private static final Color LIFE_CLR = new Color(255, 100,  80);
    private static final Color CHARGE_EMPTY  = new Color(40,  40,  60);
    private static final Color CHARGE_LOW    = new Color(60,  180, 220);
    private static final Color CHARGE_FULL   = new Color(255, 200,  50);
    private static final Color WAVE_CLR = new Color(220, 180, 80);

    private final GameState state;
    private float chargeProgress;  // 0.0 – 1.0

    public HUDPanel(GameState state) {
        this.state = state;
        setPreferredSize(new Dimension(900, 72));
        setBackground(BG);
        setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(40, 40, 70)));
    }

    /** Updates the charge-meter display (0.0 = empty, 1.0 = fully charged). */
    public void setChargeProgress(float progress) {
        this.chargeProgress = Math.min(1f, Math.max(0f, progress));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // ── Score ────────────────────────────────────────────────────────────
        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        g.setColor(new Color(130, 150, 180));
        g.drawString("SCORE", 18, 22);
        g.setFont(new Font("Monospaced", Font.BOLD, 26));
        g.setColor(ACCENT);
        g.drawString(String.format("%07d", state.getScore()), 18, 52);

        // ── Wave ─────────────────────────────────────────────────────────────
        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        g.setColor(new Color(130, 150, 180));
        g.drawString("WAVE", 200, 22);
        g.setFont(new Font("Monospaced", Font.BOLD, 26));
        g.setColor(WAVE_CLR);
        g.drawString(String.valueOf(state.getWave()), 200, 52);

        // Combo
        if (state.getCombo() > 1) {
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.setColor(new Color(255, 160, 40));
            g.drawString("×" + state.getCombo() + " COMBO", 260, 52);
        }

        // ── Lives ────────────────────────────────────────────────────────────
        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        g.setColor(new Color(130, 150, 180));
        g.drawString("LIVES", w / 2 - 40, 22);
        for (int i = 0; i < GameState.STARTING_LIVES; i++) {
            boolean alive = i < state.getLives();
            drawSprayCan(g, w / 2 - 40 + i * 28, 28, alive);
        }

        // ── Charge meter ─────────────────────────────────────────────────────
        int meterX = w - 230, meterY = 14, meterW = 200, meterH = 18;
        g.setFont(new Font("Monospaced", Font.BOLD, 11));
        g.setColor(new Color(130, 150, 180));
        g.drawString("CHARGE (hold LMB)", meterX, meterY + 11);
        // Bar background
        g.setColor(CHARGE_EMPTY);
        g.fillRoundRect(meterX, meterY + 16, meterW, meterH, 6, 6);
        // Bar fill
        if (chargeProgress > 0) {
            Color fill = chargeProgress < 0.6f
                ? interpolate(CHARGE_LOW, CHARGE_FULL, chargeProgress / 0.6f)
                : CHARGE_FULL;
            g.setColor(fill);
            int fw = (int)(meterW * chargeProgress);
            g.fillRoundRect(meterX, meterY + 16, fw, meterH, 6, 6);
        }
        // Bar border
        g.setColor(new Color(60, 70, 110));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(meterX, meterY + 16, meterW, meterH, 6, 6);

        // "READY!" label when fully charged
        if (chargeProgress >= 0.98f) {
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.setColor(CHARGE_FULL);
            g.drawString("RELEASE!", meterX + meterW + 6, meterY + 29);
        }
    }

    private void drawSprayCan(Graphics2D g, int x, int y, boolean active) {
        Color c = active ? LIFE_CLR : new Color(50, 40, 55);
        // Can body
        g.setColor(c);
        g.fillRoundRect(x, y + 4, 12, 20, 4, 4);
        // Nozzle
        g.fillRect(x + 3, y, 6, 5);
        // Highlight
        if (active) {
            g.setColor(new Color(255, 160, 140));
            g.drawLine(x + 3, y + 6, x + 3, y + 18);
        }
    }

    private static Color interpolate(Color a, Color b, float t) {
        float s = 1f - t;
        return new Color(
            (int)(a.getRed()   * s + b.getRed()   * t),
            (int)(a.getGreen() * s + b.getGreen() * t),
            (int)(a.getBlue()  * s + b.getBlue()  * t)
        );
    }
}
