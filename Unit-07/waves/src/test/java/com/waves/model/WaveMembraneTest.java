package com.waves.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WaveMembrane}.
 */
@DisplayName("WaveMembrane")
class WaveMembraneTest {

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("initial state")
    class InitialState {

        @Test
        @DisplayName("zero initial displacement stays zero after steps")
        void zeroInitialStaysZero() {
            WaveMembrane wm = new WaveMembrane();
            for (int i = 0; i < 50; i++) wm.step(200.0, 0.0);
            for (int r = 0; r < WaveMembrane.ROWS; r++) {
                for (int c = 0; c < WaveMembrane.COLS; c++) {
                    assertEquals(0.0, wm.getDisplacement(r, c), 1e-15,
                            "cell [" + r + "][" + c + "] should remain zero");
                }
            }
        }

        @Test
        @DisplayName("grid dimensions are correct")
        void gridSizeCorrect() {
            WaveMembrane wm = new WaveMembrane();
            assertEquals(WaveMembrane.ROWS, wm.getRows());
            assertEquals(WaveMembrane.COLS, wm.getCols());
        }
    }

    // -------------------------------------------------------------------------
    // Poke
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("poke")
    class Poke {

        @Test
        @DisplayName("poke creates nonzero displacement at centre")
        void pokeCreatesDisplacement() {
            WaveMembrane wm = new WaveMembrane();
            int cx = WaveMembrane.COLS / 2;
            int cy = WaveMembrane.ROWS / 2;
            wm.poke(cx, cy, 1.0, 8.0);
            double centre = wm.getDisplacement(cy, cx);
            assertTrue(centre > 0.5, "centre displacement should be near amplitude 1.0, was " + centre);
        }

        @Test
        @DisplayName("boundary remains zero after poke")
        void boundaryZeroAfterPoke() {
            WaveMembrane wm = new WaveMembrane();
            wm.poke(WaveMembrane.COLS / 2, WaveMembrane.ROWS / 2, 1.0, 8.0);

            // Top and bottom rows
            for (int c = 0; c < WaveMembrane.COLS; c++) {
                assertEquals(0.0, wm.getDisplacement(0, c),                    1e-15, "top row");
                assertEquals(0.0, wm.getDisplacement(WaveMembrane.ROWS - 1, c), 1e-15, "bottom row");
            }
            // Left and right columns
            for (int r = 0; r < WaveMembrane.ROWS; r++) {
                assertEquals(0.0, wm.getDisplacement(r, 0),                    1e-15, "left col");
                assertEquals(0.0, wm.getDisplacement(r, WaveMembrane.COLS - 1), 1e-15, "right col");
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
        @DisplayName("boundary stays zero throughout simulation")
        void boundaryStaysZeroDuringSimulation() {
            WaveMembrane wm = new WaveMembrane();
            wm.poke(WaveMembrane.COLS / 3, WaveMembrane.ROWS / 3, 1.0, 6.0);
            for (int step = 0; step < 100; step++) {
                wm.step(200.0, 0.0);
                for (int c = 0; c < WaveMembrane.COLS; c++) {
                    assertEquals(0.0, wm.getDisplacement(0, c), 1e-14, "top row step " + step);
                }
                for (int r = 0; r < WaveMembrane.ROWS; r++) {
                    assertEquals(0.0, wm.getDisplacement(r, 0), 1e-14, "left col step " + step);
                }
            }
        }

        @Test
        @DisplayName("reset clears all displacement to zero")
        void resetClearsDisplacement() {
            WaveMembrane wm = new WaveMembrane();
            wm.poke(WaveMembrane.COLS / 2, WaveMembrane.ROWS / 2, 1.0, 8.0);
            for (int i = 0; i < 20; i++) wm.step(200.0, 0.0);
            wm.reset();
            for (int r = 0; r < WaveMembrane.ROWS; r++) {
                for (int c = 0; c < WaveMembrane.COLS; c++) {
                    assertEquals(0.0, wm.getDisplacement(r, c), 1e-15,
                            "cell [" + r + "][" + c + "] should be zero after reset");
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // renderToImage
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("renderToImage")
    class Render {

        @Test
        @DisplayName("renders an image with correct dimensions")
        void imageDimensions() {
            WaveMembrane wm = new WaveMembrane();
            var img = wm.renderToImage(720, 480);
            assertNotNull(img);
            assertEquals(WaveMembrane.COLS, img.getWidth());
            assertEquals(WaveMembrane.ROWS, img.getHeight());
        }

        @Test
        @DisplayName("flat membrane renders to all-black image")
        void flatMembraneIsBlack() {
            WaveMembrane wm = new WaveMembrane();
            var img = wm.renderToImage(720, 480);
            // All pixels should be 0 (black)
            for (int r = 0; r < WaveMembrane.ROWS; r++) {
                for (int c = 0; c < WaveMembrane.COLS; c++) {
                    assertEquals(0, img.getRGB(c, r) & 0xFFFFFF,
                            "pixel at [" + r + "][" + c + "] should be black");
                }
            }
        }

        @Test
        @DisplayName("positive displacement renders with red component")
        void positiveDisplacementIsRed() {
            WaveMembrane wm = new WaveMembrane();
            wm.poke(WaveMembrane.COLS / 2, WaveMembrane.ROWS / 2, 1.0, 8.0);
            var img = wm.renderToImage(720, 480);
            int cx = WaveMembrane.COLS / 2;
            int cy = WaveMembrane.ROWS / 2;
            int rgb = img.getRGB(cx, cy);
            int red   = (rgb >> 16) & 0xFF;
            int blue  = rgb & 0xFF;
            assertTrue(red > 0,   "centre pixel should have red component for positive displacement");
            assertEquals(0, blue, "centre pixel blue should be zero for positive displacement");
        }
    }
}
