package com.patterns.interpreter;

import java.util.HashSet;
import java.util.Set;

/** Non-terminal expression — complement of a sub-expression (logical NOT). */
public class NotExpression implements SearchExpression {

    private final SearchExpression operand;

    public NotExpression(SearchExpression operand) {
        this.operand = operand;
    }

    @Override
    public Set<String> evaluate(InvertedIndex index) {
        Set<String> all     = new HashSet<>(index.allDocuments());
        Set<String> exclude = operand.evaluate(index);
        all.removeAll(exclude);
        return all;
    }

    @Override
    public String toString() { return "(NOT " + operand + ")"; }
}
