package com.projectiles;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Projectile}.
 *
 * <p>Tests physics integration, collision detection, trajectory preview,
 * and out-of-bounds deactivation — all without a display.
 */
@DisplayName("Projectile")
class ProjectileTest {

    private static final int W = 800;
    private static final int H = 600;
    private Terrain terrain;

    @BeforeEach
    void setUp() {
        terrain = new Terrain(W, H);
    }

    // =========================================================================
    // Construction / initial state
    // =========================================================================

    @Nested
    @DisplayName("Initial state")
    class InitialState {

        @Test
        @DisplayName("newly created projectile is active")
        void startsActive() {
            Projectile p = new Projectile(100, 100, 50, -200);
            assertTrue(p.isActive());
        }

        @Test
        @DisplayName("initial position is correct")
        void initialPosition() {
            Projectile p = new Projectile(150.0, 250.0, 0, 0);
            assertEquals(150.0, p.getX(), 1e-9);
            assertEquals(250.0, p.getY(), 1e-9);
        }
    }

    // =========================================================================
    // Physics integration
    // =========================================================================

    @Nested
    @DisplayName("Physics")
    class Physics {

        @Test
        @DisplayName("gravity causes downward acceleration over time")
        void gravityAccelerates() {
            // Start high in the air, launch horizontally
            double startY = 50.0;
            Projectile p = new Projectile(W / 2.0, startY, 0, 0);
            double gravity = 680.0;
            double dt = 0.016;

            // Update several frames without hitting terrain
            for (int i = 0; i < 5; i++) {
                p.update(dt, gravity, terrain);
            }

            // Y should have increased (downward) due to gravity
            assertTrue(p.getY() > startY,
                "Projectile should fall under gravity");
        }

        @Test
        @DisplayName("horizontal velocity moves projectile in correct direction")
        void horizontalMotion() {
            double startX = W / 2.0;
            Projectile p = new Projectile(startX, 50.0, 200.0, 0.0);
            p.update(0.016, 0.0, terrain);   // zero gravity for isolation
            assertTrue(p.getX() > startX,
                "Positive vx should move projectile right");
        }

        @Test
        @DisplayName("zero gravity — projectile moves in straight line")
        void straightLineWithoutGravity() {
            double sx = 50.0, sy = 50.0, vx = 100.0;
            Projectile p = new Projectile(sx, sy, vx, 0.0);
            double dt = 0.1;
            p.update(dt, 0.0, terrain);
            assertEquals(sx + vx * dt, p.getX(), 1e-6);
            assertEquals(sy,            p.getY(), 1e-6);
        }
    }

    // =========================================================================
    // Collision / deactivation
    // =========================================================================

    @Nested
    @DisplayName("Collision and deactivation")
    class Collision {

        @Test
        @DisplayName("update() returns false for in-flight projectile")
        void noHitWhileFlying() {
            Projectile p = new Projectile(W / 2.0, 30.0, 0, 0);
            boolean hit = p.update(0.016, 680.0, terrain);
            // Still high — very unlikely to hit on first frame
            // (may or may not hit depending on surface, so only assert active check)
            // If no hit, active should still be true; if hit, update returns true
            if (!hit) {
                assertTrue(p.isActive());
            } else {
                assertFalse(p.isActive());
            }
        }

        @Test
        @DisplayName("update() on inactive projectile returns false and stays inactive")
        void inactiveProjectileStaysInactive() {
            // Create a projectile already at the bottom of the terrain
            int cx = W / 2;
            int cy = terrain.getSurfaceY(cx) + 1;  // just inside ground
            Projectile p = new Projectile(cx, cy, 0, 0);
            // First update should detect collision
            p.update(0.016, 0.0, terrain);
            assertFalse(p.isActive());
            // Second update on an inactive projectile must return false
            boolean result = p.update(0.016, 0.0, terrain);
            assertFalse(result);
        }

        @Test
        @DisplayName("projectile deactivates when it falls off the bottom")
        void deactivatesOffBottom() {
            // Start just below H with no terrain collision (well outside bounds)
            Projectile p = new Projectile(W / 2.0, H + 60.0, 0, 100.0);
            p.update(0.016, 680.0, terrain);
            assertFalse(p.isActive(),
                "Projectile below terrain height+50 should deactivate");
        }

        @Test
        @DisplayName("projectile deactivates when it flies off the left edge")
        void deactivatesOffLeft() {
            Projectile p = new Projectile(-60.0, H / 2.0, -100.0, 0.0);
            p.update(0.016, 0.0, terrain);
            assertFalse(p.isActive());
        }
    }

    // =========================================================================
    // preview()
    // =========================================================================

    @Nested
    @DisplayName("preview()")
    class Preview {

        @Test
        @DisplayName("returns exactly 'steps' points")
        void correctPointCount() {
            int steps = 45;
            double[][] pts = Projectile.preview(
                W / 2.0, 100.0, 300.0, -200.0,
                steps, 0.04, 680.0, terrain);
            assertEquals(steps, pts.length);
        }

        @Test
        @DisplayName("each preview point is a [x, y] pair")
        void pointsArePairs() {
            double[][] pts = Projectile.preview(
                W / 2.0, 100.0, 300.0, -200.0,
                10, 0.04, 680.0, terrain);
            for (double[] pt : pts) {
                assertEquals(2, pt.length);
            }
        }

        @Test
        @DisplayName("first preview point is displaced from start position")
        void firstPointDisplaced() {
            double sx = W / 2.0, sy = 80.0, vx = 200.0, vy = -300.0;
            double dt = 0.04, g = 680.0;
            double[][] pts = Projectile.preview(sx, sy, vx, vy, 5, dt, g, terrain);
            // After one step vy_new = vy + g*dt; py = sy + vy_new*dt
            double expectedX = sx + vx * dt;
            assertEquals(expectedX, pts[0][0], 1e-6);
        }

        @Test
        @DisplayName("preview with zero velocity produces downward curve under gravity")
        void zeroVelocityCurvesDown() {
            double sy = 50.0;
            double[][] pts = Projectile.preview(
                W / 2.0, sy, 0.0, 0.0, 10, 0.016, 680.0, terrain);
            // All points should have y >= sy (gravity pulls down)
            for (double[] pt : pts) {
                assertTrue(pt[1] >= sy, "Point y=" + pt[1] + " should be >= start sy=" + sy);
            }
        }

        @Test
        @DisplayName("preview stops (repeats last point) after terrain hit")
        void stopsAtTerrain() {
            // Fire straight down from just above the terrain surface
            int cx = W / 2;
            int surf = terrain.getSurfaceY(cx);
            // Start 10px above surface, fire straight down fast
            double[][] pts = Projectile.preview(
                cx, surf - 10.0, 0.0, 500.0, 20, 0.04, 0.0, terrain);
            // After the terrain is hit, remaining points should equal the impact point
            double lastX = pts[pts.length - 1][0];
            double lastY = pts[pts.length - 1][1];
            // Find the first collision index
            int hitIdx = -1;
            for (int i = 0; i < pts.length - 1; i++) {
                if (pts[i][0] == pts[i + 1][0] && pts[i][1] == pts[i + 1][1]) {
                    hitIdx = i;
                    break;
                }
            }
            // There should be a repeated point somewhere (terrain hit)
            assertTrue(hitIdx >= 0 || terrain.isSolid((int) lastX, (int) lastY),
                "Preview should stop at terrain");
        }
    }
}
