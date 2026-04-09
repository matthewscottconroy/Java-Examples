package com.buoyancy.equation;

import com.buoyancy.equation.model.BuoyancyObject;
import org.junit.jupiter.api.*;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BuoyancyObject}.
 *
 * <p>Verifies derived geometry (pixel dimensions, areas, volume) and the
 * mass formula {@code m = ρ × π × r² × h}.
 */
@DisplayName("BuoyancyObject")
class BuoyancyObjectTest {

    private static final double G = 9.81;
    private static final double R = 0.25;   // metres
    private static final double H = 0.50;   // metres
    private static final double D = 530.0;  // kg/m³ (pine)
    private static final double PPM = BuoyancyObject.PPM;

    private BuoyancyObject obj;

    @BeforeEach
    void setUp() {
        obj = new BuoyancyObject(300, 200, R, H, D, "Pine", Color.GREEN);
    }

    // ── Geometry ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("derived geometry")
    class Geometry {

        @Test
        @DisplayName("volume = π × r² × h")
        void volume() {
            double expected = Math.PI * R * R * H;
            assertEquals(expected, obj.getVolume(), 1e-9);
        }

        @Test
        @DisplayName("mass = density × volume")
        void mass() {
            double expected = D * Math.PI * R * R * H;
            assertEquals(expected, obj.getMass(), 1e-6);
        }

        @Test
        @DisplayName("radiusPx = round(radiusM × PPM), minimum 8")
        void radiusPx() {
            int expected = Math.max(8, (int) Math.round(R * PPM));
            assertEquals(expected, obj.getRadiusPx());
        }

        @Test
        @DisplayName("heightPx = round(heightM × PPM), minimum 6")
        void heightPx() {
            int expected = Math.max(6, (int) Math.round(H * PPM));
            assertEquals(expected, obj.getHeightPx());
        }

        @Test
        @DisplayName("widthPx = 2 × radiusPx")
        void widthPx() {
            assertEquals(2 * obj.getRadiusPx(), obj.getWidthPx());
        }

        @Test
        @DisplayName("bottomY = y + heightPx")
        void bottomY() {
            double expected = obj.getY() + obj.getHeightPx();
            assertEquals(expected, obj.getBottomY(), 1e-9);
        }

        @Test
        @DisplayName("leftX = cx − radiusPx")
        void leftX() {
            assertEquals(obj.getCx() - obj.getRadiusPx(), obj.getLeftX(), 1e-9);
        }
    }

    // ── Setters clamp ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("setter clamping")
    class Clamping {

        @Test
        @DisplayName("radius cannot be set below 0.05 m")
        void radiusMinClamp() {
            obj.setRadiusM(0.001);
            assertEquals(0.05, obj.getRadiusM(), 1e-9);
        }

        @Test
        @DisplayName("height cannot be set below 0.05 m")
        void heightMinClamp() {
            obj.setHeightM(0.001);
            assertEquals(0.05, obj.getHeightM(), 1e-9);
        }

        @Test
        @DisplayName("density cannot be set below 10 kg/m³")
        void densityMinClamp() {
            obj.setDensityKgM3(1.0);
            assertEquals(10.0, obj.getDensityKgM3(), 1e-9);
        }
    }

    // ── Hit test ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("contains(px, py)")
    class Contains {

        @Test
        @DisplayName("centre point is inside")
        void centreInside() {
            int cx = (int) obj.getCx();
            int cy = (int) (obj.getY() + obj.getHeightPx() / 2);
            assertTrue(obj.contains(cx, cy));
        }

        @Test
        @DisplayName("point far outside is not inside")
        void farOutside() {
            assertFalse(obj.contains(0, 0));
        }
    }

    // ── Pin / select ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("pinned flag defaults to false")
    void pinnedDefaultFalse() {
        assertFalse(obj.isPinned());
    }

    @Test
    @DisplayName("selected flag defaults to false")
    void selectedDefaultFalse() {
        assertFalse(obj.isSelected());
    }

    @Test
    @DisplayName("pin and unpin toggle correctly")
    void pinToggle() {
        obj.setPinned(true);
        assertTrue(obj.isPinned());
        obj.setPinned(false);
        assertFalse(obj.isPinned());
    }
}
