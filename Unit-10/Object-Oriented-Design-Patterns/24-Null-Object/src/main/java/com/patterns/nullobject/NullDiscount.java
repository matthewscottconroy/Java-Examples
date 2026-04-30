package com.patterns.nullobject;

/**
 * Null Object — a discount that does nothing.
 *
 * <p>Used when no promotional code has been applied. Callers treat it
 * identically to a real discount; no special-casing required.
 */
public final class NullDiscount implements Discount {

    public static final NullDiscount INSTANCE = new NullDiscount();

    private NullDiscount() {}

    @Override
    public int apply(int priceCents) { return priceCents; }

    @Override
    public String description() { return "No discount"; }
}
