package com.wizardrogue.core;

import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Timestamped ring-buffer of recent spell-key presses with a sliding combo window.
 *
 * <h2>How it works</h2>
 * <p>Each time the player presses a spell key (Q, E, or R), the panel pushes
 * its {@link KeyEvent} virtual-key code here via {@link #push(int)}.  Old
 * entries older than {@value #WINDOW_MS} ms are lazily expired on each push.
 *
 * <p>{@link #findMatch(List)} iterates the known spell list and checks whether
 * the tail of the buffer exactly matches each spell's key sequence, using both
 * order <em>and</em> timing.  The first matching spell is returned and the
 * buffer is cleared so that a cast cannot trigger multiple overlapping sequences.
 *
 * <h2>Why this matters (keyboard input theory)</h2>
 * <pre>
 *   Simple key listener    → fires one event per keypress, stateless
 *   Key-state set          → knows which keys are currently held (movement)
 *   THIS CLASS             → knows the recent history of typed keys + timing
 * </pre>
 * The combination of the three techniques covers every category of keyboard
 * input a real-time game needs.
 *
 * <h2>Combo window</h2>
 * <p>The window ({@value #WINDOW_MS} ms) is intentionally generous so players
 * can complete spells without rushing, while still expiring stale fragments that
 * would block later sequences.
 */
public final class InputBuffer {

    /**
     * Maximum age (ms) of a key entry before it is discarded.
     * Entries older than this cannot contribute to a spell match.
     */
    public static final long WINDOW_MS = 2_500;

    /** The three spell-input keys (everything else is ignored here). */
    public static final int[] SPELL_KEYS = {
        KeyEvent.VK_Q,
        KeyEvent.VK_E,
        KeyEvent.VK_R
    };

    /** One entry in the buffer: a key code with the millisecond it was pressed. */
    private record TimedKey(int keyCode, long timeMs) {}

    private final Deque<TimedKey> buffer = new ArrayDeque<>();

    // ------------------------------------------------------------------ push

    /**
     * Records a key press.  Entries older than the combo window are pruned first
     * so the buffer never grows without bound.
     */
    public void push(int keyCode) {
        prune();
        buffer.addLast(new TimedKey(keyCode, System.currentTimeMillis()));
    }

    // ------------------------------------------------------------------ matching

    /**
     * Scans {@code spells} for a sequence match against the current buffer tail.
     *
     * <p>A match requires:
     * <ol>
     *   <li>The last {@code spell.sequenceLength()} buffer entries equal the
     *       spell's key sequence in order.</li>
     *   <li>The oldest of those entries is within {@value #WINDOW_MS} ms of the
     *       newest (all keys typed within the same combo window).</li>
     *   <li>The player has sufficient MP and meets the spell's level requirement
     *       (checked by the caller via {@link Spell#canCast(Player)}).</li>
     * </ol>
     * The buffer is <b>not</b> cleared here; the caller clears it after casting.
     *
     * @return the first matching spell, or {@code null} if none
     */
    public Spell findMatch(List<Spell> spells) {
        prune();
        List<TimedKey> recent = new ArrayList<>(buffer);
        if (recent.isEmpty()) return null;

        for (Spell spell : spells) {
            int[] seq = spell.getSequence();
            if (recent.size() < seq.length) continue;

            int start = recent.size() - seq.length;
            boolean ok = true;
            for (int i = 0; i < seq.length; i++) {
                if (recent.get(start + i).keyCode() != seq[i]) { ok = false; break; }
            }
            if (!ok) continue;

            // Timing check: first to last must be within the window
            long t0 = recent.get(start).timeMs();
            long t1 = recent.get(recent.size() - 1).timeMs();
            if (t1 - t0 <= WINDOW_MS) return spell;
        }
        return null;
    }

    /** Removes all entries — called after a spell is successfully cast. */
    public void clear() { buffer.clear(); }

    // ------------------------------------------------------------------ display

    /**
     * Returns a human-readable string of the currently buffered spell keys,
     * for display in the stats panel's "INPUT:" row.
     *
     * <p>Example: if the player typed Q then E, this returns {@code "Q E _"}.
     */
    public String getDisplayString() {
        prune();
        if (buffer.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (TimedKey tk : buffer) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(keyLabel(tk.keyCode()));
        }
        return sb.toString();
    }

    /**
     * Returns how many keys are currently in the (non-expired) buffer.
     * Useful for the UI to show a "building a combo" indicator.
     */
    public int size() { prune(); return buffer.size(); }

    // ------------------------------------------------------------------ private helpers

    private void prune() {
        long cutoff = System.currentTimeMillis() - WINDOW_MS;
        while (!buffer.isEmpty() && buffer.peekFirst().timeMs() < cutoff) {
            buffer.removeFirst();
        }
    }

    private static String keyLabel(int vk) {
        return switch (vk) {
            case KeyEvent.VK_Q -> "Q";
            case KeyEvent.VK_E -> "E";
            case KeyEvent.VK_R -> "R";
            default            -> "?";
        };
    }

    /** Returns {@code true} if the given key code is one of the three spell keys. */
    public static boolean isSpellKey(int vk) {
        for (int sk : SPELL_KEYS) if (sk == vk) return true;
        return false;
    }
}
