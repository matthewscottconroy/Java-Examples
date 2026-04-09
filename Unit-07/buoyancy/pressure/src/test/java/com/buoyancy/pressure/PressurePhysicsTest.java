package com.buoyancy.pressure;

import com.buoyancy.pressure.model.FluidMedium;
import com.buoyancy.pressure.model.PressureBody;
import com.buoyancy.pressure.physics.PressurePhysics;
import org.junit.jupiter.api.*;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PressurePhysics}.
 *
 * <p>The educational goal of this simulator is to show that the pressure
 * face-summation result equals the Archimedes formula.  Tests here verify
 * both independently and confirm they agree.
 *
 * <p>Reference physics:
 * <pre>
 *   P(d) = ρ × g × d
 *   F_up   = P(d_bottom) × A
 *   F_down = P(d_top)    × A
 *   F_net  = F_up − F_down = ρ × g × h_sub × A = ρ × g × V_sub
 * </pre>
 */
@DisplayName("PressurePhysics")
class PressurePhysicsTest {

    private static final double G      = 9.81;
    private static final double PPM    = PressureBody.PPM;
    private static final int    SURF_Y = 200;
    private static final int    FLOOR_Y = 645;
    private static final int    CEIL_Y  = 10;

    private FluidMedium  water;
    private PressureBody pine;   // ρ=530, floats
    private PressureBody steel;  // ρ=7850, sinks

    @BeforeEach
    void setUp() {
        water = new FluidMedium(1000.0, SURF_Y);
        // Fully submerged: top at surfaceY + 10px
        pine  = new PressureBody(300, SURF_Y + 10, 0.25, 0.40, 530.0,  "Pine",  Color.GREEN);
        steel = new PressureBody(300, SURF_Y + 10, 0.25, 0.40, 7850.0, "Steel", Color.GRAY);
    }

    // ── Submerged geometry ────────────────────────────────────────────────────

    @Nested
    @DisplayName("submergedHeightM")
    class SubmergedHeight {

        @Test
        @DisplayName("fully submerged returns object height")
        void fullySub() {
            assertEquals(pine.getHeightM(),
                    PressurePhysics.submergedHeightM(pine, water), 1e-6);
        }

        @Test
        @DisplayName("above surface returns 0")
        void aboveSurface() {
            pine.setY(SURF_Y - pine.getHeightPx() - 10);
            assertEquals(0.0, PressurePhysics.submergedHeightM(pine, water), 1e-9);
        }

        @Test
        @DisplayName("never exceeds object height")
        void neverExceedsHeight() {
            double h = PressurePhysics.submergedHeightM(pine, water);
            assertTrue(h <= pine.getHeightM() + 1e-9);
        }

        @Test
        @DisplayName("submerged fraction is in [0, 1]")
        void fractionInRange() {
            double f = PressurePhysics.submergedFraction(pine, water);
            assertTrue(f >= 0.0 && f <= 1.0 + 1e-9);
        }
    }

    // ── Face depth ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("face depths")
    class FaceDepths {

        @Test
        @DisplayName("bottom face depth > top face depth when submerged")
        void bottomDeeperThanTop() {
            double dBot = PressurePhysics.bottomFaceDepthM(pine, water);
            double dTop = PressurePhysics.topFaceDepthM(pine, water);
            assertTrue(dBot > dTop, "bottom face must be deeper than top face");
        }

        @Test
        @DisplayName("difference in depths equals submerged height")
        void depthDiffEqualsSubHeight() {
            double dBot = PressurePhysics.bottomFaceDepthM(pine, water);
            double dTop = PressurePhysics.topFaceDepthM(pine, water);
            assertEquals(PressurePhysics.submergedHeightM(pine, water),
                    dBot - dTop, 1e-6);
        }

        @Test
        @DisplayName("top face depth = 0 when top is above surface")
        void topFaceAboveSurface() {
            pine.setY(SURF_Y - 20);  // top is 20px above surface
            assertEquals(0.0, PressurePhysics.topFaceDepthM(pine, water), 1e-9);
        }

