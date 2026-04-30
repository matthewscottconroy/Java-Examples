package com.functional.parallel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Generates a reproducible synthetic log dataset. */
public final class LogGenerator {

    private LogGenerator() {}

    private static final String[] LEVELS   = {"INFO", "INFO", "INFO", "WARN", "ERROR"};
    private static final String[] PATHS    = {"/api/users", "/api/orders", "/api/products",
                                              "/api/search", "/api/checkout", "/health"};
    private static final String[] USERS    = {"u001", "u002", "u003", "u004", null};
    private static final int[]    STATUSES = {200, 200, 200, 200, 301, 404, 500, 503};

    public static List<LogEntry> generate(int count, long seed) {
        Random rng = new Random(seed);
        List<LogEntry> entries = new ArrayList<>(count);
        LocalDateTime base = LocalDateTime.of(2024, 6, 1, 0, 0, 0);

        for (int i = 0; i < count; i++) {
            entries.add(new LogEntry(
                    base.plusSeconds(i * 3L + rng.nextInt(3)),
                    LEVELS[rng.nextInt(LEVELS.length)],
                    PATHS[rng.nextInt(PATHS.length)],
                    STATUSES[rng.nextInt(STATUSES.length)],
                    20 + rng.nextInt(980),   // 20–999 ms
                    USERS[rng.nextInt(USERS.length)]
            ));
        }
        return entries;
    }
}
