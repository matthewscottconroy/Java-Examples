package com.reflect.annot;

import java.lang.reflect.Field;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("=== Annotation anatomy ===");
        System.out.println("Annotations on User.username:");
        Field username = User.class.getDeclaredField("username");
        for (var ann : username.getDeclaredAnnotations()) {
            System.out.println("  " + ann);
        }

        System.out.println("\n@Length attributes:");
        Length len = username.getAnnotation(Length.class);
        System.out.println("  min=" + len.min() + "  max=" + len.max() + "  message=" + len.message());

        System.out.println("\n=== Validation — valid user ===");
        User valid = new User("alice", "securePass1!", "alice@example.com", 30);
        List<Validator.Violation> v1 = Validator.validate(valid);
        System.out.println(v1.isEmpty() ? "  No violations." : "  Violations: " + v1);

        System.out.println("\n=== Validation — invalid user ===");
        User invalid = new User("x", "short", null, 0);   // username too short, password too short, null email (ok), null where @NotNull
        List<Validator.Violation> v2 = Validator.validate(invalid);
        v2.forEach(viol -> System.out.println("  " + viol));

        System.out.println("\n=== DDL generation from @Column ===");
        System.out.println(Validator.generateDdl(User.class));

        System.out.println("\n=== Annotation retention policies ===");
        // @Retention(SOURCE)  → stripped by javac; invisible everywhere after compilation
        // @Retention(CLASS)   → in .class file but NOT loaded into JVM (default)
        // @Retention(RUNTIME) → loaded into JVM; readable via reflection
        System.out.println("  @NotNull retention:  " + NotNull.class.getAnnotation(java.lang.annotation.Retention.class).value());
        System.out.println("  @Column  retention:  " + Column.class.getAnnotation(java.lang.annotation.Retention.class).value());
        System.out.println("  @Override retention: " + Override.class.getAnnotation(java.lang.annotation.Retention.class).value()
                + " (SOURCE — gone after compilation, purely a compiler hint)");
    }
}
