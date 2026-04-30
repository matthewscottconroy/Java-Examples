package com.meta.parser;

/**
 * Abstract syntax tree (AST) for arithmetic expressions.
 *
 * <p>Each grammar rule maps directly to one variant of this sealed type:
 * <pre>
 *   expr   = term   (('+' | '-') term)*
 *   term   = factor (('*' | '/') factor)*
 *   factor = NUMBER | '(' expr ')' | '-' factor
 * </pre>
 */
public sealed interface Expr permits Expr.Num, Expr.Neg, Expr.BinOp {

    record Num(double value) implements Expr {}
    record Neg(Expr operand) implements Expr {}
    record BinOp(char op, Expr left, Expr right) implements Expr {}

    /** Evaluates the expression tree to a double. */
    default double eval() {
        return switch (this) {
            case Num(var v)               -> v;
            case Neg(var e)               -> -e.eval();
            case BinOp(var op, var l, var r) when op == '+' -> l.eval() + r.eval();
            case BinOp(var op, var l, var r) when op == '-' -> l.eval() - r.eval();
            case BinOp(var op, var l, var r) when op == '*' -> l.eval() * r.eval();
            case BinOp(var op, var l, var r) when op == '/' -> l.eval() / r.eval();
            case BinOp(var op, var l, var r) ->
                throw new IllegalStateException("Unknown operator: " + op);
        };
    }

    /** Pretty-prints the expression with minimal parentheses. */
    default String pretty() {
        return switch (this) {
            case Num(var v)  -> v == (long) v ? String.valueOf((long) v) : String.valueOf(v);
            case Neg(var e)  -> "-" + e.pretty();
            case BinOp(var op, var l, var r) -> {
                String ls = (l instanceof BinOp lb && precedence(lb.op()) < precedence(op))
                    ? "(" + l.pretty() + ")" : l.pretty();
                String rs = (r instanceof BinOp rb && precedence(rb.op()) < precedence(op))
                    ? "(" + r.pretty() + ")" : r.pretty();
                yield ls + " " + op + " " + rs;
            }
        };
    }

    private static int precedence(char op) {
        return (op == '+' || op == '-') ? 1 : 2;
    }
}
