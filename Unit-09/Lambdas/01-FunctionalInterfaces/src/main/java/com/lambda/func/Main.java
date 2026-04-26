package com.lambda.func;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Built-in java.util.function interfaces ===");
        BuiltInInterfaces.demonstrate();

        System.out.println("\n=== Custom functional interfaces and checked exceptions ===");
        CustomFunctionalInterface.demonstrate();
    }
}
