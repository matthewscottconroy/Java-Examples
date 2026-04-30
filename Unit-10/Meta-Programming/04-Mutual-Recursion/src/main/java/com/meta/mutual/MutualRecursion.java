package com.meta.mutual;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates mutual recursion — two or more functions that call each other.
 *
 * <p>Mutual recursion arises naturally when problems decompose into interleaved
 * sub-problems of different kinds. The key insight is that the set of functions
 * forms a <em>single</em> recursive definition, even though no individual
 * function calls itself directly.
 *
 * <p>Examples here:
 * <ol>
 *   <li><b>Even/Odd classification</b> — the simplest mutual pair.</li>
 *   <li><b>Recursive descent tokeniser</b> — scanNumber and scanOperator
 *       alternate based on the current character class.</li>
 *   <li><b>Hofstadter sequences</b> — M and F sequences from GEB; each
 *       term is defined in terms of the other sequence.</li>
 *   <li><b>Parenthesis grammar</b> — recognising balanced brackets with
 *       a mutually recursive grammar.</li>
 * </ol>
 */
public class MutualRecursion {

    // -----------------------------------------------------------------
    // 1. Even / Odd — canonical mutual pair
    // -----------------------------------------------------------------

    static boolean isEven(int n) {
        if (n < 0) return isEven(-n);
        return n == 0 || isOdd(n - 1);
    }

    static boolean isOdd(int n) {
        if (n < 0) return isOdd(-n);
        return n != 0 && isEven(n - 1);
    }

    // -----------------------------------------------------------------
    // 2. Tokeniser — scanNumber and scanWord alternate
    // -----------------------------------------------------------------

    sealed interface Token permits Token.Num, Token.Word, Token.Op {
        record Num(double value)  implements Token {}
        record Word(String text)  implements Token {}
        record Op(char symbol)    implements Token {}
    }

    static List<Token> tokenise(String input) {
        List<Token> tokens = new ArrayList<>();
        int[] pos = { 0 };    // mutable position shared across methods

        while (pos[0] < input.length()) {
            char c = input.charAt(pos[0]);
            if (Character.isWhitespace(c)) { pos[0]++; continue; }
            if (Character.isDigit(c) || c == '.') tokens.add(scanNumber(input, pos));
            else if (Character.isLetter(c))       tokens.add(scanWord(input, pos));
            else                                   tokens.add(new Token.Op(input.charAt(pos[0]++)));
        }
        return tokens;
    }

    private static Token.Num scanNumber(String s, int[] pos) {
        int start = pos[0];
        while (pos[0] < s.length() && (Character.isDigit(s.charAt(pos[0])) || s.charAt(pos[0]) == '.'))
            pos[0]++;
        return new Token.Num(Double.parseDouble(s.substring(start, pos[0])));
    }

    private static Token.Word scanWord(String s, int[] pos) {
        int start = pos[0];
        while (pos[0] < s.length() && Character.isLetterOrDigit(s.charAt(pos[0])))
            pos[0]++;
        return new Token.Word(s.substring(start, pos[0]));
    }

    // -----------------------------------------------------------------
    // 3. Hofstadter M / F sequences (from Gödel, Escher, Bach)
    //    M(0) = 0,  M(n) = n - F(M(n-1))
    //    F(0) = 1,  F(n) = n - M(F(n-1))
    // -----------------------------------------------------------------

    static int hofstadterM(int n) {
        return n == 0 ? 0 : n - hofstadterF(hofstadterM(n - 1));
    }

    static int hofstadterF(int n) {
        return n == 0 ? 1 : n - hofstadterM(hofstadterF(n - 1));
    }

    // -----------------------------------------------------------------
    // 4. Balanced parentheses grammar (mutually recursive recogniser)
    //    S  ::= '(' S ')' S | ε
    // The trick: S calls itself twice — once inside the parens, once after.
    // -----------------------------------------------------------------

    static boolean isBalanced(String s) {
        try {
            int end = parseS(s, 0);
            return end == s.length();
        } catch (RuntimeException e) {
            return false;
        }
    }

    // S ::= '(' S ')' S | ε
    // Returns the position after the successfully parsed S, throws on unmatched '('.
    private static int parseS(String s, int pos) {
        while (pos < s.length() && s.charAt(pos) == '(') {
            pos = parseS(s, pos + 1);   // match inner S
            if (pos >= s.length() || s.charAt(pos) != ')') {
                throw new RuntimeException("Unmatched '('");
            }
            pos = parseS(s, pos + 1);   // match trailing S
        }
        return pos;
    }
}
