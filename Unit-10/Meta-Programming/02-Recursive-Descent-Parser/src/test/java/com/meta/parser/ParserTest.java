package com.meta.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private static double eval(String expr) {
        return Parser.parse(expr).eval();
    }

    @Test @DisplayName("Single number")
    void singleNumber() { assertEquals(42.0, eval("42"), 1e-9); }

    @Test @DisplayName("Addition")
    void addition() { assertEquals(5.0, eval("2 + 3"), 1e-9); }

    @Test @DisplayName("Subtraction")
    void subtraction() { assertEquals(1.0, eval("4 - 3"), 1e-9); }

    @Test @DisplayName("Multiplication")
    void multiplication() { assertEquals(12.0, eval("3 * 4"), 1e-9); }

    @Test @DisplayName("Division")
    void division() { assertEquals(2.5, eval("5 / 2"), 1e-9); }

    @Test @DisplayName("Precedence: * before +")
    void precedence_multiplyBeforeAdd() {
        assertEquals(14.0, eval("2 + 3 * 4"), 1e-9);
    }

    @Test @DisplayName("Precedence: * before - ")
    void precedence_multiplyBeforeSubtract() {
        assertEquals(2.0, eval("8 - 3 * 2"), 1e-9);
    }

    @Test @DisplayName("Parentheses override precedence")
    void parentheses() {
        assertEquals(20.0, eval("(2 + 3) * 4"), 1e-9);
    }

    @Test @DisplayName("Nested parentheses")
    void nestedParentheses() {
        assertEquals(15.0, eval("((2 + 3) * (4 - 1))"), 1e-9);
    }

    @Test @DisplayName("Unary negation")
    void unaryNegation() {
        assertEquals(-5.0, eval("-5"), 1e-9);
    }

    @Test @DisplayName("Unary negation of expression")
    void unaryNegationExpr() {
        assertEquals(-7.0, eval("-(3 + 4)"), 1e-9);
    }

    @Test @DisplayName("Left-associativity of subtraction")
    void leftAssociativity() {
        // 10 - 3 - 2 = (10 - 3) - 2 = 5, not 10 - (3 - 2) = 9
        assertEquals(5.0, eval("10 - 3 - 2"), 1e-9);
    }

    @Test @DisplayName("Left-associativity of division")
    void leftAssociativity_division() {
        // 8 / 4 / 2 = (8 / 4) / 2 = 1
        assertEquals(1.0, eval("8 / 4 / 2"), 1e-9);
    }

    @Test @DisplayName("Floating point numbers")
    void floatingPoint() {
        assertEquals(4.0, eval("1.5 + 2.5"), 1e-9);
    }

    @Test @DisplayName("Whitespace is ignored")
    void whitespaceIgnored() {
        assertEquals(eval("1+2*3"), eval("1 + 2 * 3"), 1e-9);
    }

    @Test @DisplayName("Complex expression")
    void complex() {
        // ((2 + 3) * (4 - 1)) / 3 = (5 * 3) / 3 = 5
        assertEquals(5.0, eval("((2 + 3) * (4 - 1)) / 3"), 1e-9);
    }

    @Test @DisplayName("AST: parse produces BinOp for addition")
    void ast_binOp() {
        Expr e = Parser.parse("1 + 2");
        assertInstanceOf(Expr.BinOp.class, e);
        assertEquals('+', ((Expr.BinOp) e).op());
    }

    @Test @DisplayName("AST: nested BinOp for 2 + 3 * 4")
    void ast_nested() {
        Expr e = Parser.parse("2 + 3 * 4");
        assertInstanceOf(Expr.BinOp.class, e);
        Expr.BinOp add = (Expr.BinOp) e;
        assertEquals('+', add.op());
        assertInstanceOf(Expr.BinOp.class, add.right());  // 3*4 is the right subtree
    }

    @Test @DisplayName("pretty() reproduces evaluatable string")
    void pretty_roundTrip() {
        String expr = "2 + 3 * 4";
        Expr ast = Parser.parse(expr);
        double original = ast.eval();
        double reparsed = Parser.parse(ast.pretty()).eval();
        assertEquals(original, reparsed, 1e-9);
    }

    @Test @DisplayName("Parse error on trailing garbage")
    void parseError_trailing() {
        assertThrows(Parser.ParseException.class, () -> Parser.parse("1 + 2 @"));
    }

    @Test @DisplayName("Parse error on empty input")
    void parseError_empty() {
        assertThrows(Parser.ParseException.class, () -> Parser.parse(""));
    }

    @Test @DisplayName("Parse error on mismatched parenthesis")
    void parseError_mismatchedParen() {
        assertThrows(Parser.ParseException.class, () -> Parser.parse("(1 + 2"));
    }
}
