package com.examples.dilemma.engine;

import com.examples.dilemma.strategy.Strategy;
import com.examples.dilemma.strategy.impl.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Discovers and loads {@link Strategy} implementations.
 *
 * <p>Two sources are supported:
 * <ol>
 *   <li><strong>Built-in strategies</strong> — compiled into the jar, always available.</li>
 *   <li><strong>External strategies</strong> — {@code .class} files placed in the
 *       {@code strategies/} directory at the project root. Any class that implements
 *       {@link Strategy} and has a public no-arg constructor is loaded automatically.</li>
 * </ol>
 *
 * <h2>Submitting an external strategy</h2>
 * <pre>
 *   # 1. Compile your strategy
 *   javac -cp target/dilemma-1.0-SNAPSHOT.jar MyStrategy.java
 *
 *   # 2. Drop the class file into the strategies/ directory
 *   mv MyStrategy.class strategies/
 *
 *   # 3. Run the tournament — it will be discovered automatically
 *   mvn exec:java
 * </pre>
 */
public final class StrategyLoader {

    private StrategyLoader() {}

    /**
     * Returns all built-in strategy instances.
     *
     * @return a fresh, mutable list of built-in strategies
     */
    public static List<Strategy> loadBuiltIn() {
        return new ArrayList<>(List.of(
                new TitForTat(),
                new AlwaysCooperate(),
                new AlwaysDefect(),
                new RandomStrategy(),
                new GrimTrigger(),
                new Pavlov(),
                new TitForTwoTats(),
                new SuspiciousTitForTat(),
                new Joss()
        ));
    }

    /**
     * Loads strategy implementations from {@code .class} files found in
     * {@code dir}. Classes that do not implement {@link Strategy} or lack a
     * public no-arg constructor are silently skipped.
     *
     * @param dir the directory to scan (may or may not exist)
     * @return strategy instances loaded from the directory
     */
    public static List<Strategy> loadFromDirectory(Path dir) {
        List<Strategy> external = new ArrayList<>();
        if (!Files.isDirectory(dir)) return external;

        try {
            URL[] urls = { dir.toUri().toURL() };
            try (URLClassLoader loader = new URLClassLoader(urls, Strategy.class.getClassLoader());
                 Stream<Path> files = Files.walk(dir, 1)) {

                files.filter(p -> p.toString().endsWith(".class"))
                     .forEach(p -> {
                         String className = p.getFileName().toString().replace(".class", "");
                         try {
                             Class<?> clazz = loader.loadClass(className);
                             if (Strategy.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                                 Strategy s = (Strategy) clazz.getDeclaredConstructor().newInstance();
                                 external.add(s);
                                 System.out.println("[StrategyLoader] Loaded external: " + s.getName());
                             }
                         } catch (Exception e) {
                             System.err.println("[StrategyLoader] Skipped " + p.getFileName()
                                     + ": " + e.getMessage());
                         }
                     });
            }
        } catch (IOException e) {
            System.err.println("[StrategyLoader] Could not read directory: " + dir);
        }
        return external;
    }

    /**
     * Loads all built-in strategies plus any found in {@code externalDir}.
     *
     * @param externalDir directory to scan for external strategies
     * @return combined list of all available strategies
     */
    public static List<Strategy> loadAll(Path externalDir) {
        List<Strategy> all = loadBuiltIn();
        all.addAll(loadFromDirectory(externalDir));
        return all;
    }
}
