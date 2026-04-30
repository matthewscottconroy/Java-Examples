package com.evolutionary.multiobjective;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NSGA2Test {

    // ---------------------------------------------------------------
    // Dominance
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Domination: strictly better on one objective, equal on other")
    void dominates_strictlyBetterOnOne() {
        Solution a = makeSolution(1.0, 2.0);
        Solution b = makeSolution(1.0, 3.0);
        assertTrue(a.dominates(b));
        assertFalse(b.dominates(a));
    }

    @Test
    @DisplayName("Domination: equal objectives → neither dominates")
    void dominates_equal() {
        Solution a = makeSolution(2.0, 2.0);
        Solution b = makeSolution(2.0, 2.0);
        assertFalse(a.dominates(b));
        assertFalse(b.dominates(a));
    }

    @Test
    @DisplayName("Domination: a better on obj1, b better on obj2 → neither dominates")
    void dominates_tradeOff() {
        Solution a = makeSolution(1.0, 5.0);
        Solution b = makeSolution(3.0, 1.0);
        assertFalse(a.dominates(b));
        assertFalse(b.dominates(a));
    }

    @Test
    @DisplayName("Domination: a strictly better on all objectives")
    void dominates_allObjectivesBetter() {
        Solution a = makeSolution(1.0, 1.0);
        Solution b = makeSolution(3.0, 4.0);
        assertTrue(a.dominates(b));
        assertFalse(b.dominates(a));
    }

    // ---------------------------------------------------------------
    // Non-dominated sorting
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Non-dominated sort: 3 solutions on Pareto front (trade-offs)")
    void nonDominatedSort_allOnParetoFront() {
        // All three are trade-offs — none dominates another
        List<Solution> pop = List.of(
            makeSolution(1.0, 5.0),
            makeSolution(3.0, 3.0),
            makeSolution(5.0, 1.0)
        );
        NSGA2 nsga2 = makeEngine();
        List<List<Solution>> fronts = nsga2.nonDominatedSort(new java.util.ArrayList<>(pop));
        assertEquals(1, fronts.size(), "All solutions form a single Pareto front");
        assertEquals(3, fronts.get(0).size());
    }

    @Test
    @DisplayName("Non-dominated sort: dominated solution goes to rank 1")
    void nonDominatedSort_oneDominatedSolution() {
        Solution pareto  = makeSolution(1.0, 1.0);   // dominates inferior
        Solution inferior = makeSolution(3.0, 3.0);  // dominated by pareto
        Solution tradeOff = makeSolution(0.5, 2.0);  // on pareto front with pareto

        List<Solution> pop = new java.util.ArrayList<>(List.of(pareto, inferior, tradeOff));
        NSGA2 nsga2 = makeEngine();
        List<List<Solution>> fronts = nsga2.nonDominatedSort(pop);

        assertEquals(2, fronts.size(), "Two fronts: pareto front and dominated set");
        assertEquals(2, fronts.get(0).size(), "Two solutions on rank-0 front");
        assertEquals(1, fronts.get(1).size(), "One dominated solution on rank-1 front");
    }

    // ---------------------------------------------------------------
    // Crowding distance
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Crowding distance: boundary solutions get infinity")
    void crowdingDistance_boundaryIsInfinity() {
        List<Solution> front = Arrays.asList(
            makeSolution(1.0, 5.0),
            makeSolution(3.0, 3.0),
            makeSolution(5.0, 1.0)
        );
        List<List<Solution>> fronts = new java.util.ArrayList<>();
        fronts.add(front);
        NSGA2 nsga2 = makeEngine();
        nsga2.assignCrowdingDistances(fronts);

        long infCount = fronts.get(0).stream()
            .filter(s -> s.crowdingDistance() == Double.MAX_VALUE)
            .count();
        assertEquals(2, infCount, "Two boundary solutions should have max crowding distance");
    }

    @Test
    @DisplayName("Crowding distance: interior solution gets finite distance")
    void crowdingDistance_interiorIsFinite() {
        List<Solution> front = Arrays.asList(
            makeSolution(1.0, 5.0),
            makeSolution(3.0, 3.0),
            makeSolution(5.0, 1.0)
        );
        List<List<Solution>> fronts = new java.util.ArrayList<>();
        fronts.add(front);
        NSGA2 nsga2 = makeEngine();
        nsga2.assignCrowdingDistances(fronts);

        long finiteCount = fronts.get(0).stream()
            .filter(s -> s.crowdingDistance() > 0 && s.crowdingDistance() < Double.MAX_VALUE)
            .count();
        assertEquals(1, finiteCount, "One interior solution should have finite distance > 0");
    }

    // ---------------------------------------------------------------
    // Full NSGA-II run
    // ---------------------------------------------------------------

    @Test
    @DisplayName("NSGA-II on ZDT1-inspired bi-objective: Pareto front is non-empty and valid")
    void fullRun_paretoFrontIsNonDominated() {
        int dims = 4;
        double[] lower = new double[dims];
        double[] upper = new double[dims];
        Arrays.fill(upper, 1.0);

        // ZDT1-style: f1=x[0], f2 = 1 - sqrt(x[0]/g), g = 1 + 9/(n-1)*sum(x[1..n-1])
        NSGA2.Config config = new NSGA2.Config(
            50, 100, 0.9, 0.1, 0.05, lower, upper, 0L
        );
        NSGA2 nsga2 = new NSGA2(config, genes -> {
            double f1 = genes[0];
            double g = 1 + (9.0 / (dims - 1)) * Arrays.stream(genes, 1, dims).sum();
            double f2 = g * (1 - Math.sqrt(f1 / g));
            return new double[]{f1, f2};
        });

        List<Solution> front = nsga2.run();

        assertFalse(front.isEmpty(), "Pareto front must be non-empty");

        // Verify no solution in the front dominates another
        for (int i = 0; i < front.size(); i++) {
            for (int j = 0; j < front.size(); j++) {
                if (i != j) {
                    assertFalse(front.get(i).dominates(front.get(j)),
                        "Pareto front must be non-dominated: solution " + i + " dominates " + j);
                }
            }
        }
    }

    @Test
    @DisplayName("NSGA-II: cheaper solution has higher unreliability (trade-off verified)")
    void fullRun_costVsReliabilityTradeOff() {
        int dims = 4;
        double[] lower = new double[dims];
        double[] upper = new double[dims];
        Arrays.fill(upper, 1.0);

        NSGA2.Config config = new NSGA2.Config(
            60, 150, 0.9, 0.1, 0.05, lower, upper, 1L
        );
        NSGA2 nsga2 = new NSGA2(config, genes -> {
            double cost = Arrays.stream(genes).map(q -> 10 * q * q + q).sum();
            double sysRel = Arrays.stream(genes).map(q -> 0.5 + 0.5 * q).reduce(1.0, (a, b) -> a * b);
            return new double[]{cost, 1 - sysRel};
        });

        List<Solution> front = nsga2.run();
        front.sort(Comparator.comparingDouble(s -> s.objectives()[0]));

        Solution cheapest = front.get(0);
        Solution expensive = front.get(front.size() - 1);

        assertTrue(cheapest.objectives()[0] < expensive.objectives()[0],
            "Cheapest solution must have lower cost");
        assertTrue(cheapest.objectives()[1] > expensive.objectives()[1],
            "Cheapest solution must have higher unreliability (trade-off)");
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private Solution makeSolution(double obj1, double obj2) {
        return new Solution(new double[]{0.0}, new double[]{obj1, obj2}, 0, 0.0);
    }

    private NSGA2 makeEngine() {
        double[] lower = {0.0};
        double[] upper = {1.0};
        NSGA2.Config config = new NSGA2.Config(10, 1, 0.9, 0.1, 0.05, lower, upper, 0L);
        return new NSGA2(config, genes -> new double[]{genes[0], 1 - genes[0]});
    }
}
