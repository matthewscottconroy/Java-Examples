package com.serial.advanced;

import java.io.*;

/**
 * Custom serialization via {@code writeObject} / {@code readObject}.
 *
 * <p>Declare these private methods and the JVM will call them instead of its
 * default reflection-based mechanism.  You still call
 * {@code defaultWriteObject()} / {@code defaultReadObject()} to let the JVM
 * handle the non-transient fields, then add any extra logic before or after.
 *
 * <p>Common uses:
 * <ul>
 *   <li>Encrypt or compress data before writing</li>
 *   <li>Validate invariants during reading</li>
 *   <li>Serialize a field whose type is not Serializable (by decomposing it)</li>
 *   <li>Handle version migration gracefully</li>
 * </ul>
 */
public class CustomSerial {

    /**
     * A wallet with a balance that is XOR-obfuscated on the wire.
     *
     * <p>This is a demonstration of the custom hook, not real security —
     * use proper encryption for sensitive data in production.
     */
    static class ObfuscatedWallet implements Serializable {
        @Serial private static final long serialVersionUID = 1L;

        private final String owner;
        private transient int balance;      // transient: we handle it ourselves

        private static final int XOR_KEY = 0xDEADBEEF;

        ObfuscatedWallet(String owner, int balance) {
            this.owner   = owner;
            this.balance = balance;
        }

        @Serial
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();       // writes: owner (balance is transient, skipped)
            out.writeInt(balance ^ XOR_KEY);// write obfuscated balance manually
            System.out.println("  writeObject: wrote balance as " + (balance ^ XOR_KEY));
        }

        @Serial
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();         // restores: owner
            balance = in.readInt() ^ XOR_KEY; // un-obfuscate
            if (balance < 0) throw new InvalidObjectException("Negative balance: " + balance);
            System.out.println("  readObject:  restored balance as " + balance);
        }

        @Override public String toString() {
            return "Wallet{owner=" + owner + ", balance=" + balance + "}";
        }
    }

    public static void demonstrate() throws Exception {
        System.out.println("-- Custom writeObject/readObject --");
        ObfuscatedWallet wallet = new ObfuscatedWallet("Alice", 1000);
        System.out.println("  original: " + wallet);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(wallet);
        }

        ObfuscatedWallet restored;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            restored = (ObfuscatedWallet) ois.readObject();
        }
        System.out.println("  restored: " + restored);
    }
}
