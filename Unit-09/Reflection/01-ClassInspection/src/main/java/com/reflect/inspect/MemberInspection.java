package com.reflect.inspect;

import java.lang.reflect.*;
import java.util.Arrays;

/**
 * Inspecting the members of a class: fields, methods, and constructors.
 *
 * <p><strong>getDeclared* vs get*</strong>:
 * <ul>
 *   <li>{@code getDeclaredFields()}  — all fields declared in THIS class, any access level</li>
 *   <li>{@code getFields()}          — only PUBLIC fields, including inherited ones</li>
 *   <li>Same distinction applies to getMethods / getDeclaredMethods,
 *       getConstructors / getDeclaredConstructors</li>
 * </ul>
 *
 * <p>The {@link Modifier} class decodes the int modifier bitmask into
 * human-readable flags (public, private, static, final, …).
 */
public class MemberInspection {

    @SuppressWarnings("unused")
    static class Employee {
        public  static final String COMPANY = "Acme Corp";
        private final String name;
        protected int        salary;
        private transient String cachedSummary;

        public Employee(String name, int salary) {
            this.name   = name;
            this.salary = salary;
        }

        protected Employee(String name) { this(name, 0); }

        public    String getName()   { return name;   }
        public    int    getSalary() { return salary; }
        protected void   setSalary(int s) { salary = s; }
        private   String buildSummary() { return name + "@" + salary; }
        public    @Override String toString() { return "Employee[" + name + "," + salary + "]"; }
    }

    public static void showFields() {
        System.out.println("-- getDeclaredFields() — all fields in this class --");
        for (Field f : Employee.class.getDeclaredFields()) {
            System.out.printf("  %-10s %-25s %s%n",
                    Modifier.toString(f.getModifiers()), f.getType().getSimpleName(), f.getName());
        }
    }

    public static void showMethods() {
        System.out.println("\n-- getDeclaredMethods() — all methods in this class --");
        for (Method m : Employee.class.getDeclaredMethods()) {
            String params = Arrays.stream(m.getParameterTypes())
                    .map(Class::getSimpleName)
                    .reduce("", (a, b) -> a.isEmpty() ? b : a + ", ");
            System.out.printf("  %-20s %-10s %s(%s)%n",
                    Modifier.toString(m.getModifiers()),
                    m.getReturnType().getSimpleName(),
                    m.getName(), params);
        }

        System.out.println("\n-- getMethod() also finds inherited public methods --");
        // toString() is declared in Object but accessible via Employee.class
        try {
            Method ts = Employee.class.getMethod("toString");
            System.out.println("  toString declared in: " + ts.getDeclaringClass().getSimpleName());
        } catch (NoSuchMethodException e) { System.out.println(e); }
    }

    public static void showConstructors() {
        System.out.println("\n-- getDeclaredConstructors() --");
        for (Constructor<?> c : Employee.class.getDeclaredConstructors()) {
            String params = Arrays.stream(c.getParameterTypes())
                    .map(Class::getSimpleName)
                    .reduce("", (a, b) -> a.isEmpty() ? b : a + ", ");
            System.out.printf("  %-12s %s(%s)%n",
                    Modifier.toString(c.getModifiers()),
                    c.getDeclaringClass().getSimpleName(), params);
        }
    }
}
