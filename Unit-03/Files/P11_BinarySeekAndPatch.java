import java.io.*;
import java.nio.file.*;

public class P11_BinarySeekAndPatch {
    static final int RECORD_SIZE = 12; // int (4) + long (8)

    public static void main(String[] args) throws IOException {
        Path p = Paths.get(args.length > 0 ? args[0] : "accounts.bin");

        // Create a file with 3 fixed-size records
        try (RandomAccessFile raf = new RandomAccessFile(p.toFile(), "rw")) {
            raf.setLength(0); // start fresh
            writeRecord(raf, 1, 1000L);
            writeRecord(raf, 2, 2000L);
            writeRecord(raf, 3, 3000L);
        }

        // Patch record index 1 (second record) balance to 9999
        patchBalance(p, 1, 9999L);

        // Print all records
        try (RandomAccessFile raf = new RandomAccessFile(p.toFile(), "r")) {
            long count = raf.length() / RECORD_SIZE;
            for (int i = 0; i < count; i++) {
                raf.seek((long) i * RECORD_SIZE);
                int id = raf.readInt();
                long bal = raf.readLong();
                System.out.printf("i=%d id=%d balance=%d%n", i, id, bal);
            }
        }
    }

    static void writeRecord(RandomAccessFile raf, int id, long balance) throws IOException {
        raf.writeInt(id);
        raf.writeLong(balance);
    }

    static void patchBalance(Path p, int recordIndex, long newBalance) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(p.toFile(), "rw")) {
            long offset = (long) recordIndex * RECORD_SIZE + 4; // skip int id
            raf.seek(offset);
            raf.writeLong(newBalance);
            System.out.println("Patched record " + recordIndex + " balance at byte offset " + offset);
        }
    }
}
