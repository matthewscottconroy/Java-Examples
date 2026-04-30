package com.patterns.visitor;

/**
 * Element interface — every income type must accept a visitor.
 *
 * <p>The {@code accept} method's single job is to call the right
 * {@code visit} overload on the visitor, enabling double dispatch.
 */
public interface IncomeSource {
    double accept(TaxVisitor visitor);
    double grossAmount();
    String description();
}
