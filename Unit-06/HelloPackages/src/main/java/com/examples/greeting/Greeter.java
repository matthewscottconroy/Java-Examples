package com.examples.greeting;  // <-- this class belongs to the "greeting" package

/**
 * Produces a greeting message.
 *
 * <p>This class lives in the {@code com.examples.greeting} package.
 * Its file path on disk mirrors the package name exactly:
 * {@code src/main/java/com/examples/greeting/Greeter.java}
 */
public class Greeter {

    /**
     * Returns a greeting for the given name.
     *
     * @param name the person to greet
     * @return a greeting string
     */
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
