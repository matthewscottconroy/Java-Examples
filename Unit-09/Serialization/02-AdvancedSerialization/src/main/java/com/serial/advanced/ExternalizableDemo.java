package com.serial.advanced;

import java.io.*;

/**
 * {@link Externalizable} — full manual control over serialization.
 *
 * <p>Compared to {@code Serializable}:
 *
 * <table border="1" cellpadding="4">
 *   <tr><th></th><th>Serializable</th><th>Externalizable</th></tr>
 *   <tr><td>JVM default handling</td><td>yes</td><td>no — you write everything</td></tr>
 *   <tr><td>Constructor called on read</td><td>no</td><td>yes — public no-arg required</td></tr>
 *   <tr><td>Performance</td><td>moderate</td><td>faster (no reflection)</td></tr>
 *   <tr><td>Fragility</td><td>lower</td><td>higher (manual field tracking)</td></tr>
 * </table>
 *
 * <p><strong>Gotcha:</strong> the JVM calls the public no-arg constructor before
 * calling {@code readExternal}.  If there is no public no-arg constructor,
 * deserialization throws.
 *
 * <p>Use Externalizable when serialization is a hot path and you need
 * maximum performance, or when you need complete control over what gets written.
 */
public class ExternalizableDemo {

    static class Coordinate implements Externalizable {

        private double lat;
        private double lon;
        private String label;

        // Required by Externalizable — called by the JVM before readExternal.
        public Coordinate() {}

        public Coordinate(double lat, double lon, String label) {
            this.lat   = lat;
            this.lon   = lon;
            this.label = label;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            // Write exactly what you want — nothing is automatic.
            out.writeDouble(lat);
            out.writeDouble(lon);
            out.writeUTF(label);
            System.out.println("  writeExternal: " + this);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException {
            // Read in the SAME ORDER as writeExternal.
            lat   = in.readDouble();
            lon   = in.readDouble();
            label = in.readUTF();
            System.out.println("  readExternal:  " + this);
        }

        @Override public String toString() {
            return "Coordinate{lat=" + lat + ", lon=" + lon + ", label=" + label + "}";
        }
    }

    public static void demonstrate() throws Exception {
        System.out.println("-- Externalizable --");
        Coordinate coord = new Coordinate(48.8566, 2.3522, "Paris");
        System.out.println("  original: " + coord);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(coord);
        }
        System.out.println("  serialized: " + bos.size() + " bytes");

        Coordinate restored;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            restored = (Coordinate) ois.readObject();
        }
        System.out.println("  restored: " + restored);
    }
}
