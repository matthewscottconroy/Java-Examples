package com.wizardrogue;

import com.wizardrogue.core.InputBuffer;
import com.wizardrogue.core.Spell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link InputBuffer} — the core of the combo-window spell system.
 *
 * <p>These tests verify that:
 * <ul>
 *   <li>Correct sequences are detected.</li>
 *   <li>Wrong sequences (different keys or wrong order) are not matched.</li>
 *   <li>The buffer returns {@code null} when not enough keys have been pressed.</li>
 *   <li>Clearing the buffer prevents re-matching.</li>
 *   <li>{@link InputBuffer#isSpellKey(int)} correctly filters keys.</li>
 * </ul>
 */
class InputBufferTest {

    private static final int Q = KeyEvent.VK_Q;
    private static final int E = KeyEvent.VK_E;
    private static final int R = KeyEvent.VK_R;

    private InputBuffer buffer;

    /** A dummy two-key spell (Q Q) for testing. */
    private Spell makeSpell(String name, int... seq) {
        return new Spell(name, "test spell", seq, 0, 1,
            (player, map, enemies, log) -> {});
    }

    @BeforeEach
    void setUp() {
        buffer = new InputBuffer();
    }

    @Test
    void emptyBufferMatchesNothing() {
        List<Spell> spells = List.of(makeSpell("Fire", Q, Q));
        assertNull(buffer.findMatch(spells));
    }

    @Test
    void singleKeyDoesNotMatchDoubleSequence() {
        List<Spell> spells = List.of(makeSpell("Fire", Q, Q));
        buffer.push(Q);
        assertNull(buffer.findMatch(spells));
    }

    @Test
    void correctDoubleSequenceMatches() {
        List<Spell> spells = List.of(makeSpell("Fire", Q, Q));
        buffer.push(Q);
        buffer.push(Q);
        assertNotNull(buffer.findMatch(spells));
    }

    @Test
    void correctTripleSequenceMatches() {
        List<Spell> spells = List.of(makeSpell("Lightning", Q, E, Q));
        buffer.push(Q);
        buffer.push(E);
        buffer.push(Q);
        assertNotNull(buffer.findMatch(spells));
    }

    @Test
    void wrongOrderDoesNotMatch() {
        List<Spell> spells = List.of(makeSpell("Lightning", Q, E, Q));
        buffer.push(E);
        buffer.push(Q);
        buffer.push(E);
        assertNull(buffer.findMatch(spells));
    }

    @Test
    void prefixKeysDoNotPreventLaterMatch() {
        List<Spell> spells = List.of(makeSpell("Fire", Q, Q));
        // Extra leading presses
        buffer.push(E);
        buffer.push(R);
        buffer.push(Q);
        buffer.push(Q);
        assertNotNull(buffer.findMatch(spells));
    }

    @Test
    void clearPreventsMatch() {
        List<Spell> spells = List.of(makeSpell("Fire", Q, Q));
        buffer.push(Q);
        buffer.push(Q);
        buffer.clear();
        assertNull(buffer.findMatch(spells));
    }

    @Test
    void longerSequenceTakesPrecedenceWhenMatchedFirst() {
        Spell short_ = makeSpell("Short", Q, Q);
        Spell long_  = makeSpell("Long",  Q, Q, E, E);
        List<Spell> spells = List.of(long_, short_);  // long listed first

        buffer.push(Q);
        buffer.push(Q);

        // Only short matches with 2 presses
        Spell m = buffer.findMatch(spells);
        // long_ needs 4 keys, not matched
        assertNotNull(m);
        assertEquals("Short", m.getName());
    }

    @Test
    void isSpellKeyOnlyReturnsTrueForQER() {
        assertTrue(InputBuffer.isSpellKey(KeyEvent.VK_Q));
        assertTrue(InputBuffer.isSpellKey(KeyEvent.VK_E));
        assertTrue(InputBuffer.isSpellKey(KeyEvent.VK_R));
        assertFalse(InputBuffer.isSpellKey(KeyEvent.VK_W));
        assertFalse(InputBuffer.isSpellKey(KeyEvent.VK_A));
        assertFalse(InputBuffer.isSpellKey(KeyEvent.VK_SPACE));
        assertFalse(InputBuffer.isSpellKey(KeyEvent.VK_1));
    }

    @Test
    void displayStringReflectsBuffer() {
        buffer.push(Q);
        buffer.push(E);
        String display = buffer.getDisplayString();
        assertTrue(display.contains("Q"), "Should contain Q");
        assertTrue(display.contains("E"), "Should contain E");
    }

    @Test
    void displayStringEmptyAfterClear() {
        buffer.push(Q);
        buffer.clear();
        assertEquals("", buffer.getDisplayString());
    }

    @Test
    void sizeReflectsBufferContents() {
        assertEquals(0, buffer.size());
        buffer.push(Q);
        assertEquals(1, buffer.size());
        buffer.push(E);
        assertEquals(2, buffer.size());
        buffer.clear();
        assertEquals(0, buffer.size());
    }
}
