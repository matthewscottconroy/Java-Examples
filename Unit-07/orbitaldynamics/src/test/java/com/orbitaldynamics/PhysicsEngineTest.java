package com.orbitaldynamics;

import com.orbitaldynamics.math.Vector2D;
import com.orbitaldynamics.sim.body.OrbitalBody;
import com.orbitaldynamics.sim.physics.PhysicsEngine;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PhysicsEngine}.
 *
 * <p>Tests cover gravitational attraction, conservation laws, collision modes,
 * pinned body behavior, and the preset scenario builders.
 */
@DisplayName("PhysicsEngine")
class PhysicsEngineTest {

    private static final double EPS = 1e-6;

    private static OrbitalBody body(double x, double y, double vx, double vy, double r) {
        return new OrbitalBody(new Vector2D(x, y), new Vector2D(vx, vy), r, 1.0, 0, 0);
    }

    // ── Gravity ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("gravitational attraction")
    class Gravity {

        @Test
        @DisplayName("two bodies attract each other (move closer)")
        void twoBodyAttract() {
            PhysicsEngine engine = new PhysicsEngine(1000.0);
            engine.setElasticCollisions(false);

            OrbitalBody a = body(-200, 0, 0, 0, 10);
            OrbitalBody b = body( 200, 0, 0, 0, 10);
            List<OrbitalBody> bodies = List.of(a, b);

            double initDist = a.getPosition().distanceTo(b.getPosition());
            engine.step(new ArrayList<>(bodies), 0.5);
            double finalDist = a.getPosition().distanceTo(b.getPosition());

            assertTrue(finalDist < initDist, "bodies should attract: distance should decrease");
        }

        @Test
        @DisplayName("single body does not move (no self-gravity)")
        void singleBodyStationary() {
            PhysicsEngine engine = new PhysicsEngine();
            OrbitalBody b = body(100, 200, 0, 0, 15);
            List<OrbitalBody> bodies = new ArrayList<>();
            bodies.add(b);
            Vector2D before = b.getPosition();
            engine.step(bodies, 0.016);
            assertEquals(before.x(), b.getPosition().x(), EPS);
            assertEquals(before.y(), b.getPosition().y(), EPS);
        }

        @Test
        @DisplayName("pinned body does not move under gravity")
        void pinnedBodyFixed() {
            PhysicsEngine engine = new PhysicsEngine(5000.0);
            engine.setElasticCollisions(false);

            OrbitalBody star = body(0, 0, 0, 0, 50);
            star.setPinned(true);
            OrbitalBody planet = body(300, 0, 0, 60, 15);

            List<OrbitalBody> bodies = new ArrayList<>();
            bodies.add(star);
            bodies.add(planet);

            Vector2D starBefore = star.getPosition();
            engine.step(bodies, 0.1);
            assertEquals(starBefore.x(), star.getPosition().x(), EPS);
            assertEquals(starBefore.y(), star.getPosition().y(), EPS);
        }

        @Test
        @DisplayName("pinned body still attracts free body")
        void pinnedBodyAttracts() {
            PhysicsEngine engine = new PhysicsEngine(5000.0);
            engine.setElasticCollisions(false);

            OrbitalBody star = body(0, 0, 0, 0, 50);
            star.setPinned(true);
            OrbitalBody planet = body(300, 0, 0, 0, 15);  // stationary planet

            List<OrbitalBody> bodies = new ArrayList<>();
            bodies.add(star);
            bodies.add(planet);

            engine.step(bodies, 0.5);
            // Planet should have moved toward star (x decreased from 300 toward 0)
            assertTrue(planet.getPosition().x() < 300.0, "planet should be pulled toward star");
        }

        @Test
        @DisplayName("empty body list does not throw")
        void emptyListSafe() {
            PhysicsEngine engine = new PhysicsEngine();
            assertDoesNotThrow(() -> engine.step(new ArrayList<>(), 0.016));
        }
    }

    // ── Conservation laws ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("conservation laws (isolated system, no collisions)")
    class Conservation {

        @Test
        @DisplayName("center of mass stays fixed for two equal-mass bodies")
        void centerOfMassConserved() {
            PhysicsEngine engine = new PhysicsEngine(5000.0);
            engine.setElasticCollisions(false);

            OrbitalBody a = body(-200, 0,  0, 50, 20);
            OrbitalBody b = body( 200, 0,  0, -50, 20);
            List<OrbitalBody> bodies = new ArrayList<>();
            bodies.add(a);
            bodies.add(b);

            Vector2D comBefore = engine.centerOfMass(bodies);
            for (int i = 0; i < 50; i++) engine.step(bodies, 0.05);
            Vector2D comAfter = engine.centerOfMass(bodies);

            assertEquals(comBefore.x(), comAfter.x(), 0.5);
            assertEquals(comBefore.y(), comAfter.y(), 0.5);
        }

        @Test
        @DisplayName("angular momentum approximately conserved over short orbit")
        void angularMomentumConserved() {
            PhysicsEngine engine = new PhysicsEngine(5000.0);
            engine.setElasticCollisions(false);

            // Circular orbit: star pinned at origin, planet orbiting
            OrbitalBody star = body(0, 0, 0, 0, 50);
            star.setPinned(true);
            // v_circ = sqrt(G*M/r), but M=density*pi*r_star^2, G=5000
            double M  = star.getMass();
            double r  = 300.0;
            double vc = Math.sqrt(5000.0 * M / r);
            OrbitalBody planet = body(r, 0, 0, vc, 10);

            List<OrbitalBody> bodies = new ArrayList<>();
            bodies.add(star);
            bodies.add(planet);

            double L0 = engine.totalAngularMomentum(bodies);
            for (int i = 0; i < 20; i++) engine.step(bodies, 0.05);
            double L1 = engine.totalAngularMomentum(bodies);

            // Angular momentum should be conserved to within a few percent
            assertEquals(L0, L1, Math.abs(L0) * 0.05 + 1.0);
        }
    }

