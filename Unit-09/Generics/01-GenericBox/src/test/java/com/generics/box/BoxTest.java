package com.generics.box;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoxTest {

    @Test @DisplayName("get() returns the value passed to the constructor")
    void get_returnsConstructorValue() {
        Box<String> box = new Box<>("hello");
        assertEquals("hello", box.get());
    }

    @Test @DisplayName("set() replaces the stored value")
    void set_replacesValue() {
        Box<Integer> box = new Box<>(1);
        box.set(42);
        assertEquals(42, box.get());
    }

    @Test @DisplayName("Box works with any reference type (Integer)")
    void generic_integer() {
        Box<Integer> box = new Box<>(100);
        assertEquals(100, box.get());
    }

    @Test @DisplayName("Box can store null")
    void get_null() {
        Box<String> box = new Box<>(null);
        assertNull(box.get());
    }

    @Test @DisplayName("toString formats as Box[value]")
    void toString_format() {
        assertEquals("Box[42]", new Box<>(42).toString());
        assertEquals("Box[hi]", new Box<>("hi").toString());
    }

    @Test @DisplayName("set() then get() reflects the new value")
    void set_thenGet() {
        Box<String> box = new Box<>("old");
        box.set("new");
        assertEquals("new", box.get());
    }
}
