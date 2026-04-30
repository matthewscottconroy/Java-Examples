package com.exam;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorTest {

    @Test
    void sameSeedException_producesIdenticalExam() {
        ExamGenerator gen = new ExamGenerator(2, 42L, null);
        List<Question> first  = gen.generate();
        List<Question> second = gen.generate();
        assertEquals(first, second, "Same seed must produce same questions");
    }

    @Test
    void differentSeeds_producesDifferentExams() {
        ExamGenerator a = new ExamGenerator(2, 1L,  null);
        ExamGenerator b = new ExamGenerator(2, 2L,  null);
        assertNotEquals(a.generate(), b.generate(),
            "Different seeds should (almost always) produce different exams");
    }

    @Test
    void perTypeCountRespected_twoPerType() {
        ExamGenerator gen = new ExamGenerator(2, 42L, null);
        List<Question> questions = gen.generate();

        Map<QuestionType, Long> counts = questions.stream()
            .collect(Collectors.groupingBy(Question::type, Collectors.counting()));

        for (QuestionType t : QuestionType.values()) {
            long count = counts.getOrDefault(t, 0L);
            assertEquals(2, count, "Expected 2 questions for type " + t);
        }
    }

    @Test
    void perTypeCountRespected_threePerType() {
        ExamGenerator gen = new ExamGenerator(3, 99L, null);
        List<Question> questions = gen.generate();

        Map<QuestionType, Long> counts = questions.stream()
            .collect(Collectors.groupingBy(Question::type, Collectors.counting()));

        for (QuestionType t : QuestionType.values()) {
            long count = counts.getOrDefault(t, 0L);
            assertEquals(3, count, "Expected 3 questions for type " + t);
        }
    }

    @Test
    void topicUniquenessWithinSection() {
        ExamGenerator gen = new ExamGenerator(3, 7L, null);
        List<Question> questions = gen.generate();

        Map<QuestionType, List<Question>> byType = questions.stream()
            .collect(Collectors.groupingBy(Question::type));

        for (Map.Entry<QuestionType, List<Question>> entry : byType.entrySet()) {
            List<Question> section = entry.getValue();
            Set<String> topics = new HashSet<>();
            for (Question q : section) {
                assertTrue(topics.add(q.topic()),
                    "Duplicate topic '" + q.topic() + "' in section " + entry.getKey());
            }
        }
    }

    @Test
    void topicFilter_restrictsResults() {
        ExamGenerator gen = new ExamGenerator(2, 42L, "arrays");
        List<Question> questions = gen.generate();

        for (Question q : questions) {
            assertTrue(q.topic().toLowerCase().contains("arrays"),
                "Non-matching topic slipped through filter: " + q.topic());
        }
    }

    @Test
    void multiVersion_differentSeeds_produce_differentExams() {
        ExamGenerator gen = new ExamGenerator(2, 1000L, null);
        List<Question> vA = gen.generate(1000L);
        List<Question> vB = gen.generate(1000L + 1_000_003L);
        assertNotEquals(vA, vB, "Version A and B should differ");
    }

    @Test
    void multiVersion_sameSeed_isReproducible() {
        ExamGenerator gen = new ExamGenerator(2, 777L, null);
        List<Question> first  = gen.generate(777L + 1_000_003L);
        List<Question> second = gen.generate(777L + 1_000_003L);
        assertEquals(first, second, "Same derived seed must reproduce version B");
    }

    @Test
    void difficultyCalibration_twoPerType_hasEasyAndHard() {
        ExamGenerator gen = new ExamGenerator(2, 42L, null);
        List<Question> questions = gen.generate();

        Map<QuestionType, List<Question>> byType = questions.stream()
            .collect(Collectors.groupingBy(Question::type));

        for (Map.Entry<QuestionType, List<Question>> entry : byType.entrySet()) {
            List<Question> section = entry.getValue();
            Set<Difficulty> diffs = section.stream()
                .map(Question::difficulty)
                .collect(Collectors.toSet());
            assertTrue(diffs.contains(Difficulty.EASY),
                "Section " + entry.getKey() + " missing EASY question");
            assertTrue(diffs.contains(Difficulty.HARD),
                "Section " + entry.getKey() + " missing HARD question");
        }
    }
}
