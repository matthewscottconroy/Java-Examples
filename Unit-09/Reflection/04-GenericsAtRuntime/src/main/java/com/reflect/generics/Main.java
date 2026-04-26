package com.reflect.generics;

import java.lang.reflect.*;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("=== Instance-level erasure (what's gone) ===");
        List<String>  ls = List.of("a");
        List<Integer> li = List.of(1);
        System.out.println("  List<String>.getClass()  == List<Integer>.getClass(): "
                + (ls.getClass() == li.getClass()));   // true — instance type args erased

        System.out.println("\n=== Class-file-level preservation (what survives) ===");
        TypeInspection.showFieldTypes();
        TypeInspection.showSuperclassBinding();
        TypeInspection.showMethodSignatures();

        System.out.println("\n=== Class type parameters ===");
        // The declared type parameter T on Repository is a TypeVariable.
        for (TypeVariable<?> tv : TypeInspection.Repository.class.getTypeParameters()) {
            System.out.println("  TypeVariable: " + tv.getName()
                    + "  bounds: " + java.util.Arrays.toString(tv.getBounds()));
        }

        System.out.println("\n=== Practical application: generic field type reader ===");
        // Simulate what Jackson does: read the element type of a List field.
        record Payload(List<String> tags, List<Integer> counts) {}
        for (var field : Payload.class.getDeclaredFields()) {
            Type generic = field.getGenericType();
            if (generic instanceof ParameterizedType pt) {
                Type element = pt.getActualTypeArguments()[0];
                System.out.println("  " + field.getName() + " → element type: " + element.getTypeName());
            }
        }
    }
}
