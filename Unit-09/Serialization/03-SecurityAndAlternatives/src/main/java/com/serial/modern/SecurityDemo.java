package com.serial.modern;

import java.io.*;

/**
 * Deserialization security and {@link ObjectInputFilter}.
 *
 * <p><strong>The vulnerability:</strong> {@code ObjectInputStream.readObject()} is
 * a remote code execution gadget.  When an attacker controls the byte stream,
 * they can craft a payload that — when deserialized — chains method calls in
 * classes already on the classpath to execute arbitrary OS commands.  This does
 * not require any bug in your code; the exploit lives in library classes (Apache
 * Commons Collections, Spring, Jackson, etc.) that happen to be on the classpath.
 *
 * <p>Real-world examples: Log4Shell-adjacent, WebLogic RCE (CVE-2015-4852),
 * Jenkins RCE, JBoss RCE — all via Java deserialization.
 *
 * <p><strong>The fix:</strong> never deserialize data from an untrusted source.
 * If you must, use {@link ObjectInputFilter} (JEP 290, Java 9+) to allowlist
 * the exact classes you expect.  Any class not on the allowlist causes an
 * immediate {@link InvalidClassException} <em>before</em> the object is constructed.
 *
 * <p>The allowlist filter runs before object construction — the gadget chain
 * is broken because the attacker's chosen class never gets instantiated.
 */
public class SecurityDemo {

    // -----------------------------------------------------------------------
    // A safe allowlist filter — permits only the classes we explicitly expect
    // -----------------------------------------------------------------------
    static ObjectInputFilter allowlistFilter(Class<?>... allowed) {
        return filterInfo -> {
            Class<?> clazz = filterInfo.serialClass();
            if (clazz == null) return ObjectInputFilter.Status.ALLOWED;   // metadata checks pass

            for (Class<?> ok : allowed) {
                if (ok.isAssignableFrom(clazz)) return ObjectInputFilter.Status.ALLOWED;
            }
            System.out.println("  FILTER REJECTED: " + clazz.getName());
            return ObjectInputFilter.Status.REJECTED;
        };
    }

    // A simple data class we actually want to deserialize.
    record SafeData(String value) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }

    // Simulate a "suspicious" class that would not be in a real allowlist.
    static class SuspiciousClass implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
        private final String payload;
        SuspiciousClass(String payload) { this.payload = payload; }
    }

    public static void demonstrateFilter() throws Exception {
        System.out.println("-- ObjectInputFilter: allowlist approach --");

        // Serialize both a safe and a suspicious object.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new SafeData("legitimate payload"));
        }
        byte[] safeBytes = bos.toByteArray();

        bos.reset();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new SuspiciousClass("attacker-controlled"));
        }
        byte[] suspiciousBytes = bos.toByteArray();

        // Deserialize SafeData — allowed.
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(safeBytes))) {
            ois.setObjectInputFilter(allowlistFilter(SafeData.class));
            SafeData result = (SafeData) ois.readObject();
            System.out.println("  SafeData passed filter: " + result);
        }

        // Deserialize SuspiciousClass — rejected.
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(suspiciousBytes))) {
            ois.setObjectInputFilter(allowlistFilter(SafeData.class));   // allowlist has only SafeData
            ois.readObject();
        } catch (InvalidClassException e) {
            System.out.println("  SuspiciousClass blocked: " + e.getMessage());
        }
    }

    public static void demonstratePatternFilter() throws Exception {
        System.out.println("\n-- Pattern-based filter (class name matching) --");

        // Java also supports a string-based filter DSL:
        //   "com.myapp.*;java.lang.*;!*"
        //   means: allow com.myapp.*, allow java.lang.*, reject everything else.
        ObjectInputFilter filter = ObjectInputFilter.Config.createFilter(
                "com.serial.modern.*;" +
                "java.lang.*;" +
                "!*");    // reject anything not matched above

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new SafeData("pattern filter test"));
        }

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            ois.setObjectInputFilter(filter);
            Object obj = ois.readObject();
            System.out.println("  allowed through pattern filter: " + obj);
        }
    }
}
