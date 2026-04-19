package com.buffon.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Needle}.
 */
@DisplayName("Needle")
class NeedleTest {

    private static final double LEN = 80.0;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("stores all fields correctly")
        void storesFields() {
            Needle n = new Needle(100, 200, Math.PI / 4, LEN, true);
            assertEquals(100.0,        n.cx(),     1e-12);
            assertEquals(200.0,        n.cy(),     1e-12);
            assertEquals(Math.PI / 4,  n.angle(),  1e-12);
            assertEquals(LEN,          n.length(), 1e-12);
            assertTrue(n.crosses());
        }
    }

    // -------------------------------------------------------------------------
    // Endpoints
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("endpoints")
    class Endpoints {

        @Test
        @DisplayName("horizontal needle (angle=0) has y1 == y2 == cy")
        void horizontalNeedle() {
            Needle n = new Needle(200, 150, 0.0, LEN, false);
            assertEquals(150.0, n.y1(), 1e-9);
            assertEquals(150.0, n.y2(), 1e-9);
        }

        @Test
        @DisplayName("horizontal needle spans ±L/2 about cx")
        void horizontalNeedleX() {
            Needle n = new Needle(200, 150, 0.0, LEN, false);
            assertEquals(200 - LEN / 2, n.x1(), 1e-9);
            assertEquals(200 + LEN / 2, n.x2(), 1e-9);
        }

        @Test
        @DisplayName("vertical needle (angle=π/2) has x1 == x2 == cx")
        void verticalNeedle() {
            Needle n = new Needle(300, 200, Math.PI / 2, LEN, false);
            assertEquals(300.0, n.x1(), 1e-9);
            assertEquals(300.0, n.x2(), 1e-9);
        }

        @Test
        @DisplayName("vertical needle spans ±L/2 about cy")
        void verticalNeedleY() {
            Needle n = new Needle(300, 200, Math.PI / 2, LEN, false);
            assertEquals(200 - LEN / 2, n.y1(), 1e-9);
            assertEquals(200 + LEN / 2, n.y2(), 1e-9);
        }

        @Test
        @DisplayName("endpoint midpoint is the centre")
        void midpointIsCentre() {
            Needle n = new Needle(250, 180, 1.1, LEN, false);
            assertEquals(n.cx(), (n.x1() + n.x2()) / 2, 1e-9);
            assertEquals(n.cy(), (n.y1() + n.y2()) / 2, 1e-9);
        }

        @Test
        @DisplayName("endpoint distance equals needle length")
        void lengthConsistency() {
            Needle n = new Needle(300, 300, 0.7, LEN, false);
            double dx   = n.x2() - n.x1();
            double dy   = n.y2() - n.y1();
            double dist = Math.hypot(dx, dy);
            assertEquals(LEN, dist, 1e-9);
        }

        @Test
        @DisplayName("45-degree needle endpoints are symmetric about centre")
        void diagonalEndpoints() {
            Needle n = new Needle(200, 200, Math.PI / 4, LEN, false);
            double halfX = LEN / 2 * Math.cos(Math.PI / 4);
            double halfY = LEN / 2 * Math.sin(Math.PI / 4);
            assertEquals(200 - halfX, n.x1(), 1e-9);
            assertEquals(200 - halfY, n.y1(), 1e-9);
            assertEquals(200 + halfX, n.x2(), 1e-9);
            assertEquals(200 + halfY, n.y2(), 1e-9);
        }
    }
}
