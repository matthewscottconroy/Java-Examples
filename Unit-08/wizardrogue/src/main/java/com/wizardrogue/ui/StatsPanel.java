package com.wizardrogue.ui;

import com.wizardrogue.core.GameController;
import com.wizardrogue.core.Player;
import com.wizardrogue.core.Spell;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Right-side stats panel displaying the player's character sheet, known spells
 * with their Q/E/R sequences, and the live input-buffer display.
 *
 * <p>The <b>INPUT</b> section at the bottom of this panel is the visual
 * centrepiece of the keyboard-input demo: it shows exactly which spell keys
 * the player has typed in the last 2.5 seconds, with a colour cue that turns
 * gold when the buffer matches the start of a known sequence.
 */
public final class StatsPanel extends JPanel {

    private static final Color BG        = new Color(12, 10, 20);
    private static final Color BORDER    = new Color(50, 45, 75);
    private static final Color HEADING   = new Color(130, 150, 200);
    private static final Color VALUE     = new Color(210, 225, 240);
    private static final Color HP_FULL   = new Color(80,  210, 80);
    private static final Color HP_LOW    = new Color(210,  60, 60);
    private static final Color MP_COLOR  = new Color(60,  130, 240);
    private static final Color XP_COLOR  = new Color(220, 180, 50);
    private static final Color SPELL_CLR = new Color(190, 140, 255);
    private static final Color INPUT_KEY = new Color(100, 220, 255);
    private static final Color INPUT_MATCH = new Color(255, 210, 50);
    private static final Color DIM       = new Color(80,  80, 100);

    static final int WIDTH = 220;

    private final GameController controller;

