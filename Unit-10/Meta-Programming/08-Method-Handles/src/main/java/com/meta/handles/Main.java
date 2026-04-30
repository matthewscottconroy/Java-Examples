package com.meta.handles;

import java.lang.invoke.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Main {

    public static void main(String[] args) throws Throwable {
        System.out.println("=== Method Handles — Low-Level Reflective Invocation ===\n");

        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // ---------------------------------------------------------------
        // 1. Virtual handle — String.toUpperCase()
        // ---------------------------------------------------------------
        System.out.println("--- 1. Virtual method handle ---");
        MethodHandle upper = Handles.upperCaseHandle();
        String result = (String) upper.invoke("hello, world");
        System.out.println("  toUpperCase: " + result);

        // ---------------------------------------------------------------
        // 2. Handle with arguments — String.substring(int, int)
        // ---------------------------------------------------------------
        System.out.println("\n--- 2. Method handle with multiple args ---");
        MethodHandle sub = Handles.substringHandle();
        String slice = (String) sub.invoke("abcdefgh", 2, 5);
        System.out.println("  substring(2,5): " + slice);

        // ---------------------------------------------------------------
        // 3. Static handle — Integer.parseInt
        // ---------------------------------------------------------------
        System.out.println("\n--- 3. Static method handle ---");
        MethodHandle parseInt = Handles.parseIntHandle();
        int n = (int) parseInt.invoke("42");
        System.out.println("  parseInt(\"42\"): " + n);

        // ---------------------------------------------------------------
        // 4. Constructor handle — new StringBuilder(String)
        // ---------------------------------------------------------------
        System.out.println("\n--- 4. Constructor handle ---");
        MethodHandle sbCtor = Handles.stringBuilderHandle();
        StringBuilder sb = (StringBuilder) sbCtor.invoke("initial text");
        System.out.println("  new StringBuilder: " + sb);

        // ---------------------------------------------------------------
        // 5. filterArguments — trim before concat
        // ---------------------------------------------------------------
        System.out.println("\n--- 5. filterArguments (trim before concat) ---");
        MethodHandle trimConcat = Handles.trimBeforeConcat();
        String joined = (String) trimConcat.invoke("  hello  ", "  world  ");
        System.out.println("  trimmed concat: \"" + joined + "\"");

        // ---------------------------------------------------------------
        // 6. filterReturnValue — concat then uppercase
        // ---------------------------------------------------------------
        System.out.println("\n--- 6. filterReturnValue (concat then uppercase) ---");
        MethodHandle concatUpper = Handles.concatThenUpper();
        String up = (String) concatUpper.invoke("foo", "bar");
        System.out.println("  concatThenUpper: " + up);

        // ---------------------------------------------------------------
        // 7. bindTo — partial application
        // ---------------------------------------------------------------
        System.out.println("\n--- 7. bindTo (partial application) ---");
        MethodHandle greet = Handles.prefixWith("Hello, ");
        System.out.println("  " + (String) greet.invoke("Alice"));
        System.out.println("  " + (String) greet.invoke("Bob"));

        // ---------------------------------------------------------------
        // 8. guardWithTest — inline branch
        // ---------------------------------------------------------------
        System.out.println("\n--- 8. guardWithTest (branch on length) ---");
        MethodHandle label = Handles.longOrShortLabel();
        for (String word : List.of("hi", "hello", "world!", "ok", "elephant")) {
            System.out.println("  \"" + word + "\" → " + (String) label.invoke(word));
        }

        // ---------------------------------------------------------------
        // 9. Convert handle to UnaryOperator and use with streams
        // ---------------------------------------------------------------
        System.out.println("\n--- 9. Handle as UnaryOperator (used in stream) ---");
        UnaryOperator<String> op = Handles.asOperator(upper);
        List<String> words = List.of("streams", "are", "fun");
        System.out.println("  " + words.stream().map(op).collect(Collectors.toList()));

        // ---------------------------------------------------------------
        // 10. Performance note: invoke vs invokeExact
        // ---------------------------------------------------------------
        System.out.println("\n--- 10. MethodType inspection ---");
        System.out.println("  upperCase type: " + upper.type());
        System.out.println("  substring type: " + sub.type());
        System.out.println("  parseInt  type: " + parseInt.type());
    }
}
