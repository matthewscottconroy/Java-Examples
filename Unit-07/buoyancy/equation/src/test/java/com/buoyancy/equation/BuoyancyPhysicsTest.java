package com.buoyancy.equation;

import com.buoyancy.equation.model.BuoyancyObject;
import com.buoyancy.equation.model.Fluid;
import com.buoyancy.equation.physics.BuoyancyPhysics;
import org.junit.jupiter.api.*;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BuoyancyPhysics}.
 *
 * <p>All physical assertions use SI units via the PPM conversion.
 * Expected values are computed by hand from Archimedes' principle:
 * {@code F_b = ρ_fluid × g × V_sub}.
 */
@DisplayName("BuoyancyPhysics")
class BuoyancyPhysicsTest {

    private static final double G   = 9.81;
    private static final double PPM = BuoyancyObject.PPM;

    /** Surface at y=200 on a 700-px canvas — fluid below that line. */
    private static final int SURF_Y   = 200;
    private static final int FLOOR_Y  = 645;
    private static final int CEIL_Y   = 10;

    private Fluid          fluid;
    private BuoyancyObject pine;    // ρ=530 — floats in water
    private BuoyancyObject lead;    // ρ=11340 — sinks

    @BeforeEach
    void setUp() {
        fluid = new Fluid(1000.0, SURF_Y);
        // Place fully submerged: top at surfaceY+10
        pine = new BuoyancyObject(300, SURF_Y + 10, 0.25, 0.40, 530.0,  "Pine", Color.GREEN);
        lead = new BuoyancyObject(300, SURF_Y + 10, 0.25, 0.40, 11340.0, "Lead", Color.GRAY);
    }

    // ── Submerged height ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("submergedHeightM")
    class SubmergedHeight {

        @Test
        @DisplayName("fully submerged object → height = heightM")
        void fullySub() {
            double h = BuoyancyPhysics.submergedHeightM(pine, fluid);
            assertEquals(pine.getHeightM(), h, 1e-6);
        }

        @Test
        @DisplayName("object entirely above surface → submerged height = 0")
        void aboveSurface() {
            pine.setY(SURF_Y - pine.getHeightPx() - 5);
            double h = BuoyancyPhysics.submergedHeightM(pine, fluid);
            assertEquals(0.0, h, 1e-9);
        }

        @Test
        @DisplayName("half-submerged object → height ≈ heightM / 2")
        void halfSub() {
            // Place top face exactly at surface: half-way submerged would need
            // top at surfaceY - heightPx/2
            int halfHeight = pine.getHeightPx() / 2;
            pine.setY(SURF_Y - halfHeight);
            double h = BuoyancyPhysics.submergedHeightM(pine, fluid);
            assertEquals(pine.getHeightM() / 2.0, h, 0.01);
        }

        @Test
        @DisplayName("submerged height is never negative")
        void neverNegative() {
            pine.setY(SURF_Y - pine.getHeightPx() - 100);
            assertTrue(BuoyancyPhysics.submergedHeightM(pine, fluid) >= 0.0);
        }

        @Test
        @DisplayName("submerged height is never greater than object height")
        void neverExceedsHeight() {
            double h = BuoyancyPhysics.submergedHeightM(pine, fluid);
            assertTrue(h <= pine.getHeightM() + 1e-9);
        }
    }

    // ── Submerged volume ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("submergedVolumeM3")
    class SubmergedVolume {

        @Test
        @DisplayName("fully submerged → volume = π r² h")
        void fullVolume() {
            double expected = Math.PI * pine.getRadiusM() * pine.getRadiusM() * pine.getHeightM();
            assertEquals(expected, BuoyancyPhysics.submergedVolumeM3(pine, fluid), 1e-9);
        }

        @Test
        @DisplayName("above surface → volume = 0")
        void zeroVolume() {
            pine.setY(SURF_Y - pine.getHeightPx() - 5);
            assertEquals(0.0, BuoyancyPhysics.submergedVolumeM3(pine, fluid), 1e-9);
        }
    }

    // ── Buoyant force ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("buoyantForce = ρ_fluid × g × V_sub")
    class BuoyantForce {

        @Test
        @DisplayName("fully submerged pine — matches Archimedes formula")
        void fullySubmergedPine() {
            double vSub     = BuoyancyPhysics.submergedVolumeM3(pine, fluid);
            double expected = fluid.getDensityKgM3() * G * vSub;
            assertEquals(expected, BuoyancyPhysics.buoyantForce(pine, fluid, G), 1e-6);
        }

        @Test
        @DisplayName("above surface → buoyant force = 0")
        void noForceAboveSurface() {
            pine.setY(SURF_Y - pine.getHeightPx() - 10);
            assertEquals(0.0, BuoyancyPhysics.buoyantForce(pine, fluid, G), 1e-9);
        }

        @Test
        @DisplayName("buoyant force increases with fluid density")
        void scalesWithFluidDensity() {
            double fbWater   = BuoyancyPhysics.buoyantForce(pine, fluid, G);
            fluid.setDensityKgM3(13534.0);
            double fbMercury = BuoyancyPhysics.buoyantForce(pine, fluid, G);
            assertTrue(fbMercury > fbWater);
        }

