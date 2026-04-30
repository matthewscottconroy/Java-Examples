package com.info.serial;

import java.io.*;

/**
 * Three serialization strategies for the same {@link Person} object,
 * each making different trade-offs between portability, size, and simplicity.
 *
 * <ol>
 *   <li><b>Java object streams</b> — zero boilerplate, JVM-only, includes class
 *       metadata; the output is opaque binary with a fixed header ({@code AC ED}).
 *   <li><b>Custom binary format</b> — hand-written with {@link DataOutputStream};
 *       compact, cross-language if documented, but fragile to schema changes.
 *   <li><b>Key-value text</b> — human-readable, easy to debug, trivially
 *       cross-language; larger than binary formats.
 * </ol>
 */
public final class Serializers {

    private Serializers() {}

    // ---------------------------------------------------------------
    // 1. Java object serialization
    // ---------------------------------------------------------------

    public static byte[] javaSerialize(Serializable obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <T> T javaDeserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) ois.readObject();
        }
    }

    // ---------------------------------------------------------------
    // 2. Custom binary format (DataOutputStream)
    // ---------------------------------------------------------------

    /**
     * Writes: UTF name, int age, UTF email.
     * {@link DataOutputStream#writeUTF} encodes length as a 2-byte prefix.
     */
    public static byte[] binarySerialize(Person p) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeUTF(p.name());
            dos.writeInt(p.age());
            dos.writeUTF(p.email());
        }
        return baos.toByteArray();
    }

    public static Person binaryDeserialize(byte[] bytes) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            return new Person(dis.readUTF(), dis.readInt(), dis.readUTF());
        }
    }

    // ---------------------------------------------------------------
    // 3. Key-value text format  (name=Alice|age=30|email=alice@x.com)
    // ---------------------------------------------------------------

    public static String textSerialize(Person p) {
        return "name=" + escape(p.name())
             + "|age=" + p.age()
             + "|email=" + escape(p.email());
    }

    public static Person textDeserialize(String s) {
        String name = null;
        int age = 0;
        String email = null;

        int i = 0;
        while (i < s.length()) {
            // Read key up to '='
            StringBuilder key = new StringBuilder();
            while (i < s.length() && s.charAt(i) != '=') key.append(s.charAt(i++));
            i++; // skip '='

            // Read value up to unescaped '|' or end-of-string
            StringBuilder val = new StringBuilder();
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c == '\\' && i + 1 < s.length()) {
                    val.append(s.charAt(i + 1));
                    i += 2;
                } else if (c == '|') {
                    i++; break;
                } else {
                    val.append(c); i++;
                }
            }

            switch (key.toString()) {
                case "name"  -> name  = val.toString();
                case "age"   -> age   = Integer.parseInt(val.toString());
                case "email" -> email = val.toString();
            }
        }
        return new Person(name, age, email);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("|", "\\|");
    }
}
