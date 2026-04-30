package com.meta.handles;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.invoke.*;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class MethodHandlesTest {

    // ---------------------------------------------------------------
    // Virtual handles
    // ---------------------------------------------------------------

    @Test @DisplayName("Virtual handle invokes String.toUpperCase()")
    void virtual_toUpperCase() throws Throwable {
        MethodHandle mh = Handles.upperCaseHandle();
        assertEquals("HELLO", (String) mh.invoke("hello"));
    }

    @Test @DisplayName("Virtual handle invokes String.substring(int,int)")
    void virtual_substring() throws Throwable {
        MethodHandle mh = Handles.substringHandle();
        assertEquals("bcd", (String) mh.invoke("abcdef", 1, 4));
    }

    // ---------------------------------------------------------------
    // Static handles
    // ---------------------------------------------------------------

    @Test @DisplayName("Static handle invokes Integer.parseInt(String)")
    void static_parseInt() throws Throwable {
        MethodHandle mh = Handles.parseIntHandle();
        assertEquals(123, (int) mh.invoke("123"));
    }

    @Test @DisplayName("Static handle parseInt throws for invalid input")
    void static_parseInt_invalid() throws Throwable {
        MethodHandle mh = Handles.parseIntHandle();
        assertThrows(NumberFormatException.class, () -> mh.invoke("xyz"));
    }

    // ---------------------------------------------------------------
    // Constructor handles
    // ---------------------------------------------------------------

    @Test @DisplayName("Constructor handle creates new StringBuilder")
    void constructor_stringBuilder() throws Throwable {
        MethodHandle mh = Handles.stringBuilderHandle();
        Object obj = mh.invoke("test");
        assertInstanceOf(StringBuilder.class, obj);
        assertEquals("test", obj.toString());
    }

    // ---------------------------------------------------------------
    // filterArguments
    // ---------------------------------------------------------------

    @Test @DisplayName("filterArguments trims both args before concat")
    void filterArgs_trimsBeforeConcat() throws Throwable {
        MethodHandle mh = Handles.trimBeforeConcat();
        String r = (String) mh.invoke("  hello  ", "  world  ");
        assertEquals("helloworld", r);
    }

    @Test @DisplayName("filterArguments leaves already-trimmed args unchanged")
    void filterArgs_noChangeWhenAlreadyTrimmed() throws Throwable {
        MethodHandle mh = Handles.trimBeforeConcat();
        assertEquals("ab", (String) mh.invoke("a", "b"));
    }

    // ---------------------------------------------------------------
    // filterReturnValue
    // ---------------------------------------------------------------

    @Test @DisplayName("filterReturnValue uppercases result of concat")
    void filterReturn_concatThenUpper() throws Throwable {
        MethodHandle mh = Handles.concatThenUpper();
        assertEquals("FOOBAR", (String) mh.invoke("foo", "bar"));
    }

    // ---------------------------------------------------------------
    // bindTo (partial application)
    // ---------------------------------------------------------------

    @Test @DisplayName("bindTo creates a partially-applied handle")
    void bindTo_prefix() throws Throwable {
        MethodHandle mh = Handles.prefixWith("Hi, ");
        assertEquals("Hi, Alice", (String) mh.invoke("Alice"));
        assertEquals("Hi, Bob",   (String) mh.invoke("Bob"));
    }

    @Test @DisplayName("bindTo handle has arity one less than original")
    void bindTo_reducedArity() throws Throwable {
        MethodHandle mh = Handles.prefixWith("x");
        assertEquals(1, mh.type().parameterCount());
    }

    // ---------------------------------------------------------------
    // guardWithTest
    // ---------------------------------------------------------------

    @Test @DisplayName("guardWithTest routes long strings to uppercase")
    void guard_longToUpper() throws Throwable {
        MethodHandle mh = Handles.longOrShortLabel();
        assertEquals("ELEPHANT", (String) mh.invoke("elephant")); // length 8 > 5
    }

    @Test @DisplayName("guardWithTest routes short strings to lowercase")
    void guard_shortToLower() throws Throwable {
        MethodHandle mh = Handles.longOrShortLabel();
        assertEquals("hi", (String) mh.invoke("HI")); // length 2 ≤ 5
    }

    @Test @DisplayName("isLong predicate: 5-char string is not long")
    void isLong_boundary() {
        assertFalse(Handles.isLong("hello")); // exactly 5, not > 5
        assertTrue(Handles.isLong("helloo")); // 6
    }

    // ---------------------------------------------------------------
    // asOperator
    // ---------------------------------------------------------------

    @Test @DisplayName("asOperator wraps handle in UnaryOperator")
    void asOperator_works() throws Throwable {
        MethodHandle mh = Handles.upperCaseHandle();
        UnaryOperator<String> op = Handles.asOperator(mh);
        assertEquals("WORLD", op.apply("world"));
    }

    // ---------------------------------------------------------------
    // MethodType inspection
    // ---------------------------------------------------------------

    @Test @DisplayName("MethodHandle.type() reflects correct signature")
    void methodType_inspection() throws Throwable {
        MethodHandle mh = Handles.upperCaseHandle();
        MethodType type = mh.type();
        assertEquals(String.class, type.returnType());
        assertEquals(String.class, type.parameterType(0)); // receiver
        assertEquals(1, type.parameterCount());
    }

    @Test @DisplayName("Lookup finds public method by name and type")
    void lookup_findVirtual() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle length = lookup.findVirtual(String.class, "length",
            MethodType.methodType(int.class));
        assertEquals(5, (int) length.invoke("hello"));
    }
}
