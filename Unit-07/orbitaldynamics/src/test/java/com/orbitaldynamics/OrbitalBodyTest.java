package com.orbitaldynamics;

import com.orbitaldynamics.math.Vector2D;
import com.orbitaldynamics.sim.body.OrbitalBody;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link OrbitalBody}.
 *
 * <p>Focuses on the physics properties: mass formula, kinetic energy,
 * trail recording, and the pin/select flags.
 */
@DisplayName("OrbitalBody")
class OrbitalBodyTest {

    private static final double EPS = 1e-9;

    private OrbitalBody body;

    @BeforeEach
    void setUp() {
        body = new OrbitalBody(
                new Vector2D(100, 200),
                new Vector2D(10, 0),
                20.0,    // radius
                1.0,     // density
                0.0,     // angle
                0.5      // omega
        );
    }

    // ── Mass ──────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("mass = density × π × r²")
    class Mass {

        @Test
        @DisplayName("mass formula correct for density=1, r=20")
        void massFormula() {
            double expected = 1.0 * Math.PI * 20.0 * 20.0;
            assertEquals(expected, body.getMass(), 1e-6);
        }

        @Test
        @DisplayName("mass doubles when density doubles")
        void massScalesWithDensity() {
            double m1 = body.getMass();
            body.setDensity(2.0);
            assertEquals(m1 * 2.0, body.getMass(), 1e-6);
        }

        @Test
        @DisplayName("mass quadruples when radius doubles")
        void massScalesWithRadiusSquared() {
            double m1 = body.getMass();
            body.setRadius(40.0);
            assertEquals(m1 * 4.0, body.getMass(), 1e-6);
        }
    }

    // ── Kinetic energy ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("kineticEnergy")
    class KineticEnergy {

        @Test
        @DisplayName("translational KE = ½mv² when ω=0")
        void translationalKe() {
            body.setOmega(0);
            double m  = body.getMass();
            double vSq = body.getVelocity().magnitudeSq();
            assertEquals(0.5 * m * vSq, body.kineticEnergy(), 1e-6);
        }

        @Test
        @DisplayName("KE > 0 when moving")
        void kePositive() {
            assertTrue(body.kineticEnergy() > 0);
        }

        @Test
        @DisplayName("KE = 0 when at rest with no spin")
        void keZeroAtRest() {
            body.setVelocity(Vector2D.ZERO);
            body.setOmega(0);
            assertEquals(0.0, body.kineticEnergy(), EPS);
        }

        @Test
        @DisplayName("KE increases with higher velocity")
        void keScalesWithSpeed() {
            body.setOmega(0);
            body.setVelocity(new Vector2D(10, 0));
            double ke1 = body.kineticEnergy();
            body.setVelocity(new Vector2D(20, 0));
            double ke2 = body.kineticEnergy();
            assertEquals(ke2, ke1 * 4.0, 1e-6);  // KE ∝ v²
        }
    }

    // ── Trail ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("trail recording")
    class Trail {

        @Test
        @DisplayName("trail is empty on construction")
        void emptyOnConstruct() {
            assertTrue(body.getTrail().isEmpty());
        }

        @Test
        @DisplayName("trail grows after enough tickTrail calls (period=3)")
        void trailGrowsAfterPeriod() {
            for (int i = 0; i < 3; i++) body.tickTrail();
            assertEquals(1, body.getTrail().size());
        }

        @Test
        @DisplayName("trail is capped at MAX_TRAIL")
        void trailCapped() {
            for (int i = 0; i < OrbitalBody.MAX_TRAIL * 3 + 10; i++) body.tickTrail();
            assertTrue(body.getTrail().size() <= OrbitalBody.MAX_TRAIL);
        }

        @Test
        @DisplayName("clearTrail empties the trail")
        void clearTrail() {
            for (int i = 0; i < 10; i++) body.tickTrail();
            body.clearTrail();
            assertTrue(body.getTrail().isEmpty());
        }
    }

    // ── Flags ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("flags")
    class Flags {

        @Test
        @DisplayName("pinned defaults to false")
        void pinnedDefault() {
            assertFalse(body.isPinned());
        }

        @Test
        @DisplayName("selected defaults to false")
        void selectedDefault() {
            assertFalse(body.isSelected());
        }

        @Test
        @DisplayName("pin toggle works")
        void pinToggle() {
            body.setPinned(true);
            assertTrue(body.isPinned());
            body.setPinned(false);
            assertFalse(body.isPinned());
        }
    }

    // ── Unique IDs ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("each body gets a unique id")
    void uniqueIds() {
        OrbitalBody a = new OrbitalBody(Vector2D.ZERO, Vector2D.ZERO, 10, 1, 0, 0);
        OrbitalBody b = new OrbitalBody(Vector2D.ZERO, Vector2D.ZERO, 10, 1, 0, 0);
        assertNotEquals(a.getId(), b.getId());
    }
}
