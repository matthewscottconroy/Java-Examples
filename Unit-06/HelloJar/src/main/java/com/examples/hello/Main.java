package com.examples.hello;

/**
 * Entry point for the HelloJar project.
 *
 * <p>After running {@code mvn package}, the JVM can launch this class directly
 * from the JAR file without specifying a classpath or class name:
 * <pre>
 *   java -jar target/hello-jar-1.0-SNAPSHOT.jar
 * </pre>
 * This works because {@code mvn package} writes a {@code MANIFEST.MF} file
 * inside the JAR that contains: {@code Main-Class: com.examples.hello.Main}
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Hello, JAR!");
        System.out.println("Running from: " + Main.class.getProtectionDomain()
                .getCodeSource().getLocation());
    }
}