        @Test
        @DisplayName("bottom face depth = 0 when whole object above surface")
        void bottomFaceAboveSurface() {
            pine.setY(SURF_Y - pine.getHeightPx() - 10);
            assertEquals(0.0, PressurePhysics.bottomFaceDepthM(pine, water), 1e-9);
        }
    }

    // ── Face pressures ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("face pressures P = ρ × g × d")
    class FacePressures {

        @Test
        @DisplayName("bottom face pressure matches ρ × g × depth_bottom")
        void bottomFacePressure() {
            double dBot     = PressurePhysics.bottomFaceDepthM(pine, water);
            double expected = water.getDensityKgM3() * G * dBot;
            assertEquals(expected,
                    PressurePhysics.bottomFacePressure(pine, water, G), 1e-6);
        }

        @Test
        @DisplayName("top face pressure matches ρ × g × depth_top")
        void topFacePressure() {
            double dTop     = PressurePhysics.topFaceDepthM(pine, water);
            double expected = water.getDensityKgM3() * G * dTop;
            assertEquals(expected,
                    PressurePhysics.topFacePressure(pine, water, G), 1e-6);
        }

        @Test
        @DisplayName("bottom pressure > top pressure when submerged")
        void bottomHigherThanTop() {
            double pBot = PressurePhysics.bottomFacePressure(pine, water, G);
            double pTop = PressurePhysics.topFacePressure(pine, water, G);
            assertTrue(pBot > pTop);
        }
    }

    // ── Face forces ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("face forces F = P × A")
    class FaceForces {

        @Test
        @DisplayName("upward force on bottom face = P_bottom × area")
        void forceOnBottomFace() {
            double p        = PressurePhysics.bottomFacePressure(pine, water, G);
            double expected = p * pine.getBottomFaceAreaM2();
            assertEquals(expected,
                    PressurePhysics.forceOnBottomFace(pine, water, G), 1e-6);
        }

        @Test
        @DisplayName("downward force on top face = P_top × area")
        void forceOnTopFace() {
            double p        = PressurePhysics.topFacePressure(pine, water, G);
            double expected = p * pine.getTopFaceAreaM2();
            assertEquals(expected,
                    PressurePhysics.forceOnTopFace(pine, water, G), 1e-6);
        }
    }

    // ── The key invariant: pressure method == Archimedes ─────────────────────

    @Nested
    @DisplayName("pressure summation == Archimedes (core educational invariant)")
    class PressureEqualsArchimedes {

        @Test
        @DisplayName("fully submerged pine: face-sum == Archimedes")
        void fullySubPineAgrees() {
            double pressure   = PressurePhysics.netBuoyancyPressure(pine, water, G);
            double archimedes = PressurePhysics.netBuoyancyArchimedes(pine, water, G);
            assertEquals(archimedes, pressure, 1e-6,
                    "face-sum and Archimedes must agree for fully submerged body");
        }

        @Test
        @DisplayName("fully submerged steel: face-sum == Archimedes")
        void fullySubSteelAgrees() {
            double pressure   = PressurePhysics.netBuoyancyPressure(steel, water, G);
            double archimedes = PressurePhysics.netBuoyancyArchimedes(steel, water, G);
            assertEquals(archimedes, pressure, 1e-6);
        }

        @Test
        @DisplayName("partially submerged: face-sum == Archimedes")
        void partiallySubAgrees() {
            pine.setY(SURF_Y - pine.getHeightPx() / 2);  // half submerged
            double pressure   = PressurePhysics.netBuoyancyPressure(pine, water, G);
            double archimedes = PressurePhysics.netBuoyancyArchimedes(pine, water, G);
            assertEquals(archimedes, pressure, 1e-5);
        }

        @Test
        @DisplayName("above surface: both methods return 0")
        void aboveSurfaceBothZero() {
            pine.setY(SURF_Y - pine.getHeightPx() - 20);
            double pressure   = PressurePhysics.netBuoyancyPressure(pine, water, G);
            double archimedes = PressurePhysics.netBuoyancyArchimedes(pine, water, G);
            assertEquals(0.0, pressure,   1e-9);
            assertEquals(0.0, archimedes, 1e-9);
        }

