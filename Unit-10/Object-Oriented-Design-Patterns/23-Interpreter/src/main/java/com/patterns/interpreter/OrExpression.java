package com.patterns.interpreter;

import java.util.HashSet;
import java.util.Set;

/** Non-terminal expression — union of two sub-expressions (logical OR). */
public class OrExpression implements SearchExpression {

    private final SearchExpression left;
    private final SearchExpression right;

    public OrExpression(SearchExpression left, SearchExpression right) {
        this.left  = left;
        this.right = right;
    }

    @Override
    public Set<String> evaluate(InvertedIndex index) {
        Set<String> result = new HashSet<>(left.evaluate(index));
        result.addAll(right.evaluate(index));
        return result;
    }

    @Override
    public String toString() { return "(" + left + " OR " + right + ")"; }
}
