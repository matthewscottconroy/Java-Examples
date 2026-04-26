package com.generics.box;

/**
 * The pre-generics approach: store anything as {@code Object}.
 *
 * Problems:
 *   1. The caller must cast the return value — the compiler can't verify the cast.
 *   2. A wrong cast compiles fine but throws ClassCastException at runtime.
 *   3. Nothing stops you from putting the wrong type in.
 */
public class ObjectBox {

    private Object value;

    public ObjectBox(Object value) {
        this.value = value;
    }

    // Returns Object — the caller must know what type was put in.
    public Object get() {
        return value;
    }

    public void set(Object value) {
        this.value = value;
    }
}
