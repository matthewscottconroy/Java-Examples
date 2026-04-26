package com.reflect.inspect;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("=== Class objects and metadata ===");
        ClassMetadata.demonstrate();

        System.out.println("\n=== Field, Method, Constructor inspection ===");
        MemberInspection.showFields();
        MemberInspection.showMethods();
        MemberInspection.showConstructors();

        System.out.println("\n=== Inspecting a JDK class: java.util.ArrayList ===");
        Class<?> al = Class.forName("java.util.ArrayList");
        System.out.println("  superclass:  " + al.getSuperclass().getSimpleName());
        System.out.println("  interfaces:  " + java.util.Arrays.toString(
                java.util.Arrays.stream(al.getInterfaces()).map(Class::getSimpleName).toArray()));
        System.out.println("  public methods count: " + al.getMethods().length);
        System.out.println("  declared fields:      " + al.getDeclaredFields().length);
    }
}
