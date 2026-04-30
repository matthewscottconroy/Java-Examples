package com.exam;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class QuestionBankTest {

    private final List<Question> bank = QuestionBank.all();

    @Test
    void allQuestionsHaveNonBlankFields() {
        for (Question q : bank) {
            assertFalse(q.topic().isBlank(),   "topic blank: " + q);
            assertFalse(q.unit().isBlank(),    "unit blank: " + q);
            assertFalse(q.prompt().isBlank(),  "prompt blank: " + q);
            assertFalse(q.key().isBlank(),     "key blank: " + q);
            assertNotNull(q.type(),            "type null: " + q);
            assertNotNull(q.difficulty(),      "difficulty null: " + q);
        }
    }

    @Test
    void eachTypePresentWithMinimumCount() {
        Map<QuestionType, Long> counts = bank.stream()
            .collect(Collectors.groupingBy(Question::type, Collectors.counting()));

        for (QuestionType t : QuestionType.values()) {
            long count = counts.getOrDefault(t, 0L);
            assertTrue(count >= 5,
                "Type " + t + " has only " + count + " questions (need >= 5)");
        }
    }

    @Test
    void eachTypeHasAtLeastOneOfEachDifficulty() {
        Map<QuestionType, Map<Difficulty, Long>> byTypeDiff = bank.stream()
            .collect(Collectors.groupingBy(Question::type,
                Collectors.groupingBy(Question::difficulty, Collectors.counting())));

        for (QuestionType t : QuestionType.values()) {
            Map<Difficulty, Long> diffMap = byTypeDiff.getOrDefault(t, Map.of());
            for (Difficulty d : Difficulty.values()) {
                long count = diffMap.getOrDefault(d, 0L);
                assertTrue(count >= 1,
                    "Type " + t + " has no " + d + " questions");
            }
        }
    }

    @Test
    void pointValuesMatchDifficulty() {
        for (Question q : bank) {
            int expected = switch (q.difficulty()) {
                case EASY   -> 5;
                case MEDIUM -> 10;
                case HARD   -> 15;
            };
            assertEquals(expected, q.points(),
                "Wrong points for " + q.difficulty() + " question: " + q.topic());
        }
    }

    @Test
    void totalBankSizeIsReasonable() {
        assertTrue(bank.size() >= 50, "Bank too small: " + bank.size());
    }

    @Test
    void codeFieldNeverNull() {
        for (Question q : bank) {
            assertNotNull(q.code(), "code() returned null for: " + q.topic());
        }
    }
}
