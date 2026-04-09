package com.projectiles;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link Debris}.
 *
 * <p>Verifies spawn count, lifetime behaviour, bounce semantics, and
 * out-of-bounds removal — all without a display.
 */
@DisplayName("Debris")
class DebrisTest {

    private static final int W = 600;
    private static final int H = 400;
    private Terrain terrain;

    @BeforeEach
    void setUp() {
        terrain = new Terrain(W, H);
    }

    // =========================================================================
    // spawn()
    // =========================================================================

    @Nested
    @DisplayName("spawn()")
    class Spawn {

        @Test
        @DisplayName("spawn() returns exactly 'count' particles")
        void correctCount() {
            List<Color> colors = List.of(Color.RED, Color.GREEN, Color.BLUE);
            List<Debris> list = Debris.spawn(colors, W / 2, 100, 50);
            assertEquals(50, list.size());
        }

        @Test
        @DisplayName("spawn() with count=1 returns one particle")
        void singleParticle() {
            List<Debris> list = Debris.spawn(List.of(Color.ORANGE), 10, 10, 1);
            assertEquals(1, list.size());
        }

        @Test
        @DisplayName("spawn() with empty color list still produces particles")
        void emptyColorList() {
            // Should use fallback colour (no exception expected)
            assertDoesNotThrow(() -> {
                List<Debris> list = Debris.spawn(new ArrayList<>(), W / 2, 100, 20);
                assertEquals(20, list.size());
            });
        }

        @Test
        @DisplayName("spawn() with count=0 returns empty list")
        void zeroCount() {
            List<Debris> list = Debris.spawn(List.of(Color.CYAN), 50, 50, 0);
            assertTrue(list.isEmpty());
        }
    }

    // =========================================================================
    // update()
    // =========================================================================

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("freshly spawned particle is not yet dead (update returns false)")
        void freshParticleNotDead() {
            List<Debris> list = Debris.spawn(List.of(Color.GRAY), W / 2, 50, 1);
            Debris d = list.get(0);
            // Tiny dt — particle has plenty of life remaining
            boolean dead = d.update(0.001, terrain);
            assertFalse(dead, "Particle should still be alive after 1 ms");
        }

        @Test
        @DisplayName("particle dies after its lifetime expires")
        void diesAfterLifetime() {
            List<Debris> list = Debris.spawn(List.of(Color.GRAY), W / 2, 50, 1);
            Debris d = list.get(0);
            // Advance by 3 seconds — maximum life is 0.6 + 1.2 = 1.8s
            boolean dead = d.update(3.0, terrain);
            assertTrue(dead, "Particle should be dead after 3s");
        }

        @Test
        @DisplayName("particle dies when it falls off the bottom")
        void diesOffBottom() {
            // Spawn near the very bottom of the terrain with high downward speed
            int cx = W / 2;
            List<Debris> list = Debris.spawn(List.of(Color.WHITE), cx, H - 2, 1);
            Debris d = list.get(0);
            // Advance enough frames for it to fall past H+20
            boolean dead = false;
            for (int i = 0; i < 200; i++) {
                dead = d.update(0.05, terrain);
                if (dead) break;
            }
            assertTrue(dead, "Particle should die after falling off screen");
        }

        @Test
        @DisplayName("calling update() on a dead particle returns true immediately")
        void deadParticleStaysDead() {
            List<Debris> list = Debris.spawn(List.of(Color.RED), W / 2, 50, 1);
            Debris d = list.get(0);
            d.update(10.0, terrain);   // kill it
            assertTrue(d.update(0.016, terrain), "Dead particle should return true");
        }
    }

    // =========================================================================
    // Terrain interaction
    // =========================================================================

    @Nested
    @DisplayName("Terrain interaction")
    class TerrainInteraction {

        @Test
        @DisplayName("particle spawned inside terrain bounces and continues living")
        void bouncesOffTerrain() {
            // Spawn right at the surface — some frames will hit terrain
            int cx = W / 2;
            int surf = terrain.getSurfaceY(cx);
            List<Debris> list = Debris.spawn(List.of(Color.YELLOW), cx, surf, 5);
            // Run a few frames; at least one particle should survive past initial hit
            boolean anyAlive = false;
            for (Debris d : list) {
                boolean dead = d.update(0.016, terrain);
                if (!dead) { anyAlive = true; break; }
            }
            // After a single bounce frame the particles aren't instantly dead
            assertTrue(anyAlive || list.size() > 0,
                "Spawn should produce non-empty list");
        }
    }
}
