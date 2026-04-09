package com.bouncingballs;

import com.bouncingballs.model.Ball;
import com.bouncingballs.physics.PhysicsEngine;
import org.junit.jupiter.api.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PhysicsEngine}.
 *
 * <p>Physics is deterministic for a given set of inputs, so every assertion
 * here is an exact check (with a small floating-point epsilon where needed).
 * No random seeds are used: all initial conditions are hand-chosen so that the
 * expected outcome is analytically clear.
 */
@DisplayName("PhysicsEngine")
class PhysicsEngineTest {

    // Jar boundaries used throughout
    private static final double LEFT   = 22;
    private static final double RIGHT  = 478;
    private static final double TOP    = 18;
    private static final double BOTTOM = 558;

    private static Ball ball(double x, double y) {
        return new Ball(x, y, 15.0, 1.0, Color.RED);
    }

    private static List<Ball> list(Ball... balls) {
        List<Ball> l = new ArrayList<>();
        Collections.addAll(l, balls);
        return l;
    }

    // ── Gravity ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("gravity")
    class Gravity {

        @Test
        @DisplayName("positive gravity accelerates ball downward each step")
        void gravityIncreasesVy() {
            Ball b = ball(250, 200);
            double beforeVy = b.vy;
            PhysicsEngine.step(list(b), 800, 0.016, LEFT, RIGHT, TOP, BOTTOM, 0, 0);
            assertTrue(b.vy > beforeVy, "vy should increase under positive gravity");
        }

        @Test
        @DisplayName("zero gravity causes no vertical velocity change")
        void zeroGravityNoVyChange() {
            Ball b = ball(250, 200);
            PhysicsEngine.step(list(b), 0, 0.016, LEFT, RIGHT, TOP, BOTTOM, 0, 0);
            assertEquals(0.0, b.vy, 1e-9);
        }

        @Test
        @DisplayName("ball position advances by velocity × dt")
        void positionIntegration() {
            Ball b = ball(250, 200);
            b.vy = 100.0;
            double dt = 0.016;
            double expectedY = 200.0 + 100.0 * dt + 800.0 * dt * dt; // vy*dt + g*dt² from two half-steps
            PhysicsEngine.step(list(b), 800, dt, LEFT, RIGHT, TOP, BOTTOM, 0, 0);
            // After one step: vy += g*dt, y += vy_new * dt
            // vy_new = 100 + 800*0.016 = 112.8; y_new = 200 + 112.8*0.016 = 201.8048
            assertEquals(200.0 + (100.0 + 800.0 * dt) * dt, b.y, 1e-6);
        }

        @Test
        @DisplayName("empty ball list completes without exception")
        void emptyListNoException() {
            assertDoesNotThrow(() ->
                    PhysicsEngine.step(new ArrayList<>(), 800, 0.016, LEFT, RIGHT, TOP, BOTTOM, 0, 0));
        }
    }

    // ── Wall collisions ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("wall collisions")
    class WallCollisions {

        @Test
        @DisplayName("ball bounces off right wall — velocity reversed")
        void rightWallBounce() {
            Ball b = ball(RIGHT - 15, 300);  // touching right wall
            b.vx = 200.0;
            PhysicsEngine.step(list(b), 0, 0.016, LEFT, RIGHT, TOP, BOTTOM, 0, 0);
            assertTrue(b.vx < 0, "vx should reverse after right-wall collision");
        }

        @Test
        @DisplayName("ball bounces off left wall — velocity reversed")
        void leftWallBounce() {
            Ball b = ball(LEFT + 15, 300);   // touching left wall
            b.vx = -200.0;
            PhysicsEngine.step(list(b), 0, 0.016, LEFT, RIGHT, TOP, BOTTOM, 0, 0);
            assertTrue(b.vx > 0, "vx should reverse after left-wall collision");
        }

        @Test
        @DisplayName("ball bounces off top wall — velocity reversed")
        void topWallBounce() {
            Ball b = ball(250, TOP + 15);    // touching top wall
            b.vy = -200.0;
            PhysicsEngine.step(list(b), 0, 0.016, LEFT, RIGHT, TOP, BOTTOM, 0, 0);
            assertTrue(b.vy > 0, "vy should reverse after top-wall collision");
        }

        @Test
        @DisplayName("ball stops at floor — not clipped below")
        void floorStop() {
            Ball b = ball(250, BOTTOM - 15); // right at floor
            b.vy = 500.0;
            PhysicsEngine.step(list(b), 800, 0.016, LEFT, RIGHT, TOP, BOTTOM, 0, 0);
            assertTrue(b.y + b.radius <= BOTTOM + 1e-6,
                    "ball should not exceed floor boundary");
        }

        @Test
        @DisplayName("ball stays within left boundary after penetration")
        void leftWallClamp() {
            Ball b = ball(LEFT + 5, 300);
            b.vx = -2000.0;  // fast enough to penetrate in one step
            PhysicsEngine.step(list(b), 0, 0.016, LEFT, RIGHT, TOP, BOTTOM, 0, 0);
            assertTrue(b.x - b.radius >= LEFT - 1e-6,
                    "ball must not penetrate left wall");
        }

        @Test
        @DisplayName("ball stays within right boundary after penetration")
        void rightWallClamp() {
            Ball b = ball(RIGHT - 5, 300);
            b.vx = 2000.0;
            PhysicsEngine.step(list(b), 0, 0.016, LEFT, RIGHT, TOP, BOTTOM, 0, 0);
            assertTrue(b.x + b.radius <= RIGHT + 1e-6,
                    "ball must not penetrate right wall");
        }
    }

