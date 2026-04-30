package com.patterns.interpreter;

import java.util.Set;

/**
 * Abstract expression — every node in the query tree must implement this.
 *
 * <p>The context is the set of document IDs; each expression evaluates to the
 * subset of documents that match its sub-query.
 */
public interface SearchExpression {

    /**
     * Evaluate this expression against the index.
     *
     * @param index the inverted index mapping terms to document IDs
     * @return set of document IDs that satisfy this expression
     */
    Set<String> evaluate(InvertedIndex index);
}
