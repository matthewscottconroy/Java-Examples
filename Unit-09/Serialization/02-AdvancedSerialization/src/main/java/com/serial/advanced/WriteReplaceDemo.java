package com.serial.advanced;

import java.io.*;

/**
 * {@code writeReplace} and {@code readResolve} — object substitution hooks.
 *
 * <p><strong>{@code writeReplace()}</strong>: called <em>before</em> writing.
 * Return a different object; that replacement is what gets serialized.
 * Use it to serialize a proxy or a token instead of the real object.
 *
 * <p><strong>{@code readResolve()}</strong>: called <em>after</em> reading.
 * The deserialized bytes produce a temporary object; {@code readResolve}
 * can throw it away and return something else — typically the canonical instance.
 * Use it to enforce singleton or enum-like identity across deserialization.
 *
 * <p>These hooks are inherited by subclasses (unlike writeObject/readObject),
 * which makes them particularly useful for frameworks.
 */
public class WriteReplaceDemo {

    // -----------------------------------------------------------------------
    // Singleton: readResolve prevents a second instance from sneaking in.
    // -----------------------------------------------------------------------
    static class AppConfig implements Serializable {
        @Serial private static final long serialVersionUID = 1L;

        private static final AppConfig INSTANCE = new AppConfig();
        private final String env = "production";

        private AppConfig() {}

        public static AppConfig getInstance() { return INSTANCE; }

        // After deserialization, replace the fresh object with the canonical one.
        @Serial
        private Object readResolve() throws ObjectStreamException {
            System.out.println("  readResolve: returning canonical INSTANCE");
            return INSTANCE;
        }

        @Override public String toString() {
            return "AppConfig{env=" + env + ", id=" + System.identityHashCode(this) + "}";
        }
    }

    public static void showSingletonResolve() throws Exception {
        System.out.println("-- readResolve: Singleton identity across deserialization --");
        AppConfig original = AppConfig.getInstance();
        System.out.println("  original: " + original);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(original);
        }

        AppConfig restored;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            restored = (AppConfig) ois.readObject();
        }
        System.out.println("  restored: " + restored);
        System.out.println("  same instance? " + (original == restored));
    }

    // -----------------------------------------------------------------------
    // writeReplace: serialize a lightweight proxy instead of the real object.
    // -----------------------------------------------------------------------
    static class HeavyResource implements Serializable {
        @Serial private static final long serialVersionUID = 1L;

        private final String resourceId;
        @SuppressWarnings("unused")
        private transient byte[] hugeData = new byte[1024 * 1024]; // 1 MB — don't serialize this!

        HeavyResource(String resourceId) { this.resourceId = resourceId; }

        // Instead of serializing this large object, serialize a small proxy.
        @Serial
        private Object writeReplace() throws ObjectStreamException {
            System.out.println("  writeReplace: substituting with Proxy{" + resourceId + "}");
            return new HeavyResourceProxy(resourceId);
        }

        @Override public String toString() { return "HeavyResource{" + resourceId + "}"; }
    }

    static class HeavyResourceProxy implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
        private final String resourceId;
        HeavyResourceProxy(String id) { this.resourceId = id; }

        // When the proxy is deserialized, reconstitute the full object.
        @Serial
        private Object readResolve() {
            System.out.println("  readResolve (proxy): reconstructing HeavyResource");
            return new HeavyResource(resourceId);
        }
    }

    public static void showWriteReplace() throws Exception {
        System.out.println("\n-- writeReplace: substitute a lightweight proxy on write --");
        HeavyResource resource = new HeavyResource("res-42");
        System.out.println("  original: " + resource);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(resource);
        }
        System.out.println("  serialized: " + bos.size() + " bytes (proxy, not 1 MB)");

        HeavyResource restored;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            restored = (HeavyResource) ois.readObject();
        }
        System.out.println("  restored: " + restored);
    }
}
