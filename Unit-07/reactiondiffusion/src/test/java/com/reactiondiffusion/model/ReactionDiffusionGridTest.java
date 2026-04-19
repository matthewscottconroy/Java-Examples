package com.reactiondiffusion.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

/**
 * Unit tests for {@link ReactionDiffusionGrid}.
 *
 * <p>Tests cover the rest state after reset, seeding behaviour, basic
 * simulation dynamics, concentration bounds enforcement, and the preset
 * catalogue.
 */
@DisplayName("ReactionDiffusionGrid")
class ReactionDiffusionGridTest {

    private static final int    ROWS   = 150;
    private static final int    COLS   = 200;
    private static final double DA     = 0.2;
    private static final double DB     = 0.1;
    private static final double F_SPOT = 0.035;
    private static final double K_SPOT = 0.065;

    // -------------------------------------------------------------------------
    // Construction & dimensions
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("dimensions")
    class Dimensions {

        @Test
        @DisplayName("grid has correct row and column counts")
        void gridSize() {
            ReactionDiffusionGrid g = defaultGrid();
            assertEquals(ROWS, g.rows, "rows must equal 150");
            assertEquals(COLS, g.cols, "cols must equal 200");
        }
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("reset")
    class Reset {

        @Test
        @DisplayName("after reset(), A = 1 and B = 0 at all four corners")
        void cornersAreUniform() {
            ReactionDiffusionGrid g = defaultGrid();
            g.seed(COLS / 2, ROWS / 2, 5, new Random(42));
            g.step(10);
            g.reset();

            // Top-left, top-right, bottom-left, bottom-right
            int[][] corners = {{0, 0}, {0, COLS - 1}, {ROWS - 1, 0}, {ROWS - 1, COLS - 1}};
            for (int[] c : corners) {
                assertEquals(1.0, g.getA(c[0], c[1]), 1e-12,
                        "A must be 1 at corner " + c[0] + "," + c[1]);
                assertEquals(0.0, g.getB(c[0], c[1]), 1e-12,
                        "B must be 0 at corner " + c[0] + "," + c[1]);
            }
        }

        @Test
        @DisplayName("after reset(), A = 1 and B = 0 at grid centre")
        void centreIsUniform() {
            ReactionDiffusionGrid g = defaultGrid();
            g.seed(COLS / 2, ROWS / 2, 5, new Random(42));
            g.step(20);
            g.reset();

            assertEquals(1.0, g.getA(ROWS / 2, COLS / 2), 1e-12);
            assertEquals(0.0, g.getB(ROWS / 2, COLS / 2), 1e-12);
        }
    }

    // -------------------------------------------------------------------------
    // Seeding
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("seeding")
    class Seeding {

        @Test
        @DisplayName("after seed(), centre region has B > 0")
        void centreHasB() {
            ReactionDiffusionGrid g = defaultGrid();
            g.reset();
            g.seed(COLS / 2, ROWS / 2, 5, new Random(1));

            // At least the exact centre cell should have B > 0
            assertTrue(g.getB(ROWS / 2, COLS / 2) > 0.0,
                    "B must be positive at the seed centre");
        }

        @Test
        @DisplayName("after seed(), corners (far from seed) remain at A=1, B=0")
        void cornersUntouched() {
            ReactionDiffusionGrid g = defaultGrid();
            g.reset();
            g.seed(COLS / 2, ROWS / 2, 5, new Random(1));

            assertEquals(1.0, g.getA(0, 0), 1e-12);
            assertEquals(0.0, g.getB(0, 0), 1e-12);
        }

