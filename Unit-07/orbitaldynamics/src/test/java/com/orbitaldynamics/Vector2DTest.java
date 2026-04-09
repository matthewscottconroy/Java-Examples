package com.orbitaldynamics;

import com.orbitaldynamics.math.Vector2D;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Vector2D}.
 *
 * <p>All operations are exact (no floating-point loss in integer coordinates).
 */
@DisplayName("Vector2D")
class Vector2DTest {

    private static final double EPS = 1e-10;

    // ── Arithmetic ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("arithmetic")
    class Arithmetic {

        @Test
        @DisplayName("add combines components")
        void add() {
            Vector2D r = new Vector2D(1, 2).add(new Vector2D(3, 4));
            assertEquals(4, r.x(), EPS);
            assertEquals(6, r.y(), EPS);
        }

        @Test
        @DisplayName("sub subtracts components")
        void sub() {
            Vector2D r = new Vector2D(5, 7).sub(new Vector2D(2, 3));
            assertEquals(3, r.x(), EPS);
            assertEquals(4, r.y(), EPS);
        }

        @Test
        @DisplayName("scale multiplies both components")
        void scale() {
            Vector2D r = new Vector2D(2, 3).scale(4);
            assertEquals(8,  r.x(), EPS);
            assertEquals(12, r.y(), EPS);
        }

        @Test
        @DisplayName("negate flips both components")
        void negate() {
            Vector2D r = new Vector2D(3, -5).negate();
            assertEquals(-3, r.x(), EPS);
            assertEquals(5,  r.y(), EPS);
        }

        @Test
        @DisplayName("scale by 0 returns zero vector")
        void scaleZero() {
            Vector2D r = new Vector2D(5, 7).scale(0);
            assertEquals(0, r.x(), EPS);
            assertEquals(0, r.y(), EPS);
        }
    }

    // ── Dot and cross products ────────────────────────────────────────────────

    @Nested
    @DisplayName("dot and cross products")
    class Products {

        @Test
        @DisplayName("dot product is commutative")
        void dotCommutative() {
            Vector2D a = new Vector2D(2, 3);
            Vector2D b = new Vector2D(4, 5);
            assertEquals(a.dot(b), b.dot(a), EPS);
        }

        @Test
        @DisplayName("dot product of orthogonal vectors is 0")
        void dotOrthogonal() {
            assertEquals(0, new Vector2D(1, 0).dot(new Vector2D(0, 1)), EPS);
        }

        @Test
        @DisplayName("dot of (3,4) · (3,4) = 25")
        void dotSelf() {
            assertEquals(25, new Vector2D(3, 4).dot(new Vector2D(3, 4)), EPS);
        }

        @Test
        @DisplayName("cross product (z-component) correct")
        void cross() {
            // (1,0) × (0,1) = 1  (CCW)
            assertEquals(1, new Vector2D(1, 0).cross(new Vector2D(0, 1)), EPS);
        }

        @Test
        @DisplayName("cross is antisymmetric")
        void crossAntiSymmetric() {
            Vector2D a = new Vector2D(2, 3);
            Vector2D b = new Vector2D(5, 1);
            assertEquals(-a.cross(b), b.cross(a), EPS);
        }
    }

    // ── Magnitude ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("magnitude")
    class Magnitude {

        @Test
        @DisplayName("3-4-5 triangle")
        void magnitude345() {
            assertEquals(5, new Vector2D(3, 4).magnitude(), EPS);
        }

        @Test
        @DisplayName("ZERO has magnitude 0")
        void zeroMagnitude() {
            assertEquals(0, Vector2D.ZERO.magnitude(), EPS);
        }

        @Test
        @DisplayName("magnitudeSq = magnitude²")
        void magnitudeSq() {
            Vector2D v = new Vector2D(3, 4);
            assertEquals(v.magnitude() * v.magnitude(), v.magnitudeSq(), EPS);
        }
    }

    // ── Normalize ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("normalize")
    class Normalize {

        @Test
        @DisplayName("normalized vector has magnitude 1")
        void normalizedMagnitude() {
            assertEquals(1.0, new Vector2D(3, 4).normalize().magnitude(), EPS);
        }

        @Test
        @DisplayName("normalizing ZERO returns ZERO (no NaN)")
        void normalizeZero() {
            Vector2D r = Vector2D.ZERO.normalize();
            assertFalse(Double.isNaN(r.x()));
            assertFalse(Double.isNaN(r.y()));
        }

        @Test
        @DisplayName("direction is preserved after normalizing")
        void normalizeDirection() {
            Vector2D v  = new Vector2D(3, 4);
            Vector2D n  = v.normalize();
            assertEquals(3.0 / 5.0, n.x(), EPS);
            assertEquals(4.0 / 5.0, n.y(), EPS);
        }
    }

    // ── Geometric operations ──────────────────────────────────────────────────

    @Nested
    @DisplayName("geometric operations")
    class Geometric {

        @Test
        @DisplayName("perpendicular of (1,0) is (0,1)")
        void perpendicular() {
            Vector2D p = new Vector2D(1, 0).perpendicular();
            assertEquals(0, p.x(), EPS);
            assertEquals(1, p.y(), EPS);
        }

        @Test
        @DisplayName("perpendicular is orthogonal to original")
        void perpendicularOrthogonal() {
            Vector2D v = new Vector2D(3, 4);
            assertEquals(0, v.dot(v.perpendicular()), EPS);
        }

        @Test
        @DisplayName("rotate 90° CCW: (1,0) → (0,1)")
        void rotate90() {
            Vector2D r = new Vector2D(1, 0).rotate(Math.PI / 2);
            assertEquals(0, r.x(), 1e-9);
            assertEquals(1, r.y(), 1e-9);
        }

        @Test
        @DisplayName("rotate 180°: (1,0) → (-1,0)")
        void rotate180() {
            Vector2D r = new Vector2D(1, 0).rotate(Math.PI);
            assertEquals(-1, r.x(), 1e-9);
            assertEquals(0,  r.y(), 1e-9);
        }

        @Test
        @DisplayName("distanceTo is symmetric")
        void distanceSymmetric() {
            Vector2D a = new Vector2D(0, 0);
            Vector2D b = new Vector2D(3, 4);
            assertEquals(a.distanceTo(b), b.distanceTo(a), EPS);
        }

        @Test
        @DisplayName("distanceTo 3-4-5 = 5")
        void distanceTo345() {
            assertEquals(5.0, new Vector2D(0, 0).distanceTo(new Vector2D(3, 4)), EPS);
        }
    }

    // ── Immutability ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("operations return new instances (immutability)")
    void immutable() {
        Vector2D original = new Vector2D(1, 2);
        Vector2D result   = original.add(new Vector2D(10, 20));
        assertEquals(1, original.x(), EPS);
        assertEquals(2, original.y(), EPS);
        assertEquals(11, result.x(), EPS);
    }
}
