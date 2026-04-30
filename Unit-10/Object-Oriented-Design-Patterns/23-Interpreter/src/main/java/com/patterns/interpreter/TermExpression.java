package com.patterns.interpreter;

import java.util.Set;

/** Terminal expression — looks up a single keyword in the index. */
public class TermExpression implements SearchExpression {

    private final String term;

    public TermExpression(String term) {
        this.term = term.toLowerCase();
    }

    @Override
    public Set<String> evaluate(InvertedIndex index) {
        return index.lookup(term);
    }

    @Override
    public String toString() { return term; }
}
