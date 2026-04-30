package com.meta.mutual;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MutualRecursionTest {

    @Test @DisplayName("isEven: 0 is even")
    void isEven_zero() { assertTrue(MutualRecursion.isEven(0)); }

    @Test @DisplayName("isEven: 1 is not even")
    void isEven_one() { assertFalse(MutualRecursion.isEven(1)); }

    @Test @DisplayName("isEven: even numbers")
    void isEven_evens() {
        for (int n : new int[]{ 0, 2, 4, 10, 100 })
            assertTrue(MutualRecursion.isEven(n), n + " should be even");
    }

    @Test @DisplayName("isOdd: odd numbers")
    void isOdd_odds() {
        for (int n : new int[]{ 1, 3, 5, 11, 99 })
            assertTrue(MutualRecursion.isOdd(n), n + " should be odd");
    }

    @Test @DisplayName("isEven and isOdd are complementary")
    void evenOdd_complementary() {
        for (int n = 0; n <= 20; n++)
            assertNotEquals(MutualRecursion.isEven(n), MutualRecursion.isOdd(n),
                "isEven and isOdd must differ at n=" + n);
    }

    @Test @DisplayName("Tokeniser: numbers are recognised")
    void tokeniser_numbers() {
        List<MutualRecursion.Token> tokens = MutualRecursion.tokenise("3.14 42");
        assertEquals(2, tokens.size());
        assertInstanceOf(MutualRecursion.Token.Num.class, tokens.get(0));
        assertInstanceOf(MutualRecursion.Token.Num.class, tokens.get(1));
        assertEquals(3.14, ((MutualRecursion.Token.Num) tokens.get(0)).value(), 1e-9);
    }

    @Test @DisplayName("Tokeniser: words are recognised")
    void tokeniser_words() {
        List<MutualRecursion.Token> tokens = MutualRecursion.tokenise("sin cos");
        assertEquals(2, tokens.size());
        assertInstanceOf(MutualRecursion.Token.Word.class, tokens.get(0));
        assertEquals("sin", ((MutualRecursion.Token.Word) tokens.get(0)).text());
    }

    @Test @DisplayName("Tokeniser: operators are recognised")
    void tokeniser_operators() {
        List<MutualRecursion.Token> tokens = MutualRecursion.tokenise("+ - * /");
        assertEquals(4, tokens.size());
        assertInstanceOf(MutualRecursion.Token.Op.class, tokens.get(0));
    }

    @Test @DisplayName("Tokeniser: mixed expression tokenises correctly")
    void tokeniser_mixed() {
        List<MutualRecursion.Token> tokens = MutualRecursion.tokenise("2 + foo * 3");
        assertEquals(5, tokens.size());
    }

    @Test @DisplayName("Hofstadter: M(0) = 0")
    void hofstadter_M0() { assertEquals(0, MutualRecursion.hofstadterM(0)); }

    @Test @DisplayName("Hofstadter: F(0) = 1")
    void hofstadter_F0() { assertEquals(1, MutualRecursion.hofstadterF(0)); }

    @Test @DisplayName("Hofstadter: known M sequence values")
    void hofstadter_M_knownValues() {
        // M: 0, 0, 1, 2, 2, 3, 4, 4, 5, 6
        int[] expected = { 0, 0, 1, 2, 2, 3, 4, 4, 5, 6 };
        for (int n = 0; n < expected.length; n++)
            assertEquals(expected[n], MutualRecursion.hofstadterM(n), "M(" + n + ")");
    }

    @Test @DisplayName("Hofstadter: known F sequence values")
    void hofstadter_F_knownValues() {
        // F: 1, 1, 2, 2, 3, 3, 4, 5, 5, 6
        int[] expected = { 1, 1, 2, 2, 3, 3, 4, 5, 5, 6 };
        for (int n = 0; n < expected.length; n++)
            assertEquals(expected[n], MutualRecursion.hofstadterF(n), "F(" + n + ")");
    }

    @Test @DisplayName("Balanced: empty string is balanced")
    void balanced_empty() { assertTrue(MutualRecursion.isBalanced("")); }

    @Test @DisplayName("Balanced: () is balanced")
    void balanced_single() { assertTrue(MutualRecursion.isBalanced("()")); }

    @Test @DisplayName("Balanced: nested parens are balanced")
    void balanced_nested() {
        assertTrue(MutualRecursion.isBalanced("(())"));
        assertTrue(MutualRecursion.isBalanced("((()))"));
    }

    @Test @DisplayName("Balanced: sequential pairs are balanced")
    void balanced_sequential() {
        assertTrue(MutualRecursion.isBalanced("()()"));
        assertTrue(MutualRecursion.isBalanced("()()()"));
    }

    @Test @DisplayName("Balanced: complex balanced expression")
    void balanced_complex() {
        assertTrue(MutualRecursion.isBalanced("(()(()))"));
    }

    @Test @DisplayName("Unbalanced: single open paren")
    void unbalanced_open() { assertFalse(MutualRecursion.isBalanced("(")); }

    @Test @DisplayName("Unbalanced: reversed paren")
    void unbalanced_reversed() { assertFalse(MutualRecursion.isBalanced(")(")); }

    @Test @DisplayName("Unbalanced: unclosed nested")
    void unbalanced_unclosed() { assertFalse(MutualRecursion.isBalanced("(()")); }
}
