package com.exam;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Loads additional questions from {@code .q} files in a directory.
 *
 * <p>This lets instructors add or edit questions without touching Java source.
 * Place {@code .q} files anywhere under the {@code questions/} directory
 * (sub-directories are scanned recursively) and they will be merged with
 * the built-in bank at runtime.
 *
 * <h2>File format</h2>
 * <pre>
 * # Lines that start with # are comments and are ignored.
 *
 * TYPE:       WRITE          # WRITE | DEBUG | EXTEND | TRACE | DESIGN
 * TOPIC:      Arrays
 * UNIT:       01
 * DIFFICULTY: EASY           # EASY | MEDIUM | HARD
 *
 * ---PROMPT---
 * Write a static method int[] reverseArray(int[] arr) that returns
 * a new array with the elements in reverse order.
 * ---CODE---
 * (leave empty or omit this section entirely if there is no starter code)
 * ---KEY---
 * Allocate result[arr.length]. For index i copy arr[arr.length-1-i].
 * Or swap in-place with two pointers.
 * ---END---
 * </pre>
 *
 * <p>The {@code ---CODE---} section is optional; if absent the question
 * will have an empty code block. Every other section is required.
 */
public class QuestionLoader {

    private static final String PROMPT_MARKER = "---PROMPT---";
    private static final String CODE_MARKER   = "---CODE---";
    private static final String KEY_MARKER    = "---KEY---";
    private static final String END_MARKER    = "---END---";

    /**
     * Loads all {@code .q} files found under {@code dir} (recursively).
     * Files that cannot be parsed are skipped with a warning to stderr.
     *
     * @param dir root directory to scan; returns empty list if it does not exist
     */
    public static List<Question> loadFrom(Path dir) {
        if (!Files.isDirectory(dir)) return List.of();

        try (Stream<Path> walk = Files.walk(dir)) {
            return walk
                .filter(p -> p.toString().endsWith(".q"))
                .flatMap(p -> {
                    try {
                        return Stream.of(parseFile(p));
                    } catch (Exception e) {
                        System.err.println("[QuestionLoader] Skipping " + p
                            + " — " + e.getMessage());
                        return Stream.empty();
                    }
                })
                .toList();
        } catch (IOException e) {
            System.err.println("[QuestionLoader] Cannot scan " + dir + ": " + e.getMessage());
            return List.of();
        }
    }

    // -----------------------------------------------------------------------
    // Parser
    // -----------------------------------------------------------------------

    static Question parseFile(Path path) throws IOException {
        String content = Files.readString(path);
        return parse(content, path.toString());
    }

    /** Package-private for testing. */
    static Question parse(String content, String source) {
        // Split into raw lines, strip comments and blank-only leading/trailing
        String[] lines = content.split("\n", -1);

        // Phase 1: collect header key-value pairs until the first section marker
        Map<String, String> headers = new LinkedHashMap<>();
        int i = 0;
        while (i < lines.length) {
            String line = lines[i].strip();
            if (line.startsWith("#") || line.isEmpty()) { i++; continue; }
            if (isSectionMarker(line)) break;
            int colon = line.indexOf(':');
            if (colon > 0) {
                headers.put(line.substring(0, colon).strip().toUpperCase(),
                            line.substring(colon + 1).strip());
            }
            i++;
        }

        // Phase 2: collect labelled sections
        Map<String, String> sections = new LinkedHashMap<>();
        String currentSection = null;
        StringBuilder buf = new StringBuilder();

        while (i < lines.length) {
            String line = lines[i];
            String stripped = line.strip();
            if (stripped.equals(PROMPT_MARKER) || stripped.equals(CODE_MARKER)
                    || stripped.equals(KEY_MARKER) || stripped.equals(END_MARKER)) {
                if (currentSection != null) {
                    sections.put(currentSection, buf.toString().stripTrailing());
                }
                buf.setLength(0);
                currentSection = stripped.equals(END_MARKER) ? null : stripped;
            } else if (currentSection != null) {
                if (buf.length() > 0 || !stripped.isEmpty()) { // skip leading blank lines
                    buf.append(line).append('\n');
                }
            }
            i++;
        }
        if (currentSection != null) {
            sections.put(currentSection, buf.toString().stripTrailing());
        }

        // Phase 3: assemble Question
        String prompt = sections.getOrDefault(PROMPT_MARKER, "").strip();
        String code   = sections.getOrDefault(CODE_MARKER,   "").strip();
        String key    = sections.getOrDefault(KEY_MARKER,    "").strip();

        if (prompt.isEmpty()) throw new IllegalArgumentException(
            "Missing ---PROMPT--- section in " + source);
        if (key.isEmpty()) throw new IllegalArgumentException(
            "Missing ---KEY--- section in " + source);

        QuestionType type       = parseEnum(QuestionType.class,  headers, "TYPE",       source);
        Difficulty   difficulty = parseEnum(Difficulty.class,     headers, "DIFFICULTY", source);
        String       topic      = require(headers, "TOPIC",  source);
        String       unit       = require(headers, "UNIT",   source);

        return new Question(type, topic, unit, difficulty, prompt, code, key);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static boolean isSectionMarker(String line) {
        return line.equals(PROMPT_MARKER) || line.equals(CODE_MARKER)
            || line.equals(KEY_MARKER)    || line.equals(END_MARKER);
    }

    private static String require(Map<String, String> h, String key, String src) {
        String v = h.get(key);
        if (v == null || v.isBlank())
            throw new IllegalArgumentException("Missing header '" + key + "' in " + src);
        return v;
    }

    private static <E extends Enum<E>> E parseEnum(
            Class<E> cls, Map<String, String> h, String key, String src) {
        String raw = require(h, key, src);
        try {
            return Enum.valueOf(cls, raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Unknown " + key + " value '" + raw + "' in " + src
                + ". Valid: " + Arrays.toString(cls.getEnumConstants()));
        }
    }
}
