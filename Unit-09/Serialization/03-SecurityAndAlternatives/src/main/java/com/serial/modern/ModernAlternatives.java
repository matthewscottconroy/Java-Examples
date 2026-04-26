package com.serial.modern;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Modern alternatives to Java's built-in serialization.
 *
 * <p><strong>Why avoid Java serialization for new designs:</strong>
 * <ol>
 *   <li>Security — deserialization of untrusted data is a persistent RCE vector</li>
 *   <li>Fragility — field renames or refactoring break binary compatibility</li>
 *   <li>Performance — reflection-based; slower than format-specific libraries</li>
 *   <li>Coupling — byte stream is JVM-internal; not readable by other languages</li>
 *   <li>No schema — no way to validate the shape of incoming data independently</li>
 * </ol>
 *
 * <p><strong>Recommended alternatives:</strong>
 * <table border="1" cellpadding="4">
 *   <tr><th>Format</th><th>Library</th><th>Best for</th></tr>
 *   <tr><td>JSON</td><td>Jackson, Gson</td><td>APIs, config, general persistence</td></tr>
 *   <tr><td>Protocol Buffers</td><td>protobuf-java</td><td>high-performance, cross-language</td></tr>
 *   <tr><td>Avro</td><td>Apache Avro</td><td>Hadoop/Kafka, schema evolution</td></tr>
 *   <tr><td>MessagePack</td><td>msgpack-java</td><td>compact binary JSON-like</td></tr>
 * </table>
 *
 * <p>This example shows the pattern without external dependencies:
 * manual JSON-like encoding of a simple record.  In production, use Jackson.
 */
public class ModernAlternatives {

    // -----------------------------------------------------------------------
    // 1. Records as data carriers — no Serializable baggage
    // -----------------------------------------------------------------------
    record Product(String name, double price, int quantity) {

        // A record can define its own text serialization contract.
        String toJson() {
            return "{\"name\":\"%s\",\"price\":%.2f,\"quantity\":%d}"
                    .formatted(name, price, quantity);
        }

        static Product fromJson(String json) {
            // Minimal hand-rolled parser for the exact format above.
            String[] parts = json.replaceAll("[{}\"]", "").split(",");
            String  name     = parts[0].split(":")[1];
            double  price    = Double.parseDouble(parts[1].split(":")[1]);
            int     quantity = Integer.parseInt(parts[2].split(":")[1]);
            return new Product(name, price, quantity);
        }
    }

    public static void showRecordJson() {
        System.out.println("-- Records + manual JSON --");
        Product p = new Product("Widget", 9.99, 42);
        System.out.println("  original:   " + p);

        String json = p.toJson();
        System.out.println("  toJson():   " + json);

        Product restored = Product.fromJson(json);
        System.out.println("  fromJson(): " + restored);
        System.out.println("  equals:     " + p.equals(restored));

        System.out.println("  (In production: jackson.writeValueAsString(p) / readValue(json, Product.class))");
    }

    // -----------------------------------------------------------------------
    // 2. DataOutputStream as a structured binary format — lightweight, fast,
    //    no reflection.  Define your own schema; write/read it explicitly.
    // -----------------------------------------------------------------------
    record Sensor(String id, long timestamp, double reading) {}

    public static void showManualBinary() throws IOException {
        System.out.println("\n-- Manual binary format with DataOutputStream --");

        List<Sensor> sensors = List.of(
                new Sensor("temp-01", System.currentTimeMillis(), 23.5),
                new Sensor("temp-02", System.currentTimeMillis(), 24.1),
                new Sensor("temp-03", System.currentTimeMillis(), 22.8));

        // Write: count, then each record.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bos)) {
            out.writeInt(sensors.size());
            for (Sensor s : sensors) {
                out.writeUTF(s.id());
                out.writeLong(s.timestamp());
                out.writeDouble(s.reading());
            }
        }
        System.out.println("  " + sensors.size() + " sensors → " + bos.size() + " bytes");

        // Read back.
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                System.out.println("  " + new Sensor(in.readUTF(), in.readLong(), in.readDouble()));
            }
        }
        System.out.println("  (vs Java serialization: no class metadata, no reflection, full control)");
    }

    // -----------------------------------------------------------------------
    // 3. Simple toString-based serialization — good for value objects
    // -----------------------------------------------------------------------
    public static void showToStringSerialization() {
        System.out.println("\n-- toString + parse (simplest possible alternative) --");

        record Point(int x, int y) {
            @Override public String toString() { return x + "," + y; }
            static Point parse(String s) {
                String[] p = s.split(",");
                return new Point(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
            }
        }

        Point original = new Point(10, 20);
        String serialized = original.toString();
        Point restored = Point.parse(serialized);

        System.out.println("  original:   " + original);
        System.out.println("  serialized: " + serialized);
        System.out.println("  restored:   " + restored);
        System.out.println("  equals:     " + original.equals(restored));
    }
}
