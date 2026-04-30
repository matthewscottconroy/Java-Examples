package com.exam;

import java.util.*;
import java.util.stream.Collectors;

import static com.exam.Difficulty.*;

/**
 * Draws a calibrated sample of questions from the question pool.
 *
 * <p><b>Difficulty calibration</b> — for each section the generator targets:
 * <ul>
 *   <li>1 EASY question (anchor — builds confidence)
 *   <li>middle MEDIUM questions (bulk of the section)
 *   <li>1 HARD question (stretch — differentiates top students)
 * </ul>
 * If a difficulty tier is exhausted the gap is filled from whatever remains.
 *
 * <p><b>Topic uniqueness</b> — no two questions in the same section share
 * the same topic string. If the difficulty selection produces a clash the
 * duplicate is replaced with the next available question from the pool
 * whose topic has not yet been used in that section.
 */
public class ExamGenerator {

    private final int perType;
    private final long seed;
    private final String topicFilter;

    public ExamGenerator(int perType, long seed, String topicFilter) {
        this.perType     = perType;
        this.seed        = seed;
        this.topicFilter = topicFilter == null ? "" : topicFilter.toLowerCase();
    }

    /** Returns selected questions ordered by section (one per QuestionType). */
    public List<Question> generate() {
        return generate(seed);
    }

    /** Generates with an explicit seed (used by the multi-version caller). */
    public List<Question> generate(long effectiveSeed) {
        Random rng = new Random(effectiveSeed);

        List<Question> pool = QuestionBank.all();
        if (!topicFilter.isEmpty()) {
            pool = pool.stream()
                .filter(q -> q.topic().toLowerCase().contains(topicFilter))
                .toList();
        }

        Map<QuestionType, List<Question>> byType = pool.stream()
            .collect(Collectors.groupingBy(Question::type));

        List<Question> result = new ArrayList<>();
        for (QuestionType type : QuestionType.values()) {
            List<Question> bucket = new ArrayList<>(
                byType.getOrDefault(type, List.of()));
            result.addAll(selectFromBucket(bucket, perType, rng));
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // Selection logic
    // -----------------------------------------------------------------------

    private List<Question> selectFromBucket(List<Question> bucket, int n, Random rng) {
        if (bucket.isEmpty() || n == 0) return List.of();

        // Split into difficulty tiers, shuffle each independently
        Map<Difficulty, List<Question>> byDiff = bucket.stream()
            .collect(Collectors.groupingBy(Question::difficulty));

        List<Question> easy   = shuffled(byDiff.getOrDefault(EASY,   List.of()), rng);
        List<Question> medium = shuffled(byDiff.getOrDefault(MEDIUM, List.of()), rng);
        List<Question> hard   = shuffled(byDiff.getOrDefault(HARD,   List.of()), rng);

        // Target: 1 EASY anchor + 1 HARD stretch; MEDIUM fills the rest
        int nEasy   = Math.min((n >= 2) ? 1 : 0, easy.size());
        int nHard   = Math.min((n >= 2) ? 1 : 0, hard.size());
        int nMedium = Math.min(n - nEasy - nHard, medium.size());

        List<Question> chosen = new ArrayList<>();
        chosen.addAll(easy.subList(0, nEasy));
        chosen.addAll(medium.subList(0, nMedium));
        chosen.addAll(hard.subList(0, nHard));

        // If tiers exhausted, fill from remaining pool
        if (chosen.size() < n) {
            List<Question> remainder = new ArrayList<>(bucket);
            remainder.removeAll(chosen);
            Collections.shuffle(remainder, rng);
            int need = Math.min(n - chosen.size(), remainder.size());
            chosen.addAll(remainder.subList(0, need));
        }

        return enforceTopicUniqueness(chosen, bucket, n, rng);
    }

    /**
     * Ensures no two questions in the selection share the same topic.
     * Duplicates are replaced with unused-topic questions from the full bucket.
     */
    private List<Question> enforceTopicUniqueness(
            List<Question> chosen, List<Question> bucket, int target, Random rng) {

        Set<String> usedTopics = new LinkedHashSet<>();
        List<Question> unique  = new ArrayList<>();

        for (Question q : chosen) {
            if (usedTopics.add(q.topic())) unique.add(q);
        }

        if (unique.size() < chosen.size()) {
            // Try to fill gaps from the remainder of the bucket
            List<Question> remainder = new ArrayList<>(bucket);
            remainder.removeAll(chosen);
            Collections.shuffle(remainder, rng);
            for (Question q : remainder) {
                if (unique.size() >= target) break;
                if (usedTopics.add(q.topic())) unique.add(q);
            }
        }

        return unique;
    }

    private static List<Question> shuffled(List<Question> list, Random rng) {
        List<Question> copy = new ArrayList<>(list);
        Collections.shuffle(copy, rng);
        return copy;
    }
}