    // ── Collision response ────────────────────────────────────────────────────

    @Nested
    @DisplayName("collision response")
    class Collisions {

        @Test
        @DisplayName("elastic collision: overlapping bodies separate")
        void elasticSeparates() {
            PhysicsEngine engine = new PhysicsEngine(0);  // no gravity
            engine.setElasticCollisions(true);
            engine.setMergeOnCollision(false);

            OrbitalBody a = body(-5, 0,  50, 0, 15);
            OrbitalBody b = body( 5, 0, -50, 0, 15);  // head-on, already overlapping
            List<OrbitalBody> bodies = new ArrayList<>();
            bodies.add(a);
            bodies.add(b);

            engine.step(bodies, 0.001);
            double dist = a.getPosition().distanceTo(b.getPosition());
            assertTrue(dist >= 30.0 - 0.5, "bodies should not overlap after collision");
        }

        @Test
        @DisplayName("merge: smaller body is removed and absorbed")
        void mergeRemovesAbsorbed() {
            PhysicsEngine engine = new PhysicsEngine(0);
            engine.setElasticCollisions(false);
            engine.setMergeOnCollision(true);

            OrbitalBody big   = body(-5, 0,  20, 0, 20);
            OrbitalBody small = body( 5, 0, -20, 0, 10);  // overlapping
            List<OrbitalBody> bodies = new ArrayList<>();
            bodies.add(big);
            bodies.add(small);

            engine.step(bodies, 0.001);
            List<OrbitalBody> removed = engine.drainMergeRemovals();
            assertFalse(removed.isEmpty(), "merge should produce a removal");
        }

        @Test
        @DisplayName("merge: drainMergeRemovals clears after drain")
        void drainClearsRemovals() {
            PhysicsEngine engine = new PhysicsEngine(0);
            engine.setMergeOnCollision(true);
            engine.setElasticCollisions(false);

            OrbitalBody a = body(-5, 0,  20, 0, 20);
            OrbitalBody b = body( 5, 0, -20, 0, 10);
            engine.step(new ArrayList<>(List.of(a, b)), 0.001);
            engine.drainMergeRemovals();  // drain once
            List<OrbitalBody> second = engine.drainMergeRemovals();
            assertTrue(second.isEmpty(), "second drain should be empty");
        }
    }

    // ── Conservation quantities ────────────────────────────────────────────────

    @Nested
    @DisplayName("energy and momentum helpers")
    class EnergyMomentum {

        @Test
        @DisplayName("totalEnergy returns finite value for two-body system")
        void totalEnergyFinite() {
            PhysicsEngine engine = new PhysicsEngine();
            OrbitalBody a = body(-100, 0, 0, 30, 15);
            OrbitalBody b = body( 100, 0, 0, -30, 15);
            double E = engine.totalEnergy(List.of(a, b));
            assertFalse(Double.isNaN(E));
            assertFalse(Double.isInfinite(E));
        }

        @Test
        @DisplayName("centerOfMass of symmetric two-body system is at origin")
        void comSymmetric() {
            PhysicsEngine engine = new PhysicsEngine();
            OrbitalBody a = body(-100, 0, 0, 0, 15);
            OrbitalBody b = body( 100, 0, 0, 0, 15);
            Vector2D com = engine.centerOfMass(List.of(a, b));
            assertEquals(0.0, com.x(), EPS);
            assertEquals(0.0, com.y(), EPS);
        }

        @Test
        @DisplayName("centerOfMass of empty list is ZERO")
        void comEmpty() {
            PhysicsEngine engine = new PhysicsEngine();
            Vector2D com = engine.centerOfMass(new ArrayList<>());
            assertEquals(0, com.x(), EPS);
            assertEquals(0, com.y(), EPS);
        }
    }

    // ── Preset scenarios ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("preset scenarios")
    class Presets {

        @Test
        @DisplayName("binary stars preset produces exactly 2 bodies")
        void binaryStarsTwoBodies() {
            List<OrbitalBody> bodies = PhysicsEngine.presetBinaryStars(PhysicsEngine.DEFAULT_G);
            assertEquals(2, bodies.size());
        }

        @Test
        @DisplayName("binary stars: bodies equidistant from origin")
        void binaryStarsSymmetric() {
            List<OrbitalBody> bodies = PhysicsEngine.presetBinaryStars(PhysicsEngine.DEFAULT_G);
            double dA = bodies.get(0).getPosition().magnitude();
            double dB = bodies.get(1).getPosition().magnitude();
            assertEquals(dA, dB, EPS);
        }

        @Test
        @DisplayName("star and planets preset produces 3 bodies")
        void starAndPlanets3() {
            List<OrbitalBody> bodies = PhysicsEngine.presetStarAndPlanets(PhysicsEngine.DEFAULT_G);
            assertEquals(3, bodies.size());
        }

        @Test
        @DisplayName("figure-eight preset produces exactly 3 bodies")
        void figureEight3() {
            List<OrbitalBody> bodies = PhysicsEngine.presetFigureEight(PhysicsEngine.DEFAULT_G);
            assertEquals(3, bodies.size());
        }

        @Test
        @DisplayName("all preset bodies have positive mass")
        void presetBodiesPositiveMass() {
            List<OrbitalBody> bodies = PhysicsEngine.presetBinaryStars(PhysicsEngine.DEFAULT_G);
            for (OrbitalBody b : bodies) {
                assertTrue(b.getMass() > 0, b.getName() + " must have positive mass");
            }
        }
    }
}
