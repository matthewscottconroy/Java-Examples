package com.generics.erasure;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        // ----------------------------------------------------------------
        // PART 1: Same class at runtime
        // ----------------------------------------------------------------
        System.out.println("=== 1. Box<String> and Box<Integer> are the same class ===");
        ErasureDemo.showSameClass();

        // ----------------------------------------------------------------
        // PART 2: instanceof with wildcards (parameterized types not allowed)
        // ----------------------------------------------------------------
        System.out.println("\n=== 2. instanceof — only wildcard form is legal ===");
        ErasureDemo.showInstanceOf(new ErasureDemo.Box<>("hello"));
        ErasureDemo.showInstanceOf("I am a String, not a Box");

        // ----------------------------------------------------------------
        // PART 3: List<String> and List<Integer> erase to the same List
        // ----------------------------------------------------------------
        System.out.println("\n=== 3. List erasure and unchecked cast exposure ===");
        ErasureDemo.showListErasure();

        // ----------------------------------------------------------------
        // PART 4: Bridge methods (inspect via reflection)
        // ----------------------------------------------------------------
        System.out.println("\n=== 4. Compiler-generated bridge methods ===");
        ErasureDemo.showBridgeMethods();

        // ----------------------------------------------------------------
        // PART 5: Restrictions
        // ----------------------------------------------------------------
        System.out.println("\n=== 5. Restrictions caused by erasure ===");
        ErasureDemo.showRestrictions();

        // ----------------------------------------------------------------
        // PART 6: Type tokens — recovering type info at runtime
        // ----------------------------------------------------------------
        System.out.println("\n=== 6. Type token: Class<T> ===");

        // Create instances of arbitrary types without writing 'new' explicitly.
        StringBuilder sb = TypeToken.newInstance(StringBuilder.class);
        sb.append("created via Class<T> token");
        System.out.println(sb);

        // Typesafe heterogeneous container.
        TypeToken.TypedMap map = new TypeToken.TypedMap();
        map.put(String.class,  "hello");
        map.put(Integer.class, 42);
        map.put(Double.class,  3.14);

        String  s = map.get(String.class);      // no cast at the call site
        int     n = map.get(Integer.class);     // auto-unboxed
        double  d = map.get(Double.class);
        System.out.println("String=" + s + "  Integer=" + n + "  Double=" + d);

        // ----------------------------------------------------------------
        // PART 7: Super-type token — capturing List<String> as a Type
        // ----------------------------------------------------------------
        System.out.println("\n=== 7. Super-type token: capturing parameterized types ===");

        // The anonymous subclass {} captures List<String> in its superclass metadata.
        TypeToken.SuperTypeToken<List<String>> token =
                new TypeToken.SuperTypeToken<List<String>>() {};

        System.out.println("Captured type: " + token.getType().getTypeName());
        System.out.println("Token object:  " + token);
        // In a real framework (Jackson, Guava) this token would be used to
        // deserialize JSON into the correct parameterized type at runtime.
    }
}
