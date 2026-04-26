package com.reflect.generics;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

/**
 * Generic type information that survives erasure.
 *
 * <p>Type erasure removes type arguments from <em>instances</em>, but the
 * compiler preserves generic signatures in the <em>class file metadata</em>
 * of fields, method signatures, and superclass declarations.  This metadata
 * is exposed through four interfaces:
 *
 * <ul>
 *   <li>{@link ParameterizedType} — e.g. {@code List<String>}, {@code Map<K,V>}</li>
 *   <li>{@link TypeVariable}      — e.g. {@code T}, {@code E extends Comparable<E>}</li>
 *   <li>{@link WildcardType}      — e.g. {@code ? extends Number}, {@code ? super Integer}</li>
 *   <li>{@link GenericArrayType}  — e.g. {@code T[]}, {@code List<String>[]}</li>
 * </ul>
 *
 * <p>This is how Jackson knows to deserialize a {@code List<Order>} vs
 * {@code List<Product>} — it reads the field's generic type from the class file.
 */
public class TypeInspection {

    // A class with fields that carry rich generic signatures.
    @SuppressWarnings("unused")
    static class Repository<E extends Comparable<E>> {
        private List<E>                           items;
        private Map<String, List<E>>              grouped;
        private Map<? extends Number, ? super E>  wildcardMap;
        private E[]                               array;
        private Function<E, List<String>>         transformer;
    }

    // Subclass pins E = Integer — the binding is readable from getGenericSuperclass().
    static class IntegerRepository extends Repository<Integer> {}

    static String describeType(Type t) {
        return describeType(t, 0);
    }

    // depth guard prevents infinite recursion for self-referential bounds like
    // E extends Comparable<E> — the bound contains E, which has the same bound, etc.
    static String describeType(Type t, int depth) {
        if (depth > 3) return t instanceof Class<?> c ? c.getSimpleName() : "...";
        return switch (t) {
            case ParameterizedType pt -> {
                String raw  = ((Class<?>) pt.getRawType()).getSimpleName();
                String args = Arrays.stream(pt.getActualTypeArguments())
                              .map(a -> describeType(a, depth + 1))
                              .reduce((a, b) -> a + ", " + b).orElse("");
                yield raw + "<" + args + ">";
            }
            case TypeVariable<?> tv -> {
                String bounds = Arrays.stream(tv.getBounds())
                                .map(b -> describeType(b, depth + 1))
                                .reduce((a, b) -> a + " & " + b).orElse("Object");
                yield tv.getName() + (bounds.equals("Object") ? "" : " extends " + bounds);
            }
            case WildcardType wt -> {
                if (wt.getUpperBounds().length > 0 && !wt.getUpperBounds()[0].equals(Object.class))
                    yield "? extends " + describeType(wt.getUpperBounds()[0], depth + 1);
                if (wt.getLowerBounds().length > 0)
                    yield "? super " + describeType(wt.getLowerBounds()[0], depth + 1);
                yield "?";
            }
            case GenericArrayType ga -> describeType(ga.getGenericComponentType(), depth + 1) + "[]";
            case Class<?> c          -> c.getSimpleName();
            default                  -> t.toString();
        };
    }

    public static void showFieldTypes() throws Exception {
        System.out.println("-- Generic field signatures in Repository<E> --");
        for (Field f : Repository.class.getDeclaredFields()) {
            Type generic = f.getGenericType();
            System.out.printf("  %-20s %s  [%s]%n",
                    f.getName(), describeType(generic),
                    generic.getClass().getSimpleName());
        }
    }

    public static void showSuperclassBinding() {
        System.out.println("\n-- getGenericSuperclass(): recover E = Integer --");
        Type superclass = IntegerRepository.class.getGenericSuperclass();
        System.out.println("  generic superclass: " + describeType(superclass));

        // Extract the actual type argument — this is the super-type-token pattern.
        ParameterizedType pt = (ParameterizedType) superclass;
        Type actualE = pt.getActualTypeArguments()[0];
        System.out.println("  actual E: " + describeType(actualE)
                + "  (class: " + actualE.getClass().getSimpleName() + ")");
    }

    public static void showMethodSignatures() throws NoSuchMethodException {
        System.out.println("\n-- Generic method return/parameter types --");
        // Use a method from Collections for a good example.
        Method sort = Collections.class.getMethod("sort", List.class, Comparator.class);
        System.out.println("  Collections.sort generic signature:");
        System.out.print  ("    return type:  " + describeType(sort.getGenericReturnType()));
        System.out.println();
        for (int i = 0; i < sort.getGenericParameterTypes().length; i++) {
            System.out.println("    param[" + i + "]:    " + describeType(sort.getGenericParameterTypes()[i]));
        }
        System.out.println("  type parameters: " + Arrays.toString(sort.getTypeParameters()));
    }
}
