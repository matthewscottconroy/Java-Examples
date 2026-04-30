package com.meta.reflect;

import java.lang.reflect.*;
import java.util.*;

// Concrete plugin implementations registered at runtime
class CsvPlugin implements ReportPlugin {
    public String name() { return "csv"; }
    public String generate(String data) {
        return "timestamp,value\n" + data.replace(";", "\n").replace("=", ",");
    }
}

class JsonPlugin implements ReportPlugin {
    public String name() { return "json"; }
    public String generate(String data) {
        StringBuilder sb = new StringBuilder("{\n");
        for (String pair : data.split(";")) {
            String[] kv = pair.split("=");
            if (kv.length == 2)
                sb.append("  \"").append(kv[0].trim()).append("\": \"")
                  .append(kv[1].trim()).append("\",\n");
        }
        if (sb.charAt(sb.length() - 2) == ',') sb.deleteCharAt(sb.length() - 2);
        return sb.append("}").toString();
    }
}

class HtmlPlugin implements ReportPlugin {
    public String name() { return "html"; }
    public String generate(String data) {
        StringBuilder sb = new StringBuilder("<table>\n");
        for (String pair : data.split(";")) {
            String[] kv = pair.split("=");
            if (kv.length == 2)
                sb.append("  <tr><td>").append(kv[0].trim())
                  .append("</td><td>").append(kv[1].trim()).append("</td></tr>\n");
        }
        return sb.append("</table>").toString();
    }
}

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Reflection — Plugin-Based Report Generator ===\n");

        // 1. Class inspection
        System.out.println("--- Class inspection ---");
        System.out.println(Inspector.describe(ArrayList.class));

        // 2. Dynamic method invocation
        System.out.println("--- Dynamic method invocation ---");
        List<String> list = new ArrayList<>(List.of("banana", "apple", "cherry"));
        Inspector.invoke(list, "sort", Comparator.naturalOrder());
        System.out.println("After reflective sort: " + list);

        // 3. Private field access
        System.out.println("\n--- Private field access ---");
        CsvPlugin plugin = new CsvPlugin();
        // Access would work on any object — here we just show the name field via reflection
        Class<?> cls = plugin.getClass();
        System.out.println("Class simple name: " + cls.getSimpleName());
        System.out.println("Superinterfaces: " + Arrays.toString(cls.getInterfaces()));

        // 4. Plugin registry — loaded by class name at runtime
        System.out.println("\n--- Runtime plugin registry ---");
        PluginRegistry registry = new PluginRegistry();
        for (String className : List.of(
                "com.meta.reflect.CsvPlugin",
                "com.meta.reflect.JsonPlugin",
                "com.meta.reflect.HtmlPlugin")) {
            registry.register(className);
            System.out.println("Loaded: " + className);
        }

        String data = "name=Alice;score=95;grade=A";
        System.out.println("\nInput data: " + data);
        System.out.println("\nRegistered plugins: " + registry.names());

        for (String name : registry.names()) {
            System.out.println("\n[" + name + " output]");
            System.out.println(registry.run(name, data));
        }

        // 5. Method discovery by prefix
        System.out.println("--- Methods with prefix 'get' on ArrayList ---");
        Inspector.findMethodsWithPrefix(ArrayList.class, "get")
            .forEach(m -> System.out.println("  " + m.getName()
                + "(" + Arrays.stream(m.getParameterTypes())
                    .map(Class::getSimpleName)
                    .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b) + ")"));
    }
}
