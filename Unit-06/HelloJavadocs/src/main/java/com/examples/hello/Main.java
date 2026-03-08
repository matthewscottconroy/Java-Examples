package com.examples.hello;

/**
 * Entry point for the HelloJavadocs project.
 *
 * <p>Run {@code mvn javadoc:javadoc} to generate HTML documentation from
 * the {@code /**} comments in this project.  The output will be written to
 * {@code target/site/apidocs/index.html}.
 */
public class Main {

    public static void main(String[] args) {
        Greeter greeter = new Greeter("World");
        System.out.println(greeter.greet());
        System.out.println(greeter.farewell());
    }
}
