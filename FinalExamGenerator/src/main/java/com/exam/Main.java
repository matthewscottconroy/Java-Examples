package com.exam;

import java.util.List;

/**
 * Entry point for the Final Exam Generator.
 *
 * <pre>
 *   mvn exec:java                                        # 2 per section, random seed
 *   mvn exec:java -Dexec.args="--per-type 3"             # 3 per section
 *   mvn exec:java -Dexec.args="--seed 42 --key"          # reproducible + answer key
 *   mvn exec:java -Dexec.args="--topic Generics"         # filter to one topic
 *   mvn exec:java -Dexec.args="--versions 3"             # generate Versions A, B, C
 *   mvn exec:java -Dexec.args="--versions 3 --seed 1 --key"
 * </pre>
 */
public class Main {

    public static void main(String[] args) {
        Config cfg = Config.parse(args);
        ExamGenerator gen = new ExamGenerator(cfg.perType(), cfg.seed(), cfg.topic());

        if (cfg.versions() == 1) {
            List<Question> questions = gen.generate();
            System.out.println(ExamFormatter.format(questions, cfg.includeKey()));
            System.err.printf("(seed=%d  per-type=%d  questions=%d)%n",
                cfg.seed(), cfg.perType(), questions.size());
        } else {
            // Multi-version: derive each version's seed from the base seed so
            // the set of exams is still reproducible when --seed is supplied.
            for (int v = 0; v < cfg.versions(); v++) {
                long versionSeed = cfg.seed() + (long) v * 1_000_003L;
                String label = String.valueOf((char) ('A' + v));
                List<Question> questions = gen.generate(versionSeed);
                System.out.println(ExamFormatter.format(questions, cfg.includeKey(), label));
                if (v < cfg.versions() - 1) System.out.println("\f"); // page break between versions
            }
            System.err.printf("(base-seed=%d  versions=%d  per-type=%d)%n",
                cfg.seed(), cfg.versions(), cfg.perType());
        }
    }

    // -------------------------------------------------------------------------

    record Config(int perType, long seed, boolean includeKey, String topic, int versions) {

        static Config parse(String[] args) {
            int     perType    = 2;
            long    seed       = System.currentTimeMillis();
            boolean includeKey = false;
            String  topic      = null;
            int     versions   = 1;

            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--per-type" -> perType    = Integer.parseInt(args[++i]);
                    case "--seed"     -> seed        = Long.parseLong(args[++i]);
                    case "--key"      -> includeKey  = true;
                    case "--topic"    -> topic        = args[++i];
                    case "--versions" -> versions     = Integer.parseInt(args[++i]);
                    default           -> System.err.println("Unknown flag: " + args[i]);
                }
            }
            if (versions < 1 || versions > 26) {
                System.err.println("--versions must be 1–26; defaulting to 1");
                versions = 1;
            }
            return new Config(perType, seed, includeKey, topic, versions);
        }
    }
}
