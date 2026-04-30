package com.meta.reflect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionTest {

    static class Sample {
        private int secret = 42;
        public String greet(String name) { return "Hello, " + name + "!"; }
        public int add(int a, int b) { return a + b; }
        private void hidden() {}
    }

    @Test @DisplayName("describe() includes the class name")
    void describe_containsClassName() {
        String desc = Inspector.describe(Sample.class);
        assertTrue(desc.contains("Sample"), "describe() should mention the class name");
    }

    @Test @DisplayName("describe() lists declared fields")
    void describe_containsField() {
        String desc = Inspector.describe(Sample.class);
        assertTrue(desc.contains("secret"), "describe() should list the 'secret' field");
    }

    @Test @DisplayName("describe() lists public methods")
    void describe_containsPublicMethod() {
        String desc = Inspector.describe(Sample.class);
        assertTrue(desc.contains("greet"), "describe() should list the 'greet' method");
    }

    @Test @DisplayName("invoke() calls a method by name and returns result")
    void invoke_returnsResult() throws Exception {
        Sample s = new Sample();
        Object result = Inspector.invoke(s, "greet", "World");
        assertEquals("Hello, World!", result);
    }

    @Test @DisplayName("invoke() throws for unknown method")
    void invoke_unknownMethod() {
        Sample s = new Sample();
        assertThrows(NoSuchMethodException.class,
            () -> Inspector.invoke(s, "nonExistent", "arg"));
    }

    @Test @DisplayName("readField() reads a private field value")
    void readField_privateField() throws Exception {
        Sample s = new Sample();
        Object value = Inspector.readField(s, "secret");
        assertEquals(42, value);
    }

    @Test @DisplayName("readField() throws for unknown field")
    void readField_unknownField() {
        Sample s = new Sample();
        assertThrows(NoSuchFieldException.class,
            () -> Inspector.readField(s, "doesNotExist"));
    }

    @Test @DisplayName("instantiate() creates instance by class name")
    void instantiate_byName() throws Exception {
        Object obj = Inspector.instantiate("java.util.ArrayList");
        assertInstanceOf(ArrayList.class, obj);
    }

    @Test @DisplayName("instantiate() throws for unknown class")
    void instantiate_unknownClass() {
        assertThrows(ClassNotFoundException.class,
            () -> Inspector.instantiate("com.nonexistent.FakeClass"));
    }

    @Test @DisplayName("findMethodsWithPrefix() finds all 'get' methods on ArrayList")
    void findMethodsWithPrefix_getters() {
        List<Method> methods = Inspector.findMethodsWithPrefix(ArrayList.class, "get");
        assertFalse(methods.isEmpty(), "ArrayList should have 'get' methods");
        methods.forEach(m -> assertTrue(m.getName().startsWith("get")));
    }

    @Test @DisplayName("PluginRegistry: register and run CSV plugin")
    void pluginRegistry_csv() throws Exception {
        PluginRegistry registry = new PluginRegistry();
        registry.register("com.meta.reflect.CsvPlugin");
        String output = registry.run("csv", "a=1;b=2");
        assertTrue(output.contains("a") && output.contains("1"));
    }

    @Test @DisplayName("PluginRegistry: unknown plugin throws")
    void pluginRegistry_unknownPlugin() {
        PluginRegistry registry = new PluginRegistry();
        assertThrows(NoSuchElementException.class,
            () -> registry.run("missing", "data"));
    }

    @Test @DisplayName("PluginRegistry: non-plugin class throws IllegalArgumentException")
    void pluginRegistry_nonPlugin() {
        PluginRegistry registry = new PluginRegistry();
        assertThrows(IllegalArgumentException.class,
            () -> registry.register("java.lang.String"));
    }

    @Test @DisplayName("PluginRegistry: names() lists all registered plugins")
    void pluginRegistry_names() throws Exception {
        PluginRegistry registry = new PluginRegistry();
        registry.register("com.meta.reflect.CsvPlugin");
        registry.register("com.meta.reflect.JsonPlugin");
        assertEquals(Set.of("csv", "json"), registry.names());
    }

    @Test @DisplayName("Class.forName() loads class by string name")
    void classForName() throws Exception {
        Class<?> cls = Class.forName("java.util.HashMap");
        assertEquals("HashMap", cls.getSimpleName());
    }

    @Test @DisplayName("getClass() returns runtime type, not declared type")
    void getClass_runtimeType() {
        List<String> list = new ArrayList<>();
        assertNotEquals(List.class, list.getClass());
        assertEquals(ArrayList.class, list.getClass());
    }
}
