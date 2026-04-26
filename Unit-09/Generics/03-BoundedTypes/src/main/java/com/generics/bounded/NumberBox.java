package com.generics.bounded;

/**
 * A box that only accepts {@link Number} subtypes.
 *
 * <p>The upper bound {@code <T extends Number>} does two things:
 * <ol>
 *   <li>Restricts what callers can pass in (must be Number or a subclass).</li>
 *   <li>Unlocks the full {@code Number} API inside the class body — we can
 *       call {@code .intValue()}, {@code .doubleValue()}, etc. without casting.</li>
 * </ol>
 *
 * <p>Without the bound we would only have the {@code Object} API available,
 * and the arithmetic below would require an unsafe cast.
 */
public class NumberBox<T extends Number> {

    private final T value;

    public NumberBox(T value) {
        this.value = value;
    }

    public T get() { return value; }

    // Possible only because the bound gives us Number's doubleValue().
    public double doubled() {
        return value.doubleValue() * 2;
    }

    public double asDouble() {
        return value.doubleValue();
    }

    public boolean isGreaterThan(NumberBox<? extends Number> other) {
        return value.doubleValue() > other.get().doubleValue();
    }

    @Override
    public String toString() {
        return "NumberBox[" + value + "]";
    }
}
