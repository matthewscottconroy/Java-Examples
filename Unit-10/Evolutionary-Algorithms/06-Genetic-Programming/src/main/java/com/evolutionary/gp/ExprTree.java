package com.evolutionary.gp;

import java.util.Random;

/**
 * An expression tree for symbolic regression.
 *
 * <p>Each tree represents a mathematical expression. Leaf nodes are
 * constants or the input variable x. Internal nodes are operators
 * (+, -, *, protected division).
 *
 * <p>Trees are immutable. Crossover and mutation produce new trees.
 */
public sealed interface ExprTree permits ExprTree.Const, ExprTree.Var, ExprTree.BinOp {

    double eval(double x);
    int depth();
    String toFormula();
    ExprTree copy();

    // ─── Leaf: constant ─────────────────────────────────────────────────────

    record Const(double value) implements ExprTree {
        public double eval(double x) { return value; }
        public int depth() { return 0; }
        public String toFormula() { return String.format("%.2f", value); }
        public ExprTree copy() { return this; }
    }

    // ─── Leaf: variable x ───────────────────────────────────────────────────

    record Var() implements ExprTree {
        public double eval(double x) { return x; }
        public int depth() { return 0; }
        public String toFormula() { return "x"; }
        public ExprTree copy() { return this; }
    }

    // ─── Internal node: binary operator ─────────────────────────────────────

    record BinOp(char op, ExprTree left, ExprTree right) implements ExprTree {

        public double eval(double x) {
            double l = left.eval(x), r = right.eval(x);
            return switch (op) {
                case '+' -> l + r;
                case '-' -> l - r;
                case '*' -> l * r;
                case '/' -> Math.abs(r) < 1e-9 ? 1.0 : l / r;  // protected division
                default  -> throw new IllegalStateException("Unknown op: " + op);
            };
        }

        public int depth() { return 1 + Math.max(left.depth(), right.depth()); }

        public String toFormula() {
            return "(" + left.toFormula() + " " + op + " " + right.toFormula() + ")";
        }

        public ExprTree copy() { return new BinOp(op, left.copy(), right.copy()); }
    }

    // ─── Factory: random tree generation ────────────────────────────────────

    static final char[] OPS = {'+', '-', '*', '/'};

    /** Generates a random tree up to maxDepth. */
    static ExprTree random(int maxDepth, Random rng) {
        if (maxDepth == 0 || rng.nextBoolean()) return randomLeaf(rng);
        char op = OPS[rng.nextInt(OPS.length)];
        return new BinOp(op, random(maxDepth - 1, rng), random(maxDepth - 1, rng));
    }

    static ExprTree randomLeaf(Random rng) {
        if (rng.nextBoolean()) return new Var();
        double v = rng.nextInt(10) - 5;  // constant in [-5, 5]
        return new Const(v);
    }
}
