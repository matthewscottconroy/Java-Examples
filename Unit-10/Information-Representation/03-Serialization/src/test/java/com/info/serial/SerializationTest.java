package com.info.serial;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SerializationTest {

    private static final Person ALICE = new Person("Alice", 30, "alice@example.com");
    private static final Person BOB   = new Person("Bob",   0,  "b@b.com");

    // -- Java object serialization --

    @Test @DisplayName("Java serialization round-trip preserves all fields")
    void java_roundTrip() throws Exception {
        byte[] bytes = Serializers.javaSerialize(ALICE);
        Person back  = Serializers.javaDeserialize(bytes);
        assertEquals(ALICE, back);
    }

    @Test @DisplayName("Java serialization header starts with AC ED (magic bytes)")
    void java_magicHeader() throws Exception {
        byte[] bytes = Serializers.javaSerialize(ALICE);
        assertEquals(0xAC, bytes[0] & 0xFF);
        assertEquals(0xED, bytes[1] & 0xFF);
    }

    @Test @DisplayName("Different objects produce different byte arrays")
    void java_differentObjects() throws Exception {
        byte[] a = Serializers.javaSerialize(ALICE);
        byte[] b = Serializers.javaSerialize(BOB);
        assertNotEquals(new String(a), new String(b));
    }

    // -- Custom binary serialization --

    @Test @DisplayName("Binary serialization round-trip")
    void binary_roundTrip() throws Exception {
        byte[] bytes = Serializers.binarySerialize(ALICE);
        assertEquals(ALICE, Serializers.binaryDeserialize(bytes));
    }

    @Test @DisplayName("Binary format is smaller than Java object stream")
    void binary_smallerThanJava() throws Exception {
        byte[] bin  = Serializers.binarySerialize(ALICE);
        byte[] java = Serializers.javaSerialize(ALICE);
        assertTrue(bin.length < java.length,
            "Binary (%d) should be smaller than Java stream (%d)".formatted(bin.length, java.length));
    }

    @Test @DisplayName("Binary round-trip preserves age=0")
    void binary_zeroAge() throws Exception {
        assertEquals(BOB, Serializers.binaryDeserialize(Serializers.binarySerialize(BOB)));
    }

    // -- Text serialization --

    @Test @DisplayName("Text serialization round-trip")
    void text_roundTrip() {
        assertEquals(ALICE, Serializers.textDeserialize(Serializers.textSerialize(ALICE)));
    }

    @Test @DisplayName("Text format is human-readable (contains field names)")
    void text_humanReadable() {
        String s = Serializers.textSerialize(ALICE);
        assertTrue(s.contains("name="));
        assertTrue(s.contains("age="));
        assertTrue(s.contains("email="));
    }

    @Test @DisplayName("Text format escapes pipe character in values")
    void text_escapedPipe() {
        Person p = new Person("A|B", 1, "x@x.com");
        String s = Serializers.textSerialize(p);
        assertEquals(p, Serializers.textDeserialize(s));
    }

    @Test @DisplayName("Text format escapes equals sign in values")
    void text_escapedEquals() {
        Person p = new Person("key=val", 1, "a=b@x.com");
        String s = Serializers.textSerialize(p);
        assertEquals(p, Serializers.textDeserialize(s));
    }

    @Test @DisplayName("Text round-trip preserves age=0")
    void text_zeroAge() {
        assertEquals(BOB, Serializers.textDeserialize(Serializers.textSerialize(BOB)));
    }
}
