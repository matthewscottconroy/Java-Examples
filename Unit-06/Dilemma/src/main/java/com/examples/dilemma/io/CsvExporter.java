package com.examples.dilemma.io;

import com.examples.dilemma.engine.MatchResult;
import com.examples.dilemma.engine.TournamentResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Exports tournament results to CSV files.
 *
 * <p>Two files are written to the output directory:
 * <ul>
 *   <li>{@code standings-[timestamp].csv} — ranked per-strategy statistics</li>
 *   <li>{@code matches-[timestamp].csv} — every individual match result</li>
 * </ul>
 *
 * <p>The timestamp in the filename makes it easy to compare multiple
 * tournament runs without overwriting previous results.
 */
public final class CsvExporter {

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private CsvExporter() {}

    /**
     * Writes standings and match results to CSV files in {@code outputDir}.
     * The directory is created if it does not exist.
     *
     * @param result    the tournament result to export
     * @param outputDir the directory to write files into
     * @throws IOException if a file cannot be written
     */
    public static void export(TournamentResult result, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        String ts = LocalDateTime.now().format(TIMESTAMP);
        writeStandings(result, outputDir.resolve("standings-" + ts + ".csv"));
        writeMatches(result, outputDir.resolve("matches-" + ts + ".csv"));
    }

    private static void writeStandings(TournamentResult result, Path file) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(file))) {
            pw.println("Rank,Strategy,TotalScore,AvgPerRound,Wins,Losses,Draws,MatchesPlayed");
            for (TournamentResult.Standing s : result.standings()) {
                pw.printf("%d,%s,%d,%.4f,%d,%d,%d,%d%n",
                        s.rank(),
                        csvEscape(s.strategyName()),
                        s.totalScore(),
                        s.avgScorePerRound(),
                        s.wins(),
                        s.losses(),
                        s.draws(),
                        s.matchesPlayed());
            }
        }
    }

    private static void writeMatches(TournamentResult result, Path file) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(file))) {
            pw.println("StrategyA,StrategyB,ScoreA,ScoreB,Winner,Rounds");
            for (MatchResult m : result.matches()) {
                pw.printf("%s,%s,%d,%d,%s,%d%n",
                        csvEscape(m.nameA()),
                        csvEscape(m.nameB()),
                        m.scoreA(),
                        m.scoreB(),
                        csvEscape(m.winner()),
                        m.rounds());
            }
        }
    }

    private static String csvEscape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
