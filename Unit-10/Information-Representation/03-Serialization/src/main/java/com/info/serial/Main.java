package com.info.serial;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Serialization — Turning Objects into Bytes ===\n");

        Person alice = new Person("Alice", 30, "alice@example.com");
        Person bob   = new Person("Bob",   25, "bob@example.com");

        // ---------------------------------------------------------------
        // 1. Java object serialization
        // ---------------------------------------------------------------
        System.out.println("--- 1. Java object serialization ---");
        byte[] javaBytes = Serializers.javaSerialize(alice);
        Person recovered = Serializers.javaDeserialize(javaBytes);
        System.out.println("  Original  : " + alice);
        System.out.println("  Recovered : " + recovered);
        System.out.printf("  Size      : %d bytes%n", javaBytes.length);
        // Show the magic bytes header
        System.out.printf("  Header    : %02x %02x (Java object stream magic)%n",
            javaBytes[0] & 0xFF, javaBytes[1] & 0xFF);

        // ---------------------------------------------------------------
        // 2. Custom binary format
        // ---------------------------------------------------------------
        System.out.println("\n--- 2. Custom binary format (DataOutputStream) ---");
        byte[] binBytes = Serializers.binarySerialize(alice);
        Person fromBin  = Serializers.binaryDeserialize(binBytes);
        System.out.println("  Original  : " + alice);
        System.out.println("  Recovered : " + fromBin);
        System.out.printf("  Size      : %d bytes (vs %d for Java serialization)%n",
            binBytes.length, javaBytes.length);

        // ---------------------------------------------------------------
        // 3. Key-value text
        // ---------------------------------------------------------------
        System.out.println("\n--- 3. Key-value text format ---");
        String text = Serializers.textSerialize(alice);
        Person fromText = Serializers.textDeserialize(text);
        System.out.println("  Serialized: " + text);
        System.out.println("  Recovered : " + fromText);

        // Special characters in values
        Person tricky = new Person("O'Brien|Jr", 40, "o=b@x.com");
        String trickyText = Serializers.textSerialize(tricky);
        System.out.println("\n  Tricky name/email with special chars:");
        System.out.println("  Serialized: " + trickyText);
        System.out.println("  Recovered : " + Serializers.textDeserialize(trickyText));

        // ---------------------------------------------------------------
        // 4. Size comparison
        // ---------------------------------------------------------------
        System.out.println("\n--- Size comparison ---");
        System.out.printf("  %-25s %d bytes%n", "Java object stream:", javaBytes.length);
        System.out.printf("  %-25s %d bytes%n", "Custom binary:",      binBytes.length);
        System.out.printf("  %-25s %d bytes%n", "Key-value text:",     text.getBytes().length);
    }
}
