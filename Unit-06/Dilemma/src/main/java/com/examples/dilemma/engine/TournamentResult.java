package com.examples.dilemma.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The complete outcome of a tournament: per-strategy standings and every
 * individual match result.
 *
 * <p>Standings are sorted by total score descending and assigned ranks
 * starting at 1. Use {@link #from} to build a result from a list of matches.
 *
 * @param standings      ranked list of per-strategy statistics
 * @param matches        every match played, in the order they were run
 * @param roundsPerMatch the number of rounds each pair played
 */
public record TournamentResult(
        List<Standing> standings,
        List<MatchResult> matches,
        int roundsPerMatch) {

    /**
     * Per-strategy aggregate statistics, ranked by total score.
     *
     * @param rank            place in the final standings (1 = best)
     * @param strategyName    the strategy's display name
     * @param totalScore      sum of all points earned across all matches
     * @param matchesPlayed   number of matches played
     * @param wins            matches in which this strategy scored more
     * @param losses          matches in which this strategy scored less
     * @param draws           matches in which both strategies scored equally
     * @param avgScorePerRound average points earned per individual round
     */
    public record Standing(
            int rank,
            String strategyName,
            int totalScore,
            int matchesPlayed,
            int wins,
            int losses,
            int draws,
            double avgScorePerRound) {}

    /**
     * Builds a {@code TournamentResult} by aggregating a list of match results.
     *
     * @param matches        all matches played
     * @param roundsPerMatch rounds each pair played
     * @return the complete tournament result with ranked standings
     */
    public static TournamentResult from(List<MatchResult> matches, int roundsPerMatch) {
        // totalScore, matchesPlayed, wins, losses, draws
        Map<String, int[]> stats = new LinkedHashMap<>();

        for (MatchResult m : matches) {
            stats.computeIfAbsent(m.nameA(), k -> new int[5]);
            stats.computeIfAbsent(m.nameB(), k -> new int[5]);

            int[] a = stats.get(m.nameA());
            int[] b = stats.get(m.nameB());

            a[0] += m.scoreA(); a[1]++;
            b[0] += m.scoreB(); b[1]++;

            if (m.scoreA() > m.scoreB()) { a[2]++; b[3]++; }
            else if (m.scoreB() > m.scoreA()) { b[2]++; a[3]++; }
            else { a[4]++; b[4]++; }
        }

        List<Standing> sorted = new ArrayList<>();
        for (var entry : stats.entrySet()) {
            int[] s = entry.getValue();
            double avg = s[1] == 0 ? 0.0 : (double) s[0] / (s[1] * roundsPerMatch);
            sorted.add(new Standing(0, entry.getKey(), s[0], s[1], s[2], s[3], s[4], avg));
        }
        sorted.sort(Comparator.comparingInt(Standing::totalScore).reversed());

        List<Standing> ranked = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Standing s = sorted.get(i);
            ranked.add(new Standing(
                    i + 1, s.strategyName(), s.totalScore(), s.matchesPlayed(),
                    s.wins(), s.losses(), s.draws(), s.avgScorePerRound()));
        }

        return new TournamentResult(List.copyOf(ranked), List.copyOf(matches), roundsPerMatch);
    }
}