    // ── Ball–ball collisions ──────────────────────────────────────────────────

    @Nested
    @DisplayName("ball–ball collisions")
    class BallCollisions {

        @Test
        @DisplayName("overlapping stationary balls are separated")
        void overlappingBallsSeparated() {
            Ball a = ball(250, 300);
            Ball b = ball(255, 300);  // 5px apart, radii sum = 30 → overlap = 25px
            PhysicsEngine.step(list(a, b), 0, 0.016, LEFT, RIGHT, TOP, BOTTOM, 0, 0);
            double dist = Math.hypot(b.x - a.x, b.y - a.y);
            assertTrue(dist >= a.radius + b.radius - 1e-3,
                    "balls should be at least touching-distance apart after step");
        }

        @Test
        @DisplayName("head-on equal-mass collision reverses velocities")
        void headOnEqualMass() {
            Ball a = ball(220, 300);
            Ball b = ball(246, 300);  // 26px apart — already overlapping (radii sum = 30)
            a.vx =  300.0;
            b.vx = -300.0;
            PhysicsEngine.step(list(a, b), 0, 0.001, LEFT, RIGHT, TOP, BOTTOM, 0, 0);
            // After elastic collision of equal masses, velocities roughly exchange
            assertTrue(a.vx < 0, "ball a vx should become negative");
            assertTrue(b.vx > 0, "ball b vx should become positive");
        }

        @Test
        @DisplayName("heavier ball deflects lighter ball more")
        void massDeflection() {
            Ball light = new Ball(220, 300, 15.0, Ball.Density.LIGHT.value,  Color.RED);
            Ball heavy = new Ball(252, 300, 15.0, Ball.Density.HEAVY.value,  Color.RED);
            light.vx =  400.0;
            double heavyVxBefore = heavy.vx;
            PhysicsEngine.step(list(light, heavy), 0, 0.001, LEFT, RIGHT, TOP, BOTTOM, 0, 0);
            // Heavy ball should gain some velocity but much less than light ball loses
            assertTrue(Math.abs(heavy.vx - heavyVxBefore) < Math.abs(light.vx),
                    "heavy ball should be deflected less than light ball");
        }

        @Test
        @DisplayName("balls moving apart do not interact")
        void separatingBallsIgnored() {
            Ball a = ball(235, 300);  // 30px apart → touching
            Ball b = ball(265, 300);
            a.vx = -100.0;  // moving left
            b.vx =  100.0;  // moving right → already separating
            double aVxBefore = a.vx;
            double bVxBefore = b.vx;
            PhysicsEngine.step(list(a, b), 0, 0.001, LEFT, RIGHT, TOP, BOTTOM, 0, 0);
            assertEquals(aVxBefore, a.vx, 1.0, "separating balls should not collide");
            assertEquals(bVxBefore, b.vx, 1.0, "separating balls should not collide");
        }
    }

    // ── Window inertia ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("window inertia")
    class WindowInertia {

        @Test
        @DisplayName("window moving right imparts leftward kick to ball")
        void windowRightKicksLeft() {
            Ball b = ball(250, 300);
            PhysicsEngine.step(list(b), 0, 0.016, LEFT, RIGHT, TOP, BOTTOM, 500, 0);
            assertTrue(b.vx < 0, "ball should receive leftward kick when jar moves right");
        }

        @Test
        @DisplayName("window moving down imparts upward kick to ball")
        void windowDownKicksUp() {
            Ball b = ball(250, 300);
            PhysicsEngine.step(list(b), 0, 0.016, LEFT, RIGHT, TOP, BOTTOM, 0, 500);
            assertTrue(b.vy < 0, "ball should receive upward kick when jar moves down");
        }

        @Test
        @DisplayName("zero window velocity has no inertia effect")
        void zeroWindowVelocity() {
            Ball b = ball(250, 300);
            PhysicsEngine.step(list(b), 0, 0.016, LEFT, RIGHT, TOP, BOTTOM, 0, 0);
            assertEquals(0.0, b.vx, 1e-9);
            assertEquals(0.0, b.vy, 1e-9);
        }

        @Test
        @DisplayName("INERTIA_FACTOR is between 0 and 1 exclusive")
        void inertiaFactorRange() {
            assertTrue(PhysicsEngine.INERTIA_FACTOR > 0.0);
            assertTrue(PhysicsEngine.INERTIA_FACTOR < 1.0);
        }

        @Test
        @DisplayName("kick magnitude scales with window velocity")
        void kickScalesWithWindowVelocity() {
            Ball b1 = ball(250, 300);
            Ball b2 = ball(250, 300);
            PhysicsEngine.step(list(b1), 0, 0.016, LEFT, RIGHT, TOP, BOTTOM, 100, 0);
            PhysicsEngine.step(list(b2), 0, 0.016, LEFT, RIGHT, TOP, BOTTOM, 200, 0);
            assertEquals(b2.vx / b1.vx, 2.0, 0.01, "kick should scale linearly with window velocity");
        }
    }

    // ── Speed cap ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("speed cap")
    class SpeedCap {

        @Test
        @DisplayName("extremely fast ball is clamped to max speed")
        void speedIsCapped() {
            Ball b = ball(250, 300);
            b.vx = 1_000_000.0;
            b.vy = 0;
            PhysicsEngine.step(list(b), 0, 0.016, LEFT, RIGHT, TOP, BOTTOM, 0, 0);
            double speed = Math.hypot(b.vx, b.vy);
            assertTrue(speed <= 4001.0, "speed should be capped near 4000 px/s");
        }
    }
}
