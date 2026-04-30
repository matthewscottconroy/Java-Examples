package com.meta.parser;

/**
 * Interactive formula evaluator demonstrating recursive descent parsing.
 *
 * The parser transforms a string like "3 + 4 * (2 - 1)" into an expression
 * tree, then evaluates it. Each step is shown separately so the data flow
 * from text → AST → result is visible.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Recursive Descent Parser — Formula Evaluator ===\n");

        String[] expressions = {
            "1 + 2 + 3",
            "3 + 4 * 2",
            "3 * (4 + 2)",
            "(1 + 2) * (3 + 4)",
            "10 / 2 - 3",
            "-(3 + 4)",
            "2 * 3 + 4 * 5",
            "100 / (2 * 5)",
            "1.5 + 2.5 * 2",
            "((2 + 3) * (4 - 1)) / 3",
        };

        System.out.printf("%-30s  %-30s  %s%n", "Input", "AST (pretty)", "Result");
        System.out.println("-".repeat(75));

        for (String expr : expressions) {
            Expr ast = Parser.parse(expr);
            System.out.printf("%-30s  %-30s  %s%n",
                expr, ast.pretty(), ast.eval());
        }

        System.out.println("\n--- How the grammar rules call each other ---");
        System.out.println("  expr()   handles + and -  (lowest precedence)");
        System.out.println("  term()   handles * and /  (medium precedence)");
        System.out.println("  factor() handles () and unary -  (highest precedence)");
        System.out.println();
        System.out.println("  \"2 + 3 * 4\" parse trace:");
        System.out.println("    expr()");
        System.out.println("      term() -> factor() -> Num(2)");
        System.out.println("      sees '+', calls term()");
        System.out.println("        factor() -> Num(3)");
        System.out.println("        sees '*', calls factor() -> Num(4)");
        System.out.println("        returns BinOp('*', 3, 4)");
        System.out.println("      returns BinOp('+', 2, BinOp('*', 3, 4))");
        System.out.println("    eval() = 2 + 12 = 14");
    }
}
