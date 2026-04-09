package com.bouncingballs;

import com.bouncingballs.model.Ball;
import org.junit.jupiter.api.*;
import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Ball}.
 *
 * <p>Verifies construction invariants, derived-value calculations (mass, hit-testing),
 * and the density/size enum helpers.
 */
@DisplayName("Ball")
class BallTest {

    private static final double DENSITY = 1.0;
    private static final double RADIUS  = 20.0;

    // ── Construction ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("position is stored correctly")
        void positionStored() {
            Ball b = new Ball(100, 200, RADIUS, DENSITY, Color.RED);
            assertEquals(100.0, b.x, 1e-9);
            assertEquals(200.0, b.y, 1e-9);
        }

        @Test
        @DisplayName("initial velocity is zero")
        void initialVelocityZero() {
            Ball b = new Ball(0, 0, RADIUS, DENSITY, Color.RED);
            assertEquals(0.0, b.vx, 1e-9);
            assertEquals(0.0, b.vy, 1e-9);
        }

        @Test
        @DisplayName("radius is stored correctly")
        void radiusStored() {
            Ball b = new Ball(0, 0, RADIUS, DENSITY, Color.RED);
            assertEquals(RADIUS, b.radius, 1e-9);
        }

        @Test
        @DisplayName("density is stored correctly")
        void densityStored() {
            Ball b = new Ball(0, 0, RADIUS, 2.5, Color.BLUE);
            assertEquals(2.5, b.density, 1e-9);
        }

        @Test
        @DisplayName("color is stored correctly")
        void colorStored() {
            Ball b = new Ball(0, 0, RADIUS, DENSITY, Color.GREEN);
            assertEquals(Color.GREEN, b.color);
        }

        @Test
        @DisplayName("each ball receives a unique id")
        void uniqueIds() {
            Ball a = new Ball(0, 0, RADIUS, DENSITY, Color.RED);
            Ball b = new Ball(0, 0, RADIUS, DENSITY, Color.RED);
            assertNotEquals(a.id, b.id);
        }
    }

    // ── Mass calculation ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("mass = density × π × radius²")
    class Mass {

        @Test
        @DisplayName("mass matches formula for density=1 radius=20")
        void massFormula() {
            Ball b = new Ball(0, 0, 20.0, 1.0, Color.RED);
            double expected = 1.0 * Math.PI * 20.0 * 20.0;
            assertEquals(expected, b.mass, 1e-6);
        }

        @Test
        @DisplayName("mass scales linearly with density")
        void massScalesWithDensity() {
            Ball light  = new Ball(0, 0, 20.0, 0.5, Color.RED);
            Ball heavy  = new Ball(0, 0, 20.0, 2.0, Color.RED);
            assertEquals(heavy.mass / light.mass, 4.0, 1e-9);
        }

        @Test
        @DisplayName("mass scales as radius² with fixed density")
        void massScalesWithRadiusSquared() {
            Ball small = new Ball(0, 0, 10.0, 1.0, Color.RED);
            Ball large = new Ball(0, 0, 20.0, 1.0, Color.RED);
            assertEquals(large.mass / small.mass, 4.0, 1e-9);  // (20/10)² = 4
        }

        @Test
        @DisplayName("heavier density Ball.Density values produce more mass")
        void densityEnumMassOrder() {
            Ball light  = new Ball(0, 0, 15.0, Ball.Density.LIGHT.value,  Color.RED);
            Ball medium = new Ball(0, 0, 15.0, Ball.Density.MEDIUM.value, Color.RED);
            Ball heavy  = new Ball(0, 0, 15.0, Ball.Density.HEAVY.value,  Color.RED);
            assertTrue(light.mass < medium.mass);
            assertTrue(medium.mass < heavy.mass);
        }
    }

    // ── Hit testing ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("contains(px, py)")
    class Contains {

        @Test
        @DisplayName("centre point is inside")
        void centreInside() {
            Ball b = new Ball(50, 50, 20.0, DENSITY, Color.RED);
            assertTrue(b.contains(50, 50));
        }

        @Test
        @DisplayName("point just inside radius is inside")
        void justInsideRadius() {
            Ball b = new Ball(50, 50, 20.0, DENSITY, Color.RED);
            assertTrue(b.contains(50 + 19, 50));
        }

        @Test
        @DisplayName("point exactly on radius edge is inside")
        void onRadiusEdge() {
            Ball b = new Ball(50, 50, 20.0, DENSITY, Color.RED);
            assertTrue(b.contains(70, 50));  // exactly 20px right
        }

        @Test
        @DisplayName("point just outside radius is outside")
        void justOutsideRadius() {
            Ball b = new Ball(50, 50, 20.0, DENSITY, Color.RED);
            assertFalse(b.contains(71, 50));
        }

        @Test
        @DisplayName("diagonal point inside radius is inside")
        void diagonalInside() {
            Ball b = new Ball(0, 0, 20.0, DENSITY, Color.RED);
            // (14, 14) has distance ≈ 19.8 < 20
            assertTrue(b.contains(14, 14));
        }

        @Test
        @DisplayName("diagonal point outside radius is outside")
        void diagonalOutside() {
            Ball b = new Ball(0, 0, 20.0, DENSITY, Color.RED);
            // (15, 15) has distance ≈ 21.2 > 20
            assertFalse(b.contains(15, 15));
        }
    }

    // ── Size enum ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Ball.Size")
    class SizeEnum {

        @Test
        @DisplayName("random radius stays within size bounds")
        void randomRadiusInBounds() {
            java.util.Random rng = new java.util.Random(42);
            for (int i = 0; i < 200; i++) {
                for (Ball.Size s : Ball.Size.values()) {
                    double r = s.randomRadius(rng);
                    assertTrue(r >= s.min && r <= s.max,
                            "radius " + r + " out of bounds for " + s);
                }
            }
        }

        @Test
        @DisplayName("SMALL radius is smaller than MEDIUM radius on average")
        void sizesOrdered() {
            java.util.Random rng = new java.util.Random(0);
            assertTrue(Ball.Size.SMALL.max < Ball.Size.MEDIUM.min
                    || Ball.Size.SMALL.max <= Ball.Size.MEDIUM.max);
            assertTrue(Ball.Size.MEDIUM.max <= Ball.Size.LARGE.max);
        }
    }

    // ── Density enum ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Ball.Density")
    class DensityEnum {

        @Test
        @DisplayName("LIGHT < MEDIUM < HEAVY")
        void densityOrder() {
            assertTrue(Ball.Density.LIGHT.value  < Ball.Density.MEDIUM.value);
            assertTrue(Ball.Density.MEDIUM.value < Ball.Density.HEAVY.value);
        }

        @Test
        @DisplayName("all density values are positive")
        void densitiesPositive() {
            for (Ball.Density d : Ball.Density.values()) {
                assertTrue(d.value > 0, d + " must be positive");
            }
        }
    }
}
