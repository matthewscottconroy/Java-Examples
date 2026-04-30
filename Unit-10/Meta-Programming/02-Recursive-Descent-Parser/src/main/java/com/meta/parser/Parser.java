package com.meta.parser;

/**
 * Recursive descent parser for arithmetic expressions.
 *
 * <p>The grammar (EBNF):
 * <pre>
 *   expr   = term   (('+' | '-') term)*
 *   term   = factor (('*' | '/') factor)*
 *   factor = NUMBER | '(' expr ')' | '-' factor
 * </pre>
 *
 * <p>Each grammar rule becomes one private method. A rule that references
 * another rule calls that method — the mutual call structure mirrors the
 * grammar's structure exactly. Operator precedence falls out naturally:
 * lower-precedence operators are handled higher in the call stack.
 *
 * <p>The parser is a single-pass, hand-written LL(1) parser — it reads
 * one character of lookahead and never backtracks.
 */
public class Parser {

    private final String input;
    private int pos;

    private Parser(String input) {
        this.input = input.replaceAll("\\s+", "");  // strip whitespace
        this.pos = 0;
    }

    public static Expr parse(String expression) {
        Parser p = new Parser(expression);
        Expr result = p.expr();
        if (p.pos < p.input.length()) {
            throw new ParseException("Unexpected character '" + p.input.charAt(p.pos)
                + "' at position " + p.pos);
        }
        return result;
    }

    // -----------------------------------------------------------------
    // Grammar rules — each method handles one precedence level
    // -----------------------------------------------------------------

    // expr = term (('+' | '-') term)*
    private Expr expr() {
        Expr left = term();
        while (pos < input.length() && (peek() == '+' || peek() == '-')) {
            char op = consume();
            left = new Expr.BinOp(op, left, term());
        }
        return left;
    }

    // term = factor (('*' | '/') factor)*
    private Expr term() {
        Expr left = factor();
        while (pos < input.length() && (peek() == '*' || peek() == '/')) {
            char op = consume();
            left = new Expr.BinOp(op, left, factor());
        }
        return left;
    }

    // factor = NUMBER | '(' expr ')' | '-' factor
    private Expr factor() {
        if (peek() == '(') {
            consume();          // '('
            Expr inner = expr();
            expect(')');
            return inner;
        }
        if (peek() == '-') {
            consume();
            return new Expr.Neg(factor());
        }
        return number();
    }

    // NUMBER = [0-9]+ ('.' [0-9]+)?
    private Expr number() {
        int start = pos;
        while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
            pos++;
        }
        if (start == pos) {
            throw new ParseException("Expected number at position " + pos
                + (pos < input.length() ? ", got '" + input.charAt(pos) + "'" : " (end of input)"));
        }
        return new Expr.Num(Double.parseDouble(input.substring(start, pos)));
    }

    // -----------------------------------------------------------------
    // Lexer helpers
    // -----------------------------------------------------------------

    private char peek() {
        if (pos >= input.length()) throw new ParseException("Unexpected end of input");
        return input.charAt(pos);
    }

    private char consume() { return input.charAt(pos++); }

    private void expect(char c) {
        if (pos >= input.length() || input.charAt(pos) != c) {
            throw new ParseException("Expected '" + c + "' at position " + pos);
        }
        pos++;
    }

    public static class ParseException extends RuntimeException {
        public ParseException(String msg) { super(msg); }
    }
}
