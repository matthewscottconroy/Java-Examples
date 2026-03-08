package com.examples.dilemma.app;

import com.examples.dilemma.engine.StrategyLoader;
import com.examples.dilemma.engine.Tournament;
import com.examples.dilemma.engine.TournamentResult;
import com.examples.dilemma.io.CsvExporter;
import com.examples.dilemma.strategy.Strategy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Command-line entry point for the Prisoner's Dilemma tournament.
 *
 * <p>Usage:
 * <pre>
 *   mvn exec:java                               # defaults
 *   mvn exec:java -Dexec.args="--rounds 500"
 *   mvn exec:java -Dexec.args="--rounds 200 --output results/"
 * </pre>
 *
 * <p>Any {@code .class} files placed in the {@code strategies/} directory that
 * implement {@link com.examples.dilemma.strategy.Strategy} are automatically
 * entered in the tournament.
 */
public final class Main {

    private Main() {}

    /** Entry point. */
    public static void main(String[] args) throws IOException {
        int rounds           = 200;
        Path strategiesDir   = Path.of("strategies");
        Path outputDir       = Path.of("output");

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--rounds"         -> rounds        = Integer.parseInt(args[++i]);
                case "--strategies-dir" -> strategiesDir = Path.of(args[++i]);
                case "--output"         -> outputDir     = Path.of(args[++i]);
                default -> System.err.println("Unknown argument: " + args[i]);
            }
        }

        List<Strategy> strategies = StrategyLoader.loadAll(strategiesDir);
        System.out.printf("Loaded %d strategies.%n", strategies.size());
        strategies.forEach(s -> System.out.printf("  - %s%n", s.getName()));

        System.out.printf("%nRunning tournament: %d rounds per match, %d strategies...%n",
                rounds, strategies.size());

        Tournament tournament = new Tournament(strategies, rounds);
        TournamentResult result = tournament.run(System.out::println);

        System.out.println("\n=== STANDINGS ===");
        System.out.printf("%-4s %-24s %8s %10s %5s %5s %5s%n",
                "Rank", "Strategy", "Score", "Avg/Round", "Wins", "Loss", "Draw");
        System.out.println("-".repeat(62));
        for (TournamentResult.Standing s : result.standings()) {
            System.out.printf("%-4d %-24s %8d %10.4f %5d %5d %5d%n",
                    s.rank(), s.strategyName(), s.totalScore(),
                    s.avgScorePerRound(), s.wins(), s.losses(), s.draws());
        }

        CsvExporter.export(result, outputDir);
        System.out.printf("%nCSV files written to: %s/%n", outputDir.toAbsolutePath());
    }
}