        @Test
        @DisplayName("mercury fluid: face-sum still == Archimedes")
        void mercuryFluidAgrees() {
            FluidMedium mercury = new FluidMedium(13534.0, SURF_Y);
            double pressure   = PressurePhysics.netBuoyancyPressure(pine, mercury, G);
            double archimedes = PressurePhysics.netBuoyancyArchimedes(pine, mercury, G);
            assertEquals(archimedes, pressure, 1e-5);
        }
    }

    // ── Net downward force ────────────────────────────────────────────────────

    @Nested
    @DisplayName("netDownwardForce")
    class NetDownward {

        @Test
        @DisplayName("pine (light) has negative net-down force → rises")
        void pineRises() {
            assertTrue(PressurePhysics.netDownwardForce(pine, water, G) < 0);
        }

        @Test
        @DisplayName("steel (heavy) has positive net-down force → sinks")
        void steelSinks() {
            assertTrue(PressurePhysics.netDownwardForce(steel, water, G) > 0);
        }
    }

    // ── Equilibrium ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("equilibriumY")
    class Equilibrium {

        @Test
        @DisplayName("steel (ρ > ρ_fluid) returns NaN")
        void denseBodyNaN() {
            assertTrue(Double.isNaN(PressurePhysics.equilibriumY(steel, water)));
        }

        @Test
        @DisplayName("pine (ρ < ρ_fluid) returns finite y above surface")
        void lightBodyFiniteAboveSurface() {
            double eqY = PressurePhysics.equilibriumY(pine, water);
            assertFalse(Double.isNaN(eqY));
            assertTrue(eqY < SURF_Y);
        }
    }

    // ── pressureAtPixelY ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("pressureAtPixelY")
    class PressureAtPixel {

        @Test
        @DisplayName("at surface pixel → pressure = 0")
        void atSurfaceZero() {
            assertEquals(0.0, PressurePhysics.pressureAtPixelY(SURF_Y, water, G), 1e-9);
        }

        @Test
        @DisplayName("above surface → pressure = 0")
        void aboveSurfaceZero() {
            assertEquals(0.0, PressurePhysics.pressureAtPixelY(SURF_Y - 50, water, G), 1e-9);
        }

        @Test
        @DisplayName("below surface → pressure > 0 and increases with depth")
        void deeperMorePressure() {
            double p1 = PressurePhysics.pressureAtPixelY(SURF_Y + 100, water, G);
            double p2 = PressurePhysics.pressureAtPixelY(SURF_Y + 200, water, G);
            assertTrue(p1 > 0);
            assertTrue(p2 > p1);
        }

        @Test
        @DisplayName("pressure scales linearly with depth")
        void linearWithDepth() {
            double p1 = PressurePhysics.pressureAtPixelY(SURF_Y + 100, water, G);
            double p2 = PressurePhysics.pressureAtPixelY(SURF_Y + 200, water, G);
            assertEquals(p2, p1 * 2.0, 1e-6);
        }

        @Test
        @DisplayName("pressure matches ρ × g × depthM")
        void matchesFormula() {
            double depthPx = 150;
            double depthM  = depthPx / PPM;
            double expected = water.getDensityKgM3() * G * depthM;
            assertEquals(expected,
                    PressurePhysics.pressureAtPixelY(SURF_Y + depthPx, water, G), 1e-6);
        }
    }

    // ── FluidMedium ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("FluidMedium")
    class FluidMediumTests {

        @Test
        @DisplayName("pressureAt above surface returns 0")
        void aboveSurfaceZero() {
            assertEquals(0.0, water.pressureAt(SURF_Y - 1, G, PPM), 1e-9);
        }

        @Test
        @DisplayName("pressureAt floor equals maxPressure")
        void floorEqualsMax() {
            double p    = water.pressureAt(FLOOR_Y, G, PPM);
            double maxP = water.maxPressure(FLOOR_Y, G, PPM);
            assertEquals(maxP, p, 1e-9);
        }
    }
}
