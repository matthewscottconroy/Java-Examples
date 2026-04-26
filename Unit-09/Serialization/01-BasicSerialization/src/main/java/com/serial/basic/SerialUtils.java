package com.serial.basic;

import java.io.*;

/**
 * Utility methods for serializing objects to/from byte arrays.
 *
 * <p>Using a byte array (via {@link ByteArrayOutputStream}) instead of a
 * file lets us inspect and manipulate the raw bytes in unit tests and demos
 * without touching the filesystem.  The exact same code works with a
 * {@link FileOutputStream} — just swap the destination.
 */
public class SerialUtils {

    /** Serialize {@code obj} to a byte array. */
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
        }
        return bos.toByteArray();
    }

    /** Deserialize and cast to {@code T} from a byte array. */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) ois.readObject();
        }
    }

    /**
     * Deep-copy any Serializable object.
     *
     * <p>Serialize to bytes then immediately deserialize — the result is a
     * structurally identical but distinct object graph.  Useful when you need
     * a complete independent copy and the object has no copy constructor.
     */
    public static <T extends Serializable> T deepCopy(T obj) throws IOException, ClassNotFoundException {
        return deserialize(serialize(obj));
    }
}
