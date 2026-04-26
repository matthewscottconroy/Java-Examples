package com.serial.basic;

import java.io.*;
import java.util.Arrays;
import java.util.HexFormat;

public class Main {

    public static void main(String[] args) throws Exception {

        // ----------------------------------------------------------------
        // Basic round-trip: serialize → bytes → deserialize
        // ----------------------------------------------------------------
        System.out.println("=== Basic round-trip ===");
        Person alice = new Person("Alice", 30);
        System.out.println("Before: " + alice.getDisplayName());

        byte[] bytes = SerialUtils.serialize(alice);
        System.out.println("Serialized to " + bytes.length + " bytes");

        // Peek at the header: 0xACED is the Java stream magic; 0x0005 is the version.
        System.out.println("First 4 bytes (hex): " + HexFormat.of().withUpperCase().formatHex(bytes, 0, 4));
        System.out.println("  (0xACED = stream magic, 0x0005 = stream version)");

        Person restored = SerialUtils.deserialize(bytes);
        System.out.println("After:  " + restored);

        // ----------------------------------------------------------------
        // transient fields reset to default after deserialization
        // ----------------------------------------------------------------
        System.out.println("\n=== transient field behaviour ===");
        System.out.println("cachedDisplayName before serialize: " + alice.getDisplayName());
        // Warm up the cache.
        alice.getDisplayName();
        Person copy = SerialUtils.deserialize(SerialUtils.serialize(alice));
        System.out.println("cachedDisplayName after  restore:  " + copy.toString());
        System.out.println("  (null — transient fields are excluded from the byte stream)");
        System.out.println("  First call recomputes it: " + copy.getDisplayName());

        // ----------------------------------------------------------------
        // static fields are NOT serialized
        // ----------------------------------------------------------------
        System.out.println("\n=== static fields (not serialized) ===");
        int before = Person.getInstanceCount();
        System.out.println("instanceCount before restore: " + before);
        // Deserializing does NOT call the constructor → static count unchanged.
        SerialUtils.<Person>deserialize(bytes);
        System.out.println("instanceCount after  restore: " + Person.getInstanceCount());
        System.out.println("  (unchanged — deserialization bypasses the constructor)");

        // ----------------------------------------------------------------
        // Deep copy via serialization
        // ----------------------------------------------------------------
        System.out.println("\n=== Deep copy ===");
        Person original = new Person("Bob", 25);
        Person deepCopy = SerialUtils.deepCopy(original);
        System.out.println("original == deepCopy? " + (original == deepCopy));
        System.out.println("original:  " + original);
        System.out.println("deepCopy:  " + deepCopy);

        // ----------------------------------------------------------------
        // Serialize to a real file — same API, different stream
        // ----------------------------------------------------------------
        System.out.println("\n=== Serialize to a file ===");
        File file = File.createTempFile("person-", ".ser");
        file.deleteOnExit();

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(alice);
            oos.writeObject(new Person("Charlie", 22));
        }
        System.out.println("Wrote two Person objects to: " + file.getName());

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            System.out.println("Read: " + ois.readObject());
            System.out.println("Read: " + ois.readObject());
        }
    }
}
