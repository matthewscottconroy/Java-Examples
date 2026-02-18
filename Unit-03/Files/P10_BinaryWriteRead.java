import java.io.*;
import java.nio.file.*;

public class P10_BinaryWriteRead {
    public static void main(String[] args) throws IOException {
        Path p = Paths.get(args.length > 0 ? args[0] : "records.bin");

        // Write two records
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(p)))) {

            writeRecord(out, 101, 3.14, "alpha");
            writeRecord(out, 102, 2.71, "beta");
        }

        // Read them back
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(p)))) {

            while (true) {
                try {
                    int id = in.readInt();
                    double val = in.readDouble();
                    String name = in.readUTF();
                    System.out.printf("Record: id=%d val=%f name=%s%n", id, val, name);
                } catch (EOFException eof) {
                    break;
                }
            }
        }
    }

    static void writeRecord(DataOutputStream out, int id, double val, String name) throws IOException {
        out.writeInt(id);
        out.writeDouble(val);
        out.writeUTF(name);
    }
}
