package com.reflect.inspect;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Every object carries a {@link Class} object at runtime.
 *
 * <p>Three ways to obtain a Class object:
 * <ul>
 *   <li>{@code obj.getClass()}        — from a live instance</li>
 *   <li>{@code String.class}          — class literal (preferred; compile-time safe)</li>
 *   <li>{@code Class.forName("...")}  — by fully-qualified name (dynamic; can throw)</li>
 * </ul>
 *
 * <p>The Class object exposes everything the compiler knew:
 * name, package, superclass, interfaces, modifiers, members.
 */
public class ClassMetadata {

    static abstract class AbstractVehicle implements Comparable<AbstractVehicle>, Cloneable {
        protected String make;
        abstract String describe();
        @Override public int compareTo(AbstractVehicle o) { return make.compareTo(o.make); }
    }

    static final class Car extends AbstractVehicle {
        private final int doors;
        Car(String make, int doors) { this.make = make; this.doors = doors; }
        @Override String describe() { return make + " (" + doors + " doors)"; }
    }

    public static void demonstrate() throws ClassNotFoundException {
        System.out.println("-- Three ways to get a Class object --");
        Car car = new Car("Toyota", 4);

        Class<?> fromInstance  = car.getClass();
        Class<?> fromLiteral   = Car.class;
        Class<?> fromForName   = Class.forName("com.reflect.inspect.ClassMetadata$Car");

        System.out.println("  fromInstance == fromLiteral: " + (fromInstance == fromLiteral));
        System.out.println("  fromLiteral  == fromForName: " + (fromLiteral  == fromForName));

        System.out.println("\n-- Class names --");
        System.out.println("  getName():           " + Car.class.getName());
        System.out.println("  getSimpleName():     " + Car.class.getSimpleName());
        System.out.println("  getCanonicalName():  " + Car.class.getCanonicalName());
        System.out.println("  getPackageName():    " + Car.class.getPackageName());

        System.out.println("\n-- Type checks --");
        System.out.println("  isInterface:  " + Car.class.isInterface());
        System.out.println("  isAbstract:   " + Modifier.isAbstract(AbstractVehicle.class.getModifiers()));
        System.out.println("  isFinal:      " + Modifier.isFinal(Car.class.getModifiers()));
        System.out.println("  isRecord:     " + Car.class.isRecord());
        System.out.println("  isEnum:       " + Car.class.isEnum());
        System.out.println("  isPrimitive:  " + int.class.isPrimitive());

        System.out.println("\n-- Superclass and interfaces --");
        System.out.println("  superclass:   " + Car.class.getSuperclass().getSimpleName());
        System.out.println("  interfaces:   " +
                Arrays.toString(Arrays.stream(AbstractVehicle.class.getInterfaces())
                      .map(Class::getSimpleName).toArray()));

        System.out.println("\n-- Full class hierarchy --");
        printHierarchy(Car.class, "  ");

        System.out.println("\n-- Primitive and array class objects --");
        System.out.println("  int.class:         " + int.class);
        System.out.println("  int[].class:       " + int[].class.getName());
        System.out.println("  String[].class:    " + String[].class.getSimpleName());
        System.out.println("  isArray(int[]):    " + int[].class.isArray());
        System.out.println("  componentType:     " + int[].class.getComponentType());
    }

    static void printHierarchy(Class<?> cls, String indent) {
        if (cls == null) return;
        System.out.println(indent + cls.getSimpleName()
                + (cls.isInterface() ? " (interface)" : ""));
        for (Class<?> iface : cls.getInterfaces()) {
            System.out.println(indent + "  implements " + iface.getSimpleName());
        }
        printHierarchy(cls.getSuperclass(), indent + "  ");
    }
}
