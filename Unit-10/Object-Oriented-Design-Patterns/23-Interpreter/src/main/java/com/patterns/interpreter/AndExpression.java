package com.patterns.interpreter;

import java.util.HashSet;
import java.util.Set;

/** Non-terminal expression — intersection of two sub-expressions (logical AND). */
public class AndExpression implements SearchExpression {

    private final SearchExpression left;
    private final SearchExpression right;

    public AndExpression(SearchExpression left, SearchExpression right) {
        this.left  = left;
        this.right = right;
    }

    @Override
    public Set<String> evaluate(InvertedIndex index) {
        Set<String> result = new HashSet<>(left.evaluate(index));
        result.retainAll(right.evaluate(index));
        return result;
    }

    @Override
    public String toString() { return "(" + left + " AND " + right + ")"; }
}
