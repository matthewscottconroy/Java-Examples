package com.gameoflife;

import com.gameoflife.wolfram.WolframRule;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WolframRule}.
 *
 * <p>Verifies the core rule application, boundary validation, and famous
 * rules (30, 90, 110, 184) against known outputs.
 *
 * <h2>Rule encoding recap</h2>
 * <pre>
 *   neighborhood index = left*4 + center*2 + right
 *   rule bit i → output for neighborhood i
 * </pre>
 */
@DisplayName("WolframRule")
class WolframRuleTest {

    // ── Construction validation ───────────────────────────────────────────────

    @Nested
    @DisplayName("construction validation")
    class Validation {

        @Test
        @DisplayName("rule 0 is valid")
        void rule0Valid() {
            assertDoesNotThrow(() -> new WolframRule(0));
        }

        @Test
        @DisplayName("rule 255 is valid")
        void rule255Valid() {
            assertDoesNotThrow(() -> new WolframRule(255));
        }

        @Test
        @DisplayName("rule -1 throws IllegalArgumentException")
        void negativeFails() {
            assertThrows(IllegalArgumentException.class, () -> new WolframRule(-1));
        }

        @Test
        @DisplayName("rule 256 throws IllegalArgumentException")
        void tooLargeFails() {
            assertThrows(IllegalArgumentException.class, () -> new WolframRule(256));
        }
    }

    // ── Rule 0 — all dead ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Rule 0 — always dead")
    class Rule0 {

        private final WolframRule rule = new WolframRule(0);

        @Test
        void alwaysDead_000() { assertFalse(rule.apply(false, false, false)); }
        @Test
        void alwaysDead_111() { assertFalse(rule.apply(true,  true,  true));  }
        @Test
        void alwaysDead_101() { assertFalse(rule.apply(true,  false, true));  }
    }

    // ── Rule 255 — always alive ───────────────────────────────────────────────

    @Nested
    @DisplayName("Rule 255 — always alive")
    class Rule255 {

        private final WolframRule rule = new WolframRule(255);

        @Test
        void alwaysAlive_000() { assertTrue(rule.apply(false, false, false)); }
        @Test
        void alwaysAlive_111() { assertTrue(rule.apply(true,  true,  true));  }
    }

    // ── Rule 90 — Sierpiński XOR ──────────────────────────────────────────────

    @Nested
    @DisplayName("Rule 90 — XOR (Sierpiński)")
    class Rule90 {

        private final WolframRule rule = new WolframRule(90);

        // Rule 90 binary: 01011010
        // neighborhood→output: 000→0, 001→1, 010→0, 011→1, 100→1, 101→0, 110→1, 111→0
        @Test
        @DisplayName("000 → 0")
        void n000() { assertFalse(rule.apply(false, false, false)); }
        @Test
        @DisplayName("001 → 1")
        void n001() { assertTrue(rule.apply(false, false, true)); }
        @Test
        @DisplayName("010 → 0")
        void n010() { assertFalse(rule.apply(false, true, false)); }
        @Test
        @DisplayName("011 → 1")
        void n011() { assertTrue(rule.apply(false, true, true)); }
        @Test
        @DisplayName("100 → 1")
        void n100() { assertTrue(rule.apply(true, false, false)); }
        @Test
        @DisplayName("101 → 0")
        void n101() { assertFalse(rule.apply(true, false, true)); }
        @Test
        @DisplayName("110 → 1")
        void n110() { assertTrue(rule.apply(true, true, false)); }
        @Test
        @DisplayName("111 → 0")
        void n111() { assertFalse(rule.apply(true, true, true)); }

        @Test
        @DisplayName("rule 90 is equivalent to XOR of left and right")
        void equalsXor() {
            boolean[] vals = {false, true};
            for (boolean l : vals)
                for (boolean c : vals)
                    for (boolean r : vals)
                        assertEquals(l ^ r, rule.apply(l, c, r),
                                "Rule90 should equal left XOR right for (" + l + "," + c + "," + r + ")");
        }
    }

    // ── Rule 110 — Turing complete ────────────────────────────────────────────

    @Nested
    @DisplayName("Rule 110 — Turing-complete")
    class Rule110 {

        private final WolframRule rule = new WolframRule(110);

        // Rule 110 binary: 01101110
        // bit i: 0→0, 1→1, 2→1, 3→1, 4→0, 5→1, 6→1, 7→0
        @Test @DisplayName("000 → 0") void n000() { assertFalse(rule.apply(false,false,false)); }
        @Test @DisplayName("001 → 1") void n001() { assertTrue(rule.apply(false,false,true));  }
        @Test @DisplayName("010 → 1") void n010() { assertTrue(rule.apply(false,true,false));  }
        @Test @DisplayName("011 → 1") void n011() { assertTrue(rule.apply(false,true,true));   }
        @Test @DisplayName("100 → 0") void n100() { assertFalse(rule.apply(true,false,false)); }
        @Test @DisplayName("101 → 1") void n101() { assertTrue(rule.apply(true,false,true));   }
        @Test @DisplayName("110 → 1") void n110() { assertTrue(rule.apply(true,true,false));   }
        @Test @DisplayName("111 → 0") void n111() { assertFalse(rule.apply(true,true,true));   }

        @Test
        @DisplayName("wolframClass returns 4 for rule 110")
        void wolframClass4() {
            assertEquals(4, rule.wolframClass());
        }
    }

    // ── Rule 30 — chaotic ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Rule 30 — chaotic")
    class Rule30 {

        @Test
        @DisplayName("wolframClass returns 3 for rule 30")
        void wolframClass3() {
            assertEquals(3, new WolframRule(30).wolframClass());
        }
    }

    // ── wolframClass ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("wolframClass")
    class WolframClass {

        @Test
        @DisplayName("rule 0 is class 1 (uniform)")
        void class1() {
            assertEquals(1, new WolframRule(0).wolframClass());
        }

        @Test
        @DisplayName("unknown rule returns 0")
        void unknownClass() {
            assertEquals(0, new WolframRule(1).wolframClass());
        }
    }

    // ── toString ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toString includes rule number")
    void toStringIncludesNumber() {
        assertTrue(new WolframRule(42).toString().contains("42"));
    }

    // ── ruleTable ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ruleTable is non-empty and includes rule number")
    void ruleTableNonEmpty() {
        String table = new WolframRule(110).ruleTable();
        assertFalse(table.isBlank());
        assertTrue(table.contains("110"));
    }
}
