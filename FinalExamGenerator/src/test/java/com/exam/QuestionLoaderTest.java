package com.exam;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionLoaderTest {

    // -----------------------------------------------------------------------
    // parse() — unit tests on the content parser
    // -----------------------------------------------------------------------

    private static final String VALID_Q = """
        TYPE:       WRITE
        TOPIC:      Strings
        UNIT:       03
        DIFFICULTY: MEDIUM

        ---PROMPT---
        Write a method that counts vowels in a string.
        ---CODE---
        public static int countVowels(String s) { /* TODO */ }
        ---KEY---
        Iterate over characters and check if each is in "aeiouAEIOU".
        ---END---
        """;

    @Test
    void parse_validFile_returnsQuestion() {
        Question q = QuestionLoader.parse(VALID_Q, "test");
        assertEquals(QuestionType.WRITE,   q.type());
        assertEquals("Strings",            q.topic());
        assertEquals("03",                 q.unit());
        assertEquals(Difficulty.MEDIUM,    q.difficulty());
        assertTrue(q.prompt().contains("vowels"));
        assertTrue(q.code().contains("countVowels"));
        assertTrue(q.key().contains("aeiou"));
    }

    @Test
    void parse_commentsIgnored() {
        String content = """
            # This is a comment
            TYPE:       DEBUG
            # Another comment
            TOPIC:      Arrays
            UNIT:       01
            DIFFICULTY: EASY

            ---PROMPT---
            Find the bug in this sort.
            ---CODE---
            int[] arr = {3,1,2};
            ---KEY---
            The comparison is inverted.
            ---END---
            """;
        Question q = QuestionLoader.parse(content, "test");
        assertEquals(QuestionType.DEBUG, q.type());
        assertEquals("Arrays",           q.topic());
    }

    @Test
    void parse_missingCodeSection_givesEmptyCode() {
        String content = """
            TYPE:       DESIGN
            TOPIC:      OOP
            UNIT:       05
            DIFFICULTY: HARD

            ---PROMPT---
            Design a class hierarchy for vehicles.
            ---KEY---
            Vehicle base class with move(); Car and Bike subclasses.
            ---END---
            """;
        Question q = QuestionLoader.parse(content, "test");
        assertTrue(q.code().isBlank(), "Missing ---CODE--- should give empty code");
        assertFalse(q.hasCode());
    }

    @Test
    void parse_missingPrompt_throwsException() {
        String content = """
            TYPE:  WRITE
            TOPIC: Arrays
            UNIT:  01
            DIFFICULTY: EASY

            ---KEY---
            Some key
            ---END---
            """;
        assertThrows(IllegalArgumentException.class,
            () -> QuestionLoader.parse(content, "test"));
    }

    @Test
    void parse_missingKey_throwsException() {
        String content = """
            TYPE:  WRITE
            TOPIC: Arrays
            UNIT:  01
            DIFFICULTY: EASY

            ---PROMPT---
            Write something.
            ---END---
            """;
        assertThrows(IllegalArgumentException.class,
            () -> QuestionLoader.parse(content, "test"));
    }

    @Test
    void parse_unknownType_throwsException() {
        String content = """
            TYPE:  UNKNOWN
            TOPIC: Arrays
            UNIT:  01
            DIFFICULTY: EASY

            ---PROMPT---
            Write something.
            ---KEY---
            Answer.
            ---END---
            """;
        assertThrows(IllegalArgumentException.class,
            () -> QuestionLoader.parse(content, "test"));
    }

    @Test
    void parse_unknownDifficulty_throwsException() {
        String content = """
            TYPE:  WRITE
            TOPIC: Arrays
            UNIT:  01
            DIFFICULTY: EXTREME

            ---PROMPT---
            Write something.
            ---KEY---
            Answer.
            ---END---
            """;
        assertThrows(IllegalArgumentException.class,
            () -> QuestionLoader.parse(content, "test"));
    }

    // -----------------------------------------------------------------------
    // loadFrom() — integration tests on the directory scanner
    // -----------------------------------------------------------------------

    @Test
    void loadFrom_nonExistentDir_returnsEmpty() {
        List<Question> result = QuestionLoader.loadFrom(Path.of("/does/not/exist"));
        assertTrue(result.isEmpty());
    }

    @Test
    void loadFrom_emptyDir_returnsEmpty(@TempDir Path dir) {
        assertTrue(QuestionLoader.loadFrom(dir).isEmpty());
    }

    @Test
    void loadFrom_validQFile_returnsQuestion(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("test.q"), VALID_Q);
        List<Question> result = QuestionLoader.loadFrom(dir);
        assertEquals(1, result.size());
        assertEquals("Strings", result.get(0).topic());
    }

    @Test
    void loadFrom_invalidQFile_skipsWithWarning(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("good.q"), VALID_Q);
        Files.writeString(dir.resolve("bad.q"), "GARBAGE CONTENT\nno markers\n");
        List<Question> result = QuestionLoader.loadFrom(dir);
        assertEquals(1, result.size(), "Bad file should be skipped, good one loaded");
    }

    @Test
    void loadFrom_scansSubdirectories(@TempDir Path dir) throws IOException {
        Path sub = dir.resolve("sub");
        Files.createDirectory(sub);
        Files.writeString(sub.resolve("nested.q"), VALID_Q);
        List<Question> result = QuestionLoader.loadFrom(dir);
        assertEquals(1, result.size(), "Should scan recursively into subdirectories");
    }

    @Test
    void loadFrom_ignoresNonQFiles(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("notes.txt"), VALID_Q);
        Files.writeString(dir.resolve("data.java"), VALID_Q);
        assertTrue(QuestionLoader.loadFrom(dir).isEmpty(),
            "Only .q files should be loaded");
    }
}
