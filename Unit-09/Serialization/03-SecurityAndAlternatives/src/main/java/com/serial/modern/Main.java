package com.serial.modern;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("=== Deserialization Security + ObjectInputFilter ===");
        SecurityDemo.demonstrateFilter();
        SecurityDemo.demonstratePatternFilter();

        System.out.println("\n=== Modern Alternatives to Serializable ===");
        ModernAlternatives.showRecordJson();
        ModernAlternatives.showManualBinary();
        ModernAlternatives.showToStringSerialization();

        System.out.println("\n=== Decision guide ===");
        System.out.println("  Serializable            → legacy code, short-lived in-process caches only");
        System.out.println("  Serializable + filter   → if you must deserialize untrusted data");
        System.out.println("  Records + Jackson/Gson  → new designs: REST APIs, config files, persistence");
        System.out.println("  Protobuf / Avro         → cross-language, schema-governed, high throughput");
        System.out.println("  DataOutputStream        → simple custom binary formats, no library needed");

        System.out.println("\n=== Java's own guidance ===");
        System.out.println("  JEP 154 (Java 9): ObjectInputFilter added");
        System.out.println("  JEP 290 (Java 9): serialization filtering framework");
        System.out.println("  JEP 411 (Java 17+): Security Manager deprecated");
        System.out.println("  Long-term direction: records + external formats replace Serializable");
    }
}
