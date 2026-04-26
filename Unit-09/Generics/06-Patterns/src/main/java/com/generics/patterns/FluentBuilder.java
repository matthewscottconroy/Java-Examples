package com.generics.patterns;

/**
 * Self-bounded generics for a fluent, inheritable builder hierarchy.
 *
 * <p><strong>Problem:</strong> a base builder's setters return {@code this},
 * but {@code this} has the base type — so callers lose access to subclass
 * methods after calling an inherited setter.
 *
 * <p><strong>Solution:</strong> {@code <B extends AbstractBuilder<B>>}
 * lets the base class refer to the <em>concrete</em> builder subtype B.
 * Each setter returns B (not AbstractBuilder), so the chain always delivers
 * the most specific type.
 *
 * <p>This is the same trick Java's {@code Enum<E extends Enum<E>>} uses.
 */
public class FluentBuilder {

    // -----------------------------------------------------------------------
    // Shared abstract base builder
    // -----------------------------------------------------------------------
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B>> {

        protected String name;
        protected String description;

        // The unchecked cast is safe: B is always the concrete subtype of this.
        @SuppressWarnings("unchecked")
        public B name(String name) {
            this.name = name;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B description(String description) {
            this.description = description;
            return (B) this;
        }

        public abstract Object build();
    }

    // -----------------------------------------------------------------------
    // Product: Animal
    // -----------------------------------------------------------------------
    public record Animal(String name, String description, String sound) {}

    public static final class AnimalBuilder extends AbstractBuilder<AnimalBuilder> {

        private String sound = "(silent)";

        // Subclass-specific setter — returns AnimalBuilder, not AbstractBuilder.
        public AnimalBuilder sound(String sound) {
            this.sound = sound;
            return this;
        }

        @Override
        public Animal build() {
            return new Animal(name, description, sound);
        }
    }

    // -----------------------------------------------------------------------
    // Product: Vehicle
    // -----------------------------------------------------------------------
    public record Vehicle(String name, String description, int horsepower) {}

    public static final class VehicleBuilder extends AbstractBuilder<VehicleBuilder> {

        private int horsepower = 0;

        public VehicleBuilder horsepower(int hp) {
            this.horsepower = hp;
            return this;
        }

        @Override
        public Vehicle build() {
            return new Vehicle(name, description, horsepower);
        }
    }
}
