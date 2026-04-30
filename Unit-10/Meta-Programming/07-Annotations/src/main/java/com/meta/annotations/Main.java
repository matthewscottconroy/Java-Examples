package com.meta.annotations;

import java.lang.reflect.Field;
import java.util.Arrays;

// ---------------------------------------------------------------
// Sample domain classes annotated with constraints
// ---------------------------------------------------------------

class User {
    @Required(message = "Username must not be blank")
    String username;

    @Required
    @Pattern(regex = "^[\\w.+-]+@[\\w-]+\\.[a-z]{2,}$", message = "Must be a valid email")
    String email;

    @Range(min = 0, max = 150, message = "Age must be 0–150")
    int age;

    User(String username, String email, int age) {
        this.username = username;
        this.email    = email;
        this.age      = age;
    }
}

class Product {
    @Required
    String name;

    @Range(min = 0.01, max = 100_000.0, message = "Price must be 0.01–100000")
    double price;

    @Range(min = 0, max = 10_000, message = "Stock must be 0–10000")
    int stock;

    Product(String name, double price, int stock) {
        this.name  = name;
        this.price = price;
        this.stock = stock;
    }
}

// ---------------------------------------------------------------

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Runtime Annotations — Reflection-Based Validator ===\n");

        // 1. Inspect annotations declared on User
        System.out.println("--- Declared field annotations on User ---");
        for (Field f : User.class.getDeclaredFields()) {
            System.out.printf("  %-10s  %s%n", f.getName(), Arrays.toString(f.getAnnotations()));
        }

        // 2. Valid user
        System.out.println("\n--- Valid user ---");
        User valid = new User("alice", "alice@example.com", 30);
        printResult(valid);

        // 3. Blank username (Required violation)
        System.out.println("\n--- Blank username ---");
        User blankName = new User("", "alice@example.com", 30);
        printResult(blankName);

        // 4. Bad email (Pattern violation)
        System.out.println("\n--- Bad email ---");
        User badEmail = new User("bob", "not-an-email", 25);
        printResult(badEmail);

        // 5. Age out of range
        System.out.println("\n--- Age out of range ---");
        User badAge = new User("carol", "carol@x.org", 200);
        printResult(badAge);

        // 6. Multiple violations at once
        System.out.println("\n--- Multiple violations ---");
        User messy = new User(null, "???", -5);
        printResult(messy);

        // 7. Product examples
        System.out.println("\n--- Valid product ---");
        printResult(new Product("Widget", 9.99, 100));

        System.out.println("\n--- Invalid product (missing name, bad price, bad stock) ---");
        printResult(new Product(null, -1.0, 20_000));
    }

    private static void printResult(Object obj) {
        ValidationResult r = Validator.validate(obj);
        System.out.println("  " + r);
    }
}
