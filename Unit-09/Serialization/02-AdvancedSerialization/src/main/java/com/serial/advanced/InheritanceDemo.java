package com.serial.advanced;

import java.io.*;

/**
 * Serialization and inheritance.
 *
 * <p><strong>Case 1: subclass is Serializable, superclass is also Serializable.</strong>
 * Everything is handled automatically — all fields in the hierarchy are serialized.
 *
 * <p><strong>Case 2: subclass is Serializable, superclass is NOT.</strong>
 * The JVM cannot serialize the superclass's fields automatically (it never consented).
 * Instead, it calls the superclass's <em>public or protected no-arg constructor</em>
 * when deserializing, re-initializing the superclass portion via normal construction.
 * If the superclass has no accessible no-arg constructor, deserialization fails with
 * an {@link InvalidClassException}.
 *
 * <p>To manually preserve superclass state, use writeObject/readObject to write
 * and read the superclass fields yourself via {@code out.writeInt(...)} etc.
 */
public class InheritanceDemo {

    // -----------------------------------------------------------------------
    // Non-serializable superclass — must have a no-arg constructor.
    // -----------------------------------------------------------------------
    static class Shape {                // NOT Serializable
        protected String color;

        public Shape() { this.color = "unknown"; }   // required no-arg constructor
        public Shape(String color) { this.color = color; }

        @Override public String toString() { return "Shape{color=" + color + "}"; }
    }

    // -----------------------------------------------------------------------
    // Serializable subclass — stores only its own fields automatically;
    // manages the superclass state manually via writeObject/readObject.
    // -----------------------------------------------------------------------
    static class Circle extends Shape implements Serializable {
        @Serial private static final long serialVersionUID = 1L;

        private final double radius;

        Circle(String color, double radius) {
            super(color);
            this.radius = radius;
        }

        @Serial
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();   // writes: radius (the subclass field)
            out.writeObject(color);     // manually write the superclass field
        }

        @Serial
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();     // restores: radius
            // The JVM already called Shape() before readObject, setting color="unknown".
            // Now we overwrite it with the real value.
            color = (String) in.readObject();
        }

        @Override public String toString() {
            return "Circle{color=" + color + ", radius=" + radius + "}";
        }
    }

    public static void demonstrate() throws Exception {
        System.out.println("-- Subclass of non-serializable parent --");

        Circle original = new Circle("red", 5.0);
        System.out.println("  original: " + original);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(original);
        }

        Circle restored;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            restored = (Circle) ois.readObject();
        }
        System.out.println("  restored: " + restored);
        System.out.println("  color matches: " + original.color.equals(restored.color));
    }
}