    public StatsPanel(GameController controller) {
        this.controller = controller;
        setPreferredSize(new Dimension(WIDTH, 600));
        setBackground(BG);
        setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, BORDER));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Player p = controller.getPlayer();
        if (p == null) return;

        int x = 10, y = 18;
        int lineH = 17;

        // ── Title ────────────────────────────────────────────────────────────
        y = section(g, "CHARACTER", x, y);

        g.setFont(bold(13));
        g.setColor(new Color(50, 220, 255));
        g.drawString("@  Wizard", x, y); y += lineH;

        g.setFont(plain(12));
        g.setColor(VALUE);
        g.drawString("Floor: " + controller.getFloor()
            + "   Level: " + p.getLevel(), x, y); y += lineH;

        // ── HP bar ───────────────────────────────────────────────────────────
        y += 4;
        y = drawBar(g, "HP", p.getHp(), p.getMaxHp(),
            hpColor(p.getHp(), p.getMaxHp()), x, y);

        // ── MP bar ───────────────────────────────────────────────────────────
        y = drawBar(g, "MP", p.getMp(), p.getMaxMp(), MP_COLOR, x, y);

        // ── XP bar ───────────────────────────────────────────────────────────
        y = drawBar(g, "XP", p.getXp(), p.getXpToNext(), XP_COLOR, x, y);

        y += 4;
        g.setFont(plain(11));
        g.setColor(HEADING);
        g.drawString("ATK " + p.getTotalAtk()
            + "   DEF " + p.getTotalDef()
            + "   GP " + p.getGold(), x, y); y += lineH;

        // ── Equipment ────────────────────────────────────────────────────────
        y += 4;
        y = section(g, "EQUIPMENT", x, y);
        g.setFont(plain(11));
        g.setColor(VALUE);
        g.drawString("/ " + p.getWeaponName(), x, y); y += lineH;
        g.drawString("] " + p.getArmorName(),  x, y); y += lineH;

        // ── Spells ───────────────────────────────────────────────────────────
        y += 4;
        y = section(g, "SPELLS  (Q=Q  E=E  R=R)", x, y);

        List<Spell> spells = p.getKnownSpells();
        g.setFont(mono(11));
        String inputSoFar = controller.getInputBuffer().getDisplayString();

        for (Spell spell : spells) {
            boolean affordable = p.getMp() >= spell.getMpCost();
            String seq  = spell.sequenceLabel();
            String line = String.format("%-12s %s (%dmp)",
                spell.getName(), seq, spell.getMpCost());

            // Highlight the spell whose sequence starts with what the player typed
            boolean matching = !inputSoFar.isEmpty()
                && seq.startsWith(inputSoFar.replace(" ", " ").strip());
            g.setColor(matching ? INPUT_MATCH : (affordable ? SPELL_CLR : DIM));
            g.drawString(line, x, y);
            y += 15;

            if (y > getHeight() - 100) break; // clamp if too many spells
        }

        // ── Input buffer ─────────────────────────────────────────────────────
        y = getHeight() - 88;
        y = section(g, "INPUT  (2.5 s window)", x, y);

        // Background rect
        g.setColor(new Color(20, 18, 35));
        g.fillRoundRect(x - 2, y - 14, WIDTH - 16, 26, 6, 6);
        g.setColor(new Color(50, 45, 75));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(x - 2, y - 14, WIDTH - 16, 26, 6, 6);

        // Keys
        String display = controller.getInputBuffer().getDisplayString();
        if (display.isEmpty()) {
            g.setFont(italic(12));
            g.setColor(DIM);
            g.drawString("(press Q, E, or R to cast a spell)", x, y + 6);
        } else {
            g.setFont(mono(14));
            for (String part : display.split(" ")) {
                g.setColor(INPUT_KEY);
                g.drawString(part, x, y + 6);
                x += g.getFontMetrics().stringWidth(part) + 8;
            }
        }

        // Buffer size indicator dots
        int bufSize = controller.getInputBuffer().size();
        x = 10;
        y += 20;
        g.setFont(plain(10));
        g.setColor(HEADING);
        g.drawString("Buffer: " + bufSize + " key" + (bufSize == 1 ? "" : "s"), x, y);
    }

    // ------------------------------------------------------------------ helpers

    private int section(Graphics2D g, String title, int x, int y) {
        g.setFont(bold(10));
        g.setColor(HEADING);
        g.drawString(title, x, y);
        g.setColor(BORDER);
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x, y + 2, WIDTH - 12, y + 2);
        return y + 14;
    }

    private int drawBar(Graphics2D g, String label,
                        int cur, int max, Color fill,
                        int x, int y) {
        int bw = WIDTH - 24, bh = 10;
        g.setFont(plain(11));
        g.setColor(HEADING);
        g.drawString(label + ": " + cur + "/" + max, x, y);
        y += 13;
        g.setColor(new Color(30, 28, 45));
        g.fillRoundRect(x, y, bw, bh, 4, 4);
        if (max > 0) {
            int fw = (int)((double) cur / max * bw);
            g.setColor(fill);
            g.fillRoundRect(x, y, fw, bh, 4, 4);
        }
        g.setColor(BORDER);
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(x, y, bw, bh, 4, 4);
        return y + bh + 6;
    }

    private static Color hpColor(int hp, int maxHp) {
        float ratio = maxHp > 0 ? (float) hp / maxHp : 0;
        if (ratio > 0.5f) return HP_FULL;
        float t = ratio / 0.5f;
        return new Color(
            (int)(HP_LOW.getRed()   * (1 - t) + HP_FULL.getRed()   * t),
            (int)(HP_LOW.getGreen() * (1 - t) + HP_FULL.getGreen() * t),
            (int)(HP_LOW.getBlue()  * (1 - t) + HP_FULL.getBlue()  * t));
    }

    private static Font bold(int size)   { return new Font("SansSerif",   Font.BOLD,  size); }
    private static Font plain(int size)  { return new Font("SansSerif",   Font.PLAIN, size); }
    private static Font italic(int size) { return new Font("SansSerif",   Font.ITALIC,size); }
    private static Font mono(int size)   { return new Font("Monospaced",  Font.BOLD,  size); }
}