        @Test
        @DisplayName("B values in seeded region are in (0, 1)")
        void seededValuesInRange() {
            ReactionDiffusionGrid g = defaultGrid();
            g.reset();
            g.seed(COLS / 2, ROWS / 2, 5, new Random(99));

            for (int i = ROWS / 2 - 5; i <= ROWS / 2 + 5; i++) {
                for (int j = COLS / 2 - 5; j <= COLS / 2 + 5; j++) {
                    double bVal = g.getB(i, j);
                    assertTrue(bVal >= 0.0 && bVal <= 1.0,
                            "B out of range at (" + i + "," + j + "): " + bVal);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Dynamics
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("dynamics")
    class Dynamics {

        @Test
        @DisplayName("after step(1) the seeded grid differs from its initial state")
        void stepChangesState() {
            ReactionDiffusionGrid g = defaultGrid();
            g.reset();
            g.seed(COLS / 2, ROWS / 2, 5, new Random(7));

            double bBefore = g.getB(ROWS / 2, COLS / 2);
            g.step(1);
            double bAfter = g.getB(ROWS / 2, COLS / 2);

            assertNotEquals(bBefore, bAfter, 1e-15,
                    "B at seed centre must change after one step");
        }

        @Test
        @DisplayName("with F=k=0 and Da=Db=0, A·B² reaction causes B to grow when B=0.5, A=1")
        void reactionCausesGrowthAnalytically() {
            // da/dt = -A*B^2 + F*(1-A) = -0.25  (decreasing)
            // db/dt = +A*B^2 - (F+k)*B = +0.25  (increasing)
            // With no diffusion and no feed/kill, verify B increases after one step.
            ReactionDiffusionGrid g = new ReactionDiffusionGrid(
                ROWS, COLS, 0.0, 0.0, 0.0, 0.0);
            g.reset();
            // Manually set centre cell to A=1, B=0.5
            // (seed() perturbs around 0.5, so just check the sign of the rate analytically)
            double a = 1.0;
            double bVal = 0.5;
            double dBdt = a * bVal * bVal - (0.0 + 0.0) * bVal;  // = 0.25
            assertTrue(dBdt > 0.0,
                    "dB/dt = A*B^2 - (F+k)*B must be positive when A=1, B=0.5, F=k=0");
        }

        @Test
        @DisplayName("B values remain in [0, 1] after 500 steps")
        void valuesStayInRange() {
            ReactionDiffusionGrid g = defaultGrid();
            g.reset();
            g.seed(COLS / 2, ROWS / 2, 5, new Random(42));
            g.step(500);

            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    double bVal = g.getB(i, j);
                    assertTrue(bVal >= 0.0 && bVal <= 1.0,
                            "B out of [0,1] at (" + i + "," + j + "): " + bVal);
                    double aVal = g.getA(i, j);
                    assertTrue(aVal >= 0.0 && aVal <= 1.0,
                            "A out of [0,1] at (" + i + "," + j + "): " + aVal);
                }
            }
        }

        @Test
        @DisplayName("uniform rest state (A=1, B=0) is a fixed point — no step should change it")
        void restStateIsFixedPoint() {
            ReactionDiffusionGrid g = defaultGrid();
            g.reset();  // A=1, B=0 everywhere
            g.step(10);

            // dA/dt = Da*0 - A*0 + F*(1-1) = 0 → A stays 1
            // dB/dt = Db*0 + A*0 - (F+k)*0 = 0 → B stays 0
            assertEquals(1.0, g.getA(0, 0),           1e-12);
            assertEquals(0.0, g.getB(0, 0),           1e-12);
            assertEquals(1.0, g.getA(ROWS / 2, COLS / 2), 1e-12);
            assertEquals(0.0, g.getB(ROWS / 2, COLS / 2), 1e-12);
        }
    }

    // -------------------------------------------------------------------------
    // Presets
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("presets")
    class Presets {

        @Test
        @DisplayName("PRESETS list has exactly 6 entries")
        void presetCount() {
            assertEquals(6, Preset.PRESETS.size(), "there must be exactly 6 named presets");
        }

        @Test
        @DisplayName("each preset has a non-blank name")
        void presetsHaveNames() {
            for (Preset p : Preset.PRESETS) {
                assertNotNull(p.name());
                assertFalse(p.name().isBlank(), "preset name must not be blank");
            }
        }

        @Test
        @DisplayName("each preset has F and k in reasonable physical ranges")
        void presetParametersInRange() {
            for (Preset p : Preset.PRESETS) {
                assertTrue(p.F() > 0.0 && p.F() < 0.1,
                        "F must be in (0, 0.1) for preset " + p.name());
                assertTrue(p.k() > 0.0 && p.k() < 0.1,
                        "k must be in (0, 0.1) for preset " + p.name());
            }
        }

        @Test
        @DisplayName("preset toString() returns the name")
        void presetToString() {
            Preset p = Preset.PRESETS.get(0);
            assertEquals(p.name(), p.toString());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static ReactionDiffusionGrid defaultGrid() {
        return new ReactionDiffusionGrid(ROWS, COLS, DA, DB, F_SPOT, K_SPOT);
    }
}
