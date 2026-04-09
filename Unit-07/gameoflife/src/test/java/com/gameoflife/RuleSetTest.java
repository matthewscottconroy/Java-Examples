package com.gameoflife;

import com.gameoflife.gol.RuleSet;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RuleSet}.
 *
 * <p>Verifies B/S notation parsing, the birth/survival predicates, and
 * the built-in preset constants.
 */
@DisplayName("RuleSet")
class RuleSetTest {

    // ── Parsing ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("fromNotation parsing")
    class Parsing {

        @Test
        @DisplayName("Conway B3/S23 parses correctly")
        void conwayParse() {
            RuleSet r = RuleSet.fromNotation("B3/S23");
            assertTrue(r.born(3));
            assertTrue(r.survives(2));
            assertTrue(r.survives(3));
            assertFalse(r.born(2));
            assertFalse(r.survives(1));
        }

        @Test
        @DisplayName("Seeds B2/S parses — survival set empty")
        void seedsParse() {
            RuleSet r = RuleSet.fromNotation("B2/S");
            assertTrue(r.born(2));
            assertFalse(r.survives(0));
            assertFalse(r.survives(2));
        }

        @Test
        @DisplayName("notation is case-insensitive")
        void caseInsensitive() {
            RuleSet lower = RuleSet.fromNotation("b3/s23");
            RuleSet upper = RuleSet.fromNotation("B3/S23");
            assertEquals(upper, lower);
        }

        @Test
        @DisplayName("invalid notation throws IllegalArgumentException")
        void invalidNotation() {
            assertThrows(IllegalArgumentException.class,
                    () -> RuleSet.fromNotation("3/23"));
        }

        @Test
        @DisplayName("missing slash throws IllegalArgumentException")
        void missingSlash() {
            assertThrows(IllegalArgumentException.class,
                    () -> RuleSet.fromNotation("B3S23"));
        }

        @Test
        @DisplayName("invalid digit throws IllegalArgumentException")
        void invalidDigit() {
            assertThrows(IllegalArgumentException.class,
                    () -> RuleSet.fromNotation("B9/S23"));
        }
    }

    // ── Born predicate ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("born predicate")
    class BornPredicate {

        @Test
        @DisplayName("Conway: born only at 3 neighbors")
        void conwayBorn() {
            RuleSet r = RuleSet.CONWAY;
            for (int n = 0; n <= 8; n++) {
                if (n == 3) assertTrue(r.born(n), "should be born at 3");
                else        assertFalse(r.born(n), "should not be born at " + n);
            }
        }

        @Test
        @DisplayName("Replicator: born at odd neighbor counts")
        void replicatorBorn() {
            RuleSet r = RuleSet.REPLICATOR;
            assertTrue(r.born(1));
            assertTrue(r.born(3));
            assertTrue(r.born(5));
            assertTrue(r.born(7));
            assertFalse(r.born(2));
            assertFalse(r.born(4));
        }
    }

    // ── Survives predicate ────────────────────────────────────────────────────

    @Nested
    @DisplayName("survives predicate")
    class SurvivesPredicate {

        @Test
        @DisplayName("Conway: survives at 2 or 3")
        void conwaySurvives() {
            RuleSet r = RuleSet.CONWAY;
            assertTrue(r.survives(2));
            assertTrue(r.survives(3));
            assertFalse(r.survives(0));
            assertFalse(r.survives(1));
            assertFalse(r.survives(4));
        }

        @Test
        @DisplayName("Life Without Death: cell always survives once alive")
        void lifeWithoutDeathSurvives() {
            RuleSet r = RuleSet.LIFE_WITHOUT_DEATH;
            for (int n = 0; n <= 8; n++) {
                assertTrue(r.survives(n), "should survive at " + n);
            }
        }

        @Test
        @DisplayName("Seeds: no cell ever survives")
        void seedsNeverSurvives() {
            RuleSet r = RuleSet.SEEDS;
            for (int n = 0; n <= 8; n++) {
                assertFalse(r.survives(n), "seeds: should not survive at " + n);
            }
        }
    }

    // ── Notation round-trip ───────────────────────────────────────────────────

    @Nested
    @DisplayName("toNotation round-trip")
    class Notation {

        @Test
        @DisplayName("Conway notation round-trips correctly")
        void conwayRoundTrip() {
            assertEquals("B3/S23", RuleSet.CONWAY.toNotation());
        }

        @Test
        @DisplayName("parsed rule produces canonical notation")
        void parsedRuleNotation() {
            // B36/S23 should produce "B36/S23" (digits sorted)
            RuleSet r = RuleSet.fromNotation("B36/S23");
            assertEquals("B36/S23", r.toNotation());
        }
    }

    // ── Equality ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("same notation produces equal rules")
        void sameNotationEqual() {
            RuleSet a = RuleSet.fromNotation("B3/S23");
            RuleSet b = RuleSet.fromNotation("B3/S23");
            assertEquals(a, b);
        }

        @Test
        @DisplayName("different notation produces unequal rules")
        void differentNotationUnequal() {
            assertNotEquals(RuleSet.CONWAY, RuleSet.HIGH_LIFE);
        }

        @Test
        @DisplayName("equal rules have equal hash codes")
        void equalHashCodes() {
            RuleSet a = RuleSet.fromNotation("B3/S23");
            RuleSet b = RuleSet.fromNotation("B3/S23");
            assertEquals(a.hashCode(), b.hashCode());
        }
    }

    // ── Presets ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("preset constants")
    class Presets {

        @Test
        @DisplayName("CONWAY equals B3/S23")
        void conwayPreset() {
            assertEquals(RuleSet.fromNotation("B3/S23"), RuleSet.CONWAY);
        }

        @Test
        @DisplayName("HIGH_LIFE equals B36/S23")
        void highLifePreset() {
            assertEquals(RuleSet.fromNotation("B36/S23"), RuleSet.HIGH_LIFE);
        }

        @Test
        @DisplayName("PRESETS list is non-empty")
        void presetsNonEmpty() {
            assertFalse(RuleSet.PRESETS.isEmpty());
        }

        @Test
        @DisplayName("all PRESETS have non-null names")
        void presetsHaveNames() {
            for (RuleSet r : RuleSet.PRESETS) {
                assertNotNull(r.getName());
                assertFalse(r.getName().isBlank());
            }
        }
    }
}
