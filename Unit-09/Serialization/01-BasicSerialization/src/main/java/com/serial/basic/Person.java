package com.serial.basic;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * A simple serializable value class.
 *
 * <p><strong>Serializable is a marker interface</strong> — it has no methods.
 * Implementing it tells the JVM: "I consent to being serialized; please generate
 * the byte representation of my instance automatically."
 *
 * <p><strong>serialVersionUID</strong> — a version stamp baked into every
 * serialized byte stream.  When deserializing, the JVM checks that the UID in
 * the stream matches the UID of the class on the receiving side.  A mismatch
 * throws {@link java.io.InvalidClassException}.
 *
 * <p>If you omit serialVersionUID, the JVM computes one from the class structure.
 * Adding or renaming a field changes the computed UID and breaks deserialization
 * of old streams.  Always declare it explicitly.
 *
 * <p><strong>transient</strong> — marks fields that should NOT be included
 * in the serialized form.  Use it for:
 * <ul>
 *   <li>Sensitive data (passwords, tokens)</li>
 *   <li>Fields that can be recomputed from others</li>
 *   <li>Fields whose type is not Serializable</li>
 * </ul>
 *
 * <p><strong>static fields</strong> are not serialized — they belong to the
 * class, not to any instance.
 */
public class Person implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;    // explicit version stamp

    private final String name;
    private final int    age;

    // transient: excluded from the serialized form entirely.
    // When deserialized, it reverts to the default (null for objects, 0 for ints).
    private transient String cachedDisplayName;

    // static: class-level; never serialized regardless of the field type.
    private static int instanceCount = 0;

    public Person(String name, int age) {
        this.name = name;
        this.age  = age;
        instanceCount++;
    }

    public String getName() { return name; }
    public int    getAge()  { return age;  }

    public String getDisplayName() {
        if (cachedDisplayName == null) {
            cachedDisplayName = name + " (age " + age + ")";
        }
        return cachedDisplayName;
    }

    public static int getInstanceCount() { return instanceCount; }

    @Override
    public String toString() {
        return "Person{name=" + name + ", age=" + age
                + ", cachedDisplayName=" + cachedDisplayName + "}";
    }
}
