package com.reflect.dynamic;

/**
 * A class with members at various access levels — used as the reflection target.
 */
public class Target {

    public  String publicField    = "public-value";
    private String privateField   = "private-secret";
    private int    counter        = 0;

    public Target() {}
    public Target(String publicField) { this.publicField = publicField; }

    public  String publicMethod(String input) { return "public: " + input; }
    private String privateMethod()            { return "private: " + privateField; }

    private void increment(int delta) { counter += delta; }
    public  int  getCounter()         { return counter; }

    // Overloaded methods — differentiated by parameter type at lookup time.
    public String process(String s)  { return "process(String): " + s; }
    public String process(int n)     { return "process(int): "    + n; }

    @Override public String toString() {
        return "Target{public=" + publicField + ", counter=" + counter + "}";
    }
}
