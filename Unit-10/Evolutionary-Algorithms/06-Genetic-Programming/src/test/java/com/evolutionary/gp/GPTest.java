package com.evolutionary.gp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GPTest {

    @Test
    @DisplayName("ExprTree.Const evaluates to its value")
    void constEval() {
        ExprTree t = new ExprTree.Const(3.14);
        assertEquals(3.14, t.eval(0), 1e-9);
        assertEquals(3.14, t.eval(99), 1e-9);
    }

    @Test
    @DisplayName("ExprTree.Var evaluates to the input x")
    void varEval() {
        ExprTree t = new ExprTree.Var();
        assertEquals(5.0, t.eval(5.0), 1e-9);
        assertEquals(-2.0, t.eval(-2.0), 1e-9);
    }

    @Test
    @DisplayName("BinOp addition evaluates correctly")
    void addition() {
        // x + 3
        ExprTree t = new ExprTree.BinOp('+', new ExprTree.Var(), new ExprTree.Const(3));
        assertEquals(8.0, t.eval(5.0), 1e-9);
    }

    @Test
    @DisplayName("Protected division by near-zero returns 1")
    void protectedDivision() {
        ExprTree t = new ExprTree.BinOp('/', new ExprTree.Const(10), new ExprTree.Const(0));
        assertEquals(1.0, t.eval(0), 1e-9);
    }

    @Test
    @DisplayName("Depth of leaf is 0; depth of tree increases correctly")
    void depth() {
        ExprTree leaf = new ExprTree.Var();
        assertEquals(0, leaf.depth());
        ExprTree node = new ExprTree.BinOp('+', leaf, leaf);
        assertEquals(1, node.depth());
        ExprTree deep = new ExprTree.BinOp('*', node, node);
        assertEquals(2, deep.depth());
    }

    @Test
    @DisplayName("GP reduces MSE on a linear dataset (y = 2x)")
    void gpReducesMSE() {
        List<GPEngine.DataPoint> data = java.util.stream.IntStream.rangeClosed(-5, 5)
            .mapToObj(i -> new GPEngine.DataPoint(i, 2.0 * i))
            .toList();

        GPEngine gp = new GPEngine(100, 4, 0.85, 0.1, 5, 1L);
        ExprTree best = gp.evolve(data, 50);
        double mse = -gp.fitness(best, data);
        assertTrue(mse < 5.0, "MSE should be below 5 for linear data; got " + mse);
    }

    @Test
    @DisplayName("GP finds near-perfect fit for quadratic (y = x² - 2x + 1)")
    void gpQuadratic() {
        List<GPEngine.DataPoint> data = Main.generateData();
        GPEngine gp = new GPEngine(200, 5, 0.85, 0.1, 7, 42L);
        ExprTree best = gp.evolve(data, 100);
        double mse = -gp.fitness(best, data);
        assertTrue(mse < 1.0, "GP should find a good fit for the quadratic; MSE=" + mse);
    }

    @Test
    @DisplayName("Random tree has depth within bound")
    void randomTreeDepth() {
        Random rng = new Random(0);
        for (int i = 0; i < 50; i++) {
            ExprTree t = ExprTree.random(4, rng);
            assertTrue(t.depth() <= 4, "Tree depth " + t.depth() + " exceeds max 4");
        }
    }
}
