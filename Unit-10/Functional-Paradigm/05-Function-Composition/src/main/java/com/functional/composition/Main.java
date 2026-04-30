package com.functional.composition;

import java.util.function.Function;

/**
 * Demonstrates function composition with a user registration validation pipeline.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Validation Pipeline (Function Composition) ===\n");

        // Build validators by composing small functions
        Function<String, ValidationResult> usernameValidator = Validator.chain(
                Validator.notBlank("username"),
                Validator.minLength("username", 3),
                Validator.maxLength("username", 20),
                Validator.matches("username", "[a-zA-Z0-9_]+",
                                  "may only contain letters, digits, and underscores")
        );

        Function<String, ValidationResult> passwordValidator = Validator.chain(
                Validator.notBlank("password"),
                Validator.minLength("password", 8),
                Validator.matches("password", ".*[A-Z].*", "must contain at least one uppercase letter"),
                Validator.matches("password", ".*[0-9].*", "must contain at least one digit")
        );

        Function<String, ValidationResult> emailValidator = Validator.chain(
                Validator.notBlank("email"),
                Validator.matches("email", "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$",
                                  "must be a valid email address")
        );

        String[][] cases = {
                { "",           "pass",         "alice@example.com" },
                { "al",         "Password1",    "alice@example.com" },
                { "alice_chen", "short",        "alice@example.com" },
                { "alice_chen", "password1",    "alice@example.com" },
                { "alice_chen", "Password1",    "not-an-email"      },
                { "alice_chen", "Password1",    "alice@example.com" }
        };

        System.out.printf("%-15s  %-15s  %-25s  %s%n", "Username", "Password", "Email", "Result");
        System.out.println("-".repeat(90));

        for (String[] row : cases) {
            String u = row[0], p = row[1], e = row[2];
            ValidationResult ur = usernameValidator.apply(u);
            ValidationResult pr = ur.isValid() ? passwordValidator.apply(p) : ur;
            ValidationResult er = pr.isValid() ? emailValidator.apply(e)    : pr;
            String status = er.isValid() ? "OK" : er.errorMessage();
            System.out.printf("%-15s  %-15s  %-25s  %s%n",
                    u.isEmpty() ? "(blank)" : u, p, e, status);
        }
    }
}
