package com.patterns.nullobject;

/**
 * A real discount — subtracts a percentage from the price.
 *
 * @param percent discount percentage in the range [0, 100]
 * @param code    promotional code that triggered this discount
 */
public record PercentageDiscount(int percent, String code) implements Discount {

    public PercentageDiscount {
        if (percent < 0 || percent > 100)
            throw new IllegalArgumentException("percent must be 0–100, got " + percent);
    }

    @Override
    public int apply(int priceCents) {
        return priceCents - (priceCents * percent / 100);
    }

    @Override
    public String description() { return percent + "% off (code: " + code + ")"; }
}
