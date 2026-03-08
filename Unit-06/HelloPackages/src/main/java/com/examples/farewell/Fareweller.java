package com.examples.farewell;  // <-- a separate package for a separate concern

/**
 * Produces a farewell message.
 *
 * <p>This class lives in the {@code com.examples.farewell} package.
 * Packages let you group related classes together and keep unrelated
 * classes separate — even when they live in the same project.
 */
public class Fareweller {

    /**
     * Returns a farewell for the given name.
     *
     * @param name the person to say goodbye to
     * @return a farewell string
     */
    public String farewell(String name) {
        return "Goodbye, " + name + "!";
    }
}
