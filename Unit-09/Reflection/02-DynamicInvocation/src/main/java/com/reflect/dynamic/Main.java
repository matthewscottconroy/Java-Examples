package com.reflect.dynamic;

import java.lang.reflect.Method;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("=== Instance creation ===");
        InvocationDemo.showInstanceCreation();

        System.out.println("\n=== Method invocation ===");
        InvocationDemo.showMethodInvocation();

        System.out.println("\n=== Field access ===");
        InvocationDemo.showFieldAccess();

        // ----------------------------------------------------------------
        // Mini dependency injector: set any field named "value" on an object
        // ----------------------------------------------------------------
        System.out.println("\n=== Mini injector: set fields by name --");
        record Config(String host, int port) {}
        // Records are immutable so we'll use a mutable stand-in.
        class Connection {
            String host = "default";
            int    port = 0;
            @Override public String toString() { return host + ":" + port; }
        }

        Connection conn = new Connection();
        System.out.println("  before injection: " + conn);

        java.util.Map<String, Object> values = java.util.Map.of("host", "db.prod.local", "port", 5432);
        for (var entry : values.entrySet()) {
            try {
                var field = Connection.class.getDeclaredField(entry.getKey());
                field.setAccessible(true);
                field.set(conn, entry.getValue());
            } catch (NoSuchFieldException ignored) {}
        }
        System.out.println("  after  injection: " + conn);

        // ----------------------------------------------------------------
        // Dispatching by method name — like a plugin system
        // ----------------------------------------------------------------
        System.out.println("\n=== Plugin dispatch by string name ===");
        Target t = new Target();
        for (String name : new String[]{"publicMethod", "process"}) {
            Arrays.stream(Target.class.getMethods())
                  .filter(m -> m.getName().equals(name) && m.getParameterCount() == 1
                          && m.getParameterTypes()[0] == String.class)
                  .findFirst()
                  .ifPresent(m -> {
                      try { System.out.println("  " + name + ": " + m.invoke(t, "arg")); }
                      catch (Exception e) { System.out.println("  error: " + e); }
                  });
        }
    }
}
