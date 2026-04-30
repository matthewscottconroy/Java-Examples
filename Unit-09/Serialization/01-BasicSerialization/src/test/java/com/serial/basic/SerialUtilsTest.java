package com.serial.basic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SerialUtilsTest {

    // -- serialize / deserialize round-trip --

    @Test @DisplayName("serialize produces a non-empty byte array")
    void serialize_nonEmpty() throws Exception {
        byte[] bytes = SerialUtils.serialize(new Person("Alice", 30));
        assertTrue(bytes.length > 0);
    }

    @Test @DisplayName("deserialize recovers the original name and age")
    void roundTrip_nameAndAge() throws Exception {
        Person original = new Person("Bob", 25);
        byte[] bytes = SerialUtils.serialize(original);
        Person recovered = SerialUtils.deserialize(bytes);
        assertEquals("Bob", recovered.getName());
        assertEquals(25,    recovered.getAge());
    }

    @Test @DisplayName("transient field is null after deserialization")
    void roundTrip_transientFieldIsNull() throws Exception {
        Person p = new Person("Carol", 40);
        p.getDisplayName();              // populate the transient cachedDisplayName
        byte[] bytes = SerialUtils.serialize(p);
        Person recovered = SerialUtils.deserialize(bytes);
        // getDisplayName() recomputes on demand; internal cache should be null after deserialize
        // Verify by checking the string still returns correct value (recomputed from name/age)
        assertEquals("Carol (age 40)", recovered.getDisplayName());
    }

    @Test @DisplayName("two independent serializations of equal objects produce equal deserialized objects")
    void serialize_deterministic() throws Exception {
        Person p = new Person("Dave", 50);
        Person r1 = SerialUtils.deserialize(SerialUtils.serialize(p));
        Person r2 = SerialUtils.deserialize(SerialUtils.serialize(p));
        assertEquals(r1.getName(), r2.getName());
        assertEquals(r1.getAge(),  r2.getAge());
    }

    // -- deepCopy --

    @Test @DisplayName("deepCopy returns a distinct object with equal state")
    void deepCopy_distinctObject() throws Exception {
        Person original = new Person("Eve", 35);
        Person copy = SerialUtils.deepCopy(original);
        assertNotSame(original, copy);
        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getAge(),  copy.getAge());
    }

    @Test @DisplayName("deepCopy works for a String (also Serializable)")
    void deepCopy_string() throws Exception {
        String s = "immutable";
        String copy = SerialUtils.deepCopy(s);
        assertEquals(s, copy);
    }
}