        @Test
        @DisplayName("buoyant force scales with g")
        void scalesWithG() {
            double fb1 = BuoyancyPhysics.buoyantForce(pine, fluid, 9.81);
            double fb2 = BuoyancyPhysics.buoyantForce(pine, fluid, 19.62);
            assertEquals(fb2, fb1 * 2.0, 1e-6);
        }
    }

    // ── Gravitational force ───────────────────────────────────────────────────

    @Nested
    @DisplayName("gravitationalForce = mass × g")
    class GravitationalForce {

        @Test
        @DisplayName("matches mass × g")
        void matchesMassG() {
            double expected = pine.getMass() * G;
            assertEquals(expected, BuoyancyPhysics.gravitationalForce(pine, G), 1e-6);
        }
    }

    // ── Net force ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("netForce = F_gravity − F_buoyancy")
    class NetForce {

        @Test
        @DisplayName("floating object has negative net force (rises until equilibrium)")
        void pineRises() {
            // Pine fully submerged → F_b > F_g → net negative (upward)
            assertTrue(BuoyancyPhysics.netForce(pine, fluid, G) < 0,
                    "net force should be upward for buoyant pine");
        }

        @Test
        @DisplayName("dense object has positive net force (sinks)")
        void leadSinks() {
            assertTrue(BuoyancyPhysics.netForce(lead, fluid, G) > 0,
                    "net force should be downward for dense lead");
        }

        @Test
        @DisplayName("neutral-density object has near-zero net force")
        void neutralDensity() {
            BuoyancyObject neutral = new BuoyancyObject(300, SURF_Y + 10,
                    0.25, 0.40, 1000.0, "Neutral", Color.WHITE);
            assertEquals(0.0, BuoyancyPhysics.netForce(neutral, fluid, G), 0.01);
        }
    }

    // ── Equilibrium position ──────────────────────────────────────────────────

    @Nested
    @DisplayName("equilibriumY")
    class EquilibriumY {

        @Test
        @DisplayName("denser-than-fluid object returns NaN")
        void denseObjectNaN() {
            assertTrue(Double.isNaN(BuoyancyPhysics.equilibriumY(lead, fluid)));
        }

        @Test
        @DisplayName("lighter-than-fluid object returns finite y")
        void lightObjectFinite() {
            assertFalse(Double.isNaN(BuoyancyPhysics.equilibriumY(pine, fluid)));
        }

        @Test
        @DisplayName("equilibrium y is above or at fluid surface")
        void equilibriumAboveSurface() {
            double eqY = BuoyancyPhysics.equilibriumY(pine, fluid);
            // The top of the object at equilibrium should be above (less than) surface
            assertTrue(eqY < SURF_Y,
                    "floating object top should be above fluid surface at equilibrium");
        }

        @Test
        @DisplayName("density ratio ρ_obj/ρ_fluid equals fraction submerged at equilibrium")
        void densityRatioMatchesFraction() {
            double eqY   = BuoyancyPhysics.equilibriumY(pine, fluid);
            pine.setY(eqY);
            double frac  = BuoyancyPhysics.submergedHeightM(pine, fluid) / pine.getHeightM();
            double ratio = pine.getDensityKgM3() / fluid.getDensityKgM3();
            assertEquals(ratio, frac, 0.01);
        }
    }

    // ── Physics step ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("step integration")
    class Step {

        @Test
        @DisplayName("pinned object does not move")
        void pinnedDoesNotMove() {
            pine.setPinned(true);
            double yBefore = pine.getY();
            BuoyancyPhysics.step(pine, fluid, G, 0.016, FLOOR_Y, CEIL_Y);
            assertEquals(yBefore, pine.getY(), 1e-9);
        }

        @Test
        @DisplayName("sinking object moves down each step")
        void sinkingMovesDown() {
            double yBefore = lead.getY();
            BuoyancyPhysics.step(lead, fluid, G, 0.016, FLOOR_Y, CEIL_Y);
            assertTrue(lead.getY() > yBefore, "lead should sink (y increases downward)");
        }

        @Test
        @DisplayName("rising object moves up each step (fully submerged)")
        void buoyantMovesUp() {
            double yBefore = pine.getY();
            BuoyancyPhysics.step(pine, fluid, G, 0.016, FLOOR_Y, CEIL_Y);
            assertTrue(pine.getY() < yBefore, "pine should rise (y decreases upward)");
        }

        @Test
        @DisplayName("object cannot fall below floor")
        void clampedAtFloor() {
            lead.setY(FLOOR_Y - 5);  // nearly at floor
            for (int i = 0; i < 20; i++) {
                BuoyancyPhysics.step(lead, fluid, G, 0.016, FLOOR_Y, CEIL_Y);
            }
            assertTrue(lead.getBottomY() <= FLOOR_Y + 1,
                    "object bottom must not exceed floor");
        }

        @Test
        @DisplayName("object cannot rise above ceiling")
        void clampedAtCeiling() {
            pine.setY(CEIL_Y + 2);
            pine.setVy(-5000);
            BuoyancyPhysics.step(pine, fluid, G, 0.016, FLOOR_Y, CEIL_Y);
            assertTrue(pine.getY() >= CEIL_Y - 1,
                    "object must not go above ceiling");
        }
    }
}
