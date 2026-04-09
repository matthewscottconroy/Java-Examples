package com.projectiles;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.util.List;

/**
 * Unit tests for {@link Terrain}.
 *
 * <p>These tests avoid rendering assertions (pixel colours vary by RNG) and
 * instead focus on the contract-level behaviour: solid/air classification,
 * explosion carving, surface-Y bounds, and out-of-bounds safety.
 */
@DisplayName("Terrain")
class TerrainTest {

    private static final int W = 400;
    private static final int H = 300;

    private Terrain terrain;

    @BeforeEach
    void setUp() {
        terrain = new Terrain(W, H);
    }

    // =========================================================================
    // Dimensions
    // =========================================================================

    @Nested
    @DisplayName("Dimensions")
    class Dimensions {

        @Test
        @DisplayName("getWidth() returns constructor width")
        void width() {
            assertEquals(W, terrain.getWidth());
        }

        @Test
        @DisplayName("getHeight() returns constructor height")
        void height() {
            assertEquals(H, terrain.getHeight());
        }
    }

    // =========================================================================
    // isSolid() — out-of-bounds always returns false
    // =========================================================================

    @Nested
    @DisplayName("isSolid() — bounds")
    class IsSolidBounds {

        @Test
        @DisplayName("negative x returns false")
        void negativeX() {
            assertFalse(terrain.isSolid(-1, H / 2));
        }

        @Test
        @DisplayName("negative y returns false")
        void negativeY() {
            assertFalse(terrain.isSolid(W / 2, -1));
        }

        @Test
        @DisplayName("x >= width returns false")
        void xAtWidth() {
            assertFalse(terrain.isSolid(W, H / 2));
        }

        @Test
        @DisplayName("y >= height returns false")
        void yAtHeight() {
            assertFalse(terrain.isSolid(W / 2, H));
        }
    }

    // =========================================================================
    // isSolid() — terrain structure
    // =========================================================================

    @Nested
    @DisplayName("isSolid() — terrain structure")
    class IsSolidStructure {

        @Test
        @DisplayName("bottom row is always solid")
        void bottomRowSolid() {
            // The very bottom of the terrain (y = H-1) is always filled
            for (int x = 0; x < W; x += 20) {
                assertTrue(terrain.isSolid(x, H - 1),
                    "Expected solid at x=" + x + ", y=" + (H - 1));
            }
        }

        @Test
        @DisplayName("top row (y=0) is always air")
        void topRowAir() {
            // The sine-wave surface is clamped to at least y=80, so y=0 is always air
            for (int x = 0; x < W; x += 20) {
                assertFalse(terrain.isSolid(x, 0),
                    "Expected air at x=" + x + ", y=0");
            }
        }

        @Test
        @DisplayName("pixel just below surfaceY is solid")
        void surfacePixelIsSolid() {
            int x = W / 2;
            int surf = terrain.getSurfaceY(x);
            assertTrue(terrain.isSolid(x, surf),
                "Surface pixel at y=" + surf + " should be solid");
        }

        @Test
        @DisplayName("pixel just above surfaceY is air")
        void aboveSurfaceIsAir() {
            int x = W / 2;
            int surf = terrain.getSurfaceY(x);
            if (surf > 0) {
                assertFalse(terrain.isSolid(x, surf - 1),
                    "Pixel above surface at y=" + (surf - 1) + " should be air");
            }
        }
    }

    // =========================================================================
    // getSurfaceY()
    // =========================================================================

    @Nested
    @DisplayName("getSurfaceY()")
    class SurfaceY {

        @Test
        @DisplayName("surface Y is within valid bounds for every column")
        void inBounds() {
            for (int x = 0; x < W; x++) {
                int sy = terrain.getSurfaceY(x);
                assertTrue(sy >= 0 && sy < H,
                    "surfaceY[" + x + "]=" + sy + " out of range");
            }
        }

        @Test
        @DisplayName("getSurfaceY clamps x < 0 to 0")
        void clampsNegativeX() {
            assertEquals(terrain.getSurfaceY(0), terrain.getSurfaceY(-5));
        }

        @Test
        @DisplayName("getSurfaceY clamps x >= width to width-1")
        void clampsXAtWidth() {
            assertEquals(terrain.getSurfaceY(W - 1), terrain.getSurfaceY(W + 10));
        }
    }

    // =========================================================================
    // explode()
    // =========================================================================

    @Nested
    @DisplayName("explode()")
    class Explode {

        @Test
        @DisplayName("explode() returns a non-empty list when terrain is present")
        void returnsDestroyedColors() {
            int x = W / 2;
            int cy = terrain.getSurfaceY(x) + 10;   // well inside the ground
            List<Color> destroyed = terrain.explode(x, cy, 20);
            assertFalse(destroyed.isEmpty(),
                "explode() should destroy at least one pixel");
        }

        @Test
        @DisplayName("pixels inside explosion radius become air afterward")
        void centreBecomesAir() {
            int cx = W / 2;
            int cy = terrain.getSurfaceY(cx) + 15;
            terrain.explode(cx, cy, 12);
            // The centre of the explosion should now be air
            assertFalse(terrain.isSolid(cx, cy),
                "Centre of explosion should be air after explode()");
        }

        @Test
        @DisplayName("pixels far outside radius are unchanged")
        void farPixelsUntouched() {
            int cx = W / 2;
            int cy = H - 5;   // near the bottom
            // Record that the very bottom is solid before
            assertTrue(terrain.isSolid(W / 2, H - 1));
            // Explode at a radius that does NOT reach H-1 - (H-5) = 4 pixels below centre
            terrain.explode(cx, cy, 2);
            // The pixel 20 columns away should still be solid
            assertTrue(terrain.isSolid(W / 2 + 30, H - 1),
                "Distant pixel should remain solid");
        }

        @Test
        @DisplayName("explode() in mid-air (no solid pixels) returns empty list")
        void explodeInAir() {
            // y=0 is always air, so a tiny explosion there destroys nothing
            List<Color> destroyed = terrain.explode(W / 2, 0, 3);
            assertTrue(destroyed.isEmpty(),
                "Exploding in air should return no destroyed colors");
        }
    }
}
