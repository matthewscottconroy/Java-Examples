package com.examples.hello;

/**
 * Produces greeting messages.
 *
 * <p>This class exists to show how Javadoc comments work.
 * Every public class, field, and method should have a {@code /**} comment
 * so that tools like {@code mvn javadoc:javadoc} can generate HTML documentation.
 *
 * @author  Your Name
 * @version 1.0
 * @since   1.0
 */
public class Greeter {

    /** The name of the person being greeted. */
    private final String name;

    /**
     * Creates a new {@code Greeter} for the given name.
     *
     * @param name the person to greet; must not be {@code null}
     * @throws IllegalArgumentException if {@code name} is {@code null} or blank
     */
    public Greeter(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name;
    }

    /**
     * Returns a formal greeting.
     *
     * @return a greeting string, e.g. {@code "Hello, Alice!"}
     */
    public String greet() {
        return "Hello, " + name + "!";
    }

    /**
     * Returns a farewell message.
     *
     * @return a farewell string, e.g. {@code "Goodbye, Alice!"}
     */
    public String farewell() {
        return "Goodbye, " + name + "!";
    }
}
