package com.buffon.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BuffonExperiment}.
 */
@DisplayName("BuffonExperiment")
class BuffonExperimentTest {

    private static final double D = 100.0;  // line spacing
    private static final double L = 70.0;   // needle length

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("starts with zero drops and crossings")
        void startEmpty() {
            BuffonExperiment e = new BuffonExperiment(D, L);
            assertEquals(0L, e.getTotalDrops());
            assertEquals(0L, e.getCrossings());
        }

        @Test
        @DisplayName("estimatePi returns NaN when no crossings yet")
        void nanWhenNoCrossings() {
            assertTrue(Double.isNaN(new BuffonExperiment(D, L).estimatePi()));
        }

        @Test
        @DisplayName("stores line spacing and needle length")
        void storesGeometry() {
            BuffonExperiment e = new BuffonExperiment(D, L);
            assertEquals(D, e.getLineSpacing(),  1e-12);
            assertEquals(L, e.getNeedleLength(), 1e-12);
        }
    }

    // -------------------------------------------------------------------------
    // Crossing detection — deterministic cases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("crossing detection")
    class CrossingDetection {

        @Test
        @DisplayName("horizontal needle (angle=0) never crosses horizontal lines")
        void horizontalNeverCrosses() {
            BuffonExperiment e = new BuffonExperiment(D, L);
            // sin(0) = 0 → halfSpan = 0 → both endpoints at same y
            assertFalse(e.crosses(50.0, 0.0));
            assertFalse(e.crosses(99.0, 0.0));
        }

        @Test
        @DisplayName("vertical needle centred in a band does not cross when L < d")
        void verticalCentredInBandNoCross() {
            // L=70, d=100; centre at y=50 (midpoint of [0,100])
            // halfSpan = 35; yMin=15, yMax=85 → both in [0,100) → no crossing
            BuffonExperiment e = new BuffonExperiment(D, L);
            assertFalse(e.crosses(50.0, Math.PI / 2));
        }

        @Test
        @DisplayName("vertical needle near a line crosses")
        void verticalNearLineCrosses() {
            // L=70, d=100; centre at y=90
            // halfSpan=35; yMin=55, yMax=125 → floor(55/100)=0, floor(125/100)=1 → crosses
            BuffonExperiment e = new BuffonExperiment(D, L);
            assertTrue(e.crosses(90.0, Math.PI / 2));
        }

        @Test
        @DisplayName("vertical needle exactly on a line crosses")
        void verticalExactlyOnLineCrosses() {
            // centre at y=100 exactly; halfSpan=35; yMin=65, yMax=135 → crosses
            BuffonExperiment e = new BuffonExperiment(D, L);
            assertTrue(e.crosses(100.0, Math.PI / 2));
        }

        @Test
        @DisplayName("long needle (L > d) can cross multiple lines")
        void longNeedleCrossesDetected() {
            // L = 2d: vertical, centre at y = d/2
            // halfSpan = d; yMin = -d/2, yMax = 3d/2 → multiple lines crossed
            BuffonExperiment e = new BuffonExperiment(D, 2 * D);
            assertTrue(e.crosses(D / 2, Math.PI / 2));
        }

        @Test
        @DisplayName("angle π/6 (30°): crossing depends on position")
        void angledNeedlePosition() {
            // halfSpan = (L/2)|sin(30°)| = 35 * 0.5 = 17.5
            // centre y=95: yMin=77.5, yMax=112.5 → floor(77.5/100)=0, floor(112.5/100)=1 → crosses
            BuffonExperiment e = new BuffonExperiment(D, L);
            assertTrue(e.crosses(95.0, Math.PI / 6));

            // centre y=50: yMin=32.5, yMax=67.5 → both in [0,100) → no crossing
            assertFalse(e.crosses(50.0, Math.PI / 6));
        }
    }

    // -------------------------------------------------------------------------
    // Pi estimation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("π estimation")
    class PiEstimation {

        @Test
        @DisplayName("formula: π̂ = 2LN / (dC)")
        void formulaCorrect() {
            // Construct an experiment then inject exact crossing count via drop
            // Use a seeded RNG for reproducibility; just check formula consistency.
            BuffonExperiment e = new BuffonExperiment(D, L, 42L);
            e.drop(10_000, 700, 500);
            long N = e.getTotalDrops();
            long C = e.getCrossings();
            double expected = (2.0 * L * N) / (D * C);
            assertEquals(expected, e.estimatePi(), 1e-12);
        }

        @Test
        @DisplayName("theoretical crossing probability = 2L/(πd)")
        void theoreticalProbability() {
            BuffonExperiment e = new BuffonExperiment(D, L);
            double expected = (2.0 * L) / (Math.PI * D);
            assertEquals(expected, e.theoreticalCrossingProbability(), 1e-12);
        }

        @Test
        @DisplayName("observed probability converges toward theoretical over many drops")
        void observedConvergesToTheoretical() {
            BuffonExperiment e = new BuffonExperiment(D, L, 1234L);
            e.drop(500_000, 700, 500);
            double obs   = e.observedCrossingProbability();
            double theo  = e.theoreticalCrossingProbability();
            assertEquals(theo, obs, 0.005,
                    "observed P(cross) should be within 0.5% of theoretical for 500k drops");
        }

        @Test
        @DisplayName("π estimate converges toward π over many drops")
        void piConverges() {
            BuffonExperiment e = new BuffonExperiment(D, L, 7777L);
            e.drop(500_000, 700, 500);
            double piHat = e.estimatePi();
            assertEquals(Math.PI, piHat, 0.02,
                    "π estimate should be within 0.02 of π for 500k drops");
        }

        @Test
        @DisplayName("long-needle flag is set when L > d")
        void longNeedleFlag() {
            assertFalse(new BuffonExperiment(D, D      ).isLongNeedle());
            assertFalse(new BuffonExperiment(D, D - 1  ).isLongNeedle());
            assertTrue( new BuffonExperiment(D, D + 0.1).isLongNeedle());
        }
    }

    // -------------------------------------------------------------------------
    // Statistics and history
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("statistics")
    class Statistics {

        @Test
        @DisplayName("crossings ≤ totalDrops at all times")
        void crossingsNeverExceedDrops() {
            BuffonExperiment e = new BuffonExperiment(D, L, 999L);
            for (int i = 0; i < 10; i++) {
                e.drop(1000, 700, 500);
                assertTrue(e.getCrossings() <= e.getTotalDrops());
            }
        }

        @Test
        @DisplayName("display buffer is bounded by MAX_DISPLAY")
        void displayBufferBounded() {
            BuffonExperiment e = new BuffonExperiment(D, L, 1L);
            e.drop(BuffonExperiment.MAX_DISPLAY + 500, 700, 500);
            assertTrue(e.getDisplayNeedles().size() <= BuffonExperiment.MAX_DISPLAY);
        }

        @Test
        @DisplayName("pi history is bounded by MAX_HISTORY")
        void historyBounded() {
            BuffonExperiment e = new BuffonExperiment(D, L, 1L);
            e.drop(200_000, 700, 500);
            assertTrue(e.getPiHistory().size() <= BuffonExperiment.MAX_HISTORY);
        }

        @Test
        @DisplayName("getPiHistory is unmodifiable")
        void historyIsUnmodifiable() {
            BuffonExperiment e = new BuffonExperiment(D, L, 1L);
            e.drop(100, 700, 500);
            assertThrows(UnsupportedOperationException.class,
                    () -> e.getPiHistory().add(3.14));
        }
    }

    // -------------------------------------------------------------------------
    // Reset / reconfigure
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("reset and reconfigure")
    class ResetAndReconfigure {

        @Test
        @DisplayName("reset clears all state")
        void resetClearsState() {
            BuffonExperiment e = new BuffonExperiment(D, L, 1L);
            e.drop(1000, 700, 500);
            e.reset();
            assertEquals(0L, e.getTotalDrops());
            assertEquals(0L, e.getCrossings());
            assertTrue(e.getDisplayNeedles().isEmpty());
            assertTrue(e.getPiHistory().isEmpty());
        }

        @Test
        @DisplayName("setLineSpacing resets the experiment")
        void setLineSpacingResets() {
            BuffonExperiment e = new BuffonExperiment(D, L, 1L);
            e.drop(500, 700, 500);
            e.setLineSpacing(80.0);
            assertEquals(80.0, e.getLineSpacing(), 1e-12);
            assertEquals(0L, e.getTotalDrops());
        }

        @Test
        @DisplayName("setNeedleLength resets the experiment")
        void setNeedleLengthResets() {
            BuffonExperiment e = new BuffonExperiment(D, L, 1L);
            e.drop(500, 700, 500);
            e.setNeedleLength(50.0);
            assertEquals(50.0, e.getNeedleLength(), 1e-12);
            assertEquals(0L, e.getTotalDrops());
        }
    }
}
