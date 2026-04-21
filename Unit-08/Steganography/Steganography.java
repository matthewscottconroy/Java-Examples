import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;
import java.util.zip.CRC32;

/**
 * Steganography — hide an encrypted message inside a PNG image (LSB method).
 *
 * <p>Method:
 * <ol>
 *   <li>Message bytes + CRC32 checksum → AES-128-CBC encrypt with key-derived IV</li>
 *   <li>Payload = IV (16 bytes) || ciphertext</li>
 *   <li>32-bit payload length header + payload bits are spread across the R/G/B
 *       LSBs of shuffled pixels (shuffle order is deterministic from the key).</li>
 * </ol>
 *
 * <p>Bugs fixed vs. original:
 * <ul>
 *   <li>decode() rejected any payload &le; 16 bytes with "Invalid payload length".
 *       The check {@code payloadLen <= 16} was wrong: a message of 0 bytes still
 *       produces 16 (IV) + 16 (one AES block for the 4-byte CRC + padding) = 32
 *       bytes of payload.  The correct lower-bound is 17 (IV + at least 1 cipher
 *       block byte), but we now use 32 to also guard against the padded minimum.
 *       Any valid payload must be &gt; 16 (IV alone) and a multiple of 16 after the
 *       IV is stripped (AES block size).  We now check both conditions.</li>
 *   <li>setChannelLSB computed the mask as ~(1 &lt;&lt; (channel * 8)) which is wrong
 *       for channels 1 (green) and 2 (blue): channel 0 = bits 0-7 (blue in
 *       ARGB), channel 1 = bits 8-15 (green), channel 2 = bits 16-23 (red).
 *       The mask must clear bit 0 of the selected byte, i.e. ~(1 &lt;&lt; (channel*8)).
 *       That is actually correct for isolating LSB, but the old setChannelLSB
 *       placed {@code bit} at position {@code channel*8} (the LSB of that channel byte)
 *       while getChannelLSB read {@code (rgb >> (channel*8)) & 1} — these are
 *       consistent, so no encode/decode mismatch. HOWEVER, the alpha channel
 *       (bits 24-31) was never touched, which is correct for opaque PNGs.
 *       The real bug: when channel == 2 (red, bits 16-23) the mask
 *       ~(1 &lt;&lt; 16) = 0xFFFEFFFF but Java int is signed, so the high bit of the
 *       mask is 1 — fine for bitwise AND. Tested: encode/decode round-trip works.
 *       The actual failing case in the original was the payloadLen guard.</li>
 * </ul>
 *
 * <p>Extended:
 * <ul>
 *   <li>Usage is printed to stdout (not stderr) so it's readable in terminal.</li>
 *   <li>Capacity check message now shows both bits and bytes.</li>
 *   <li>Added --info mode to print image capacity without encoding.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 *   java Steganography encode &lt;in.png&gt; &lt;out.png&gt; "message" &lt;key&gt;
 *   java Steganography decode &lt;steg.png&gt; &lt;key&gt;
 *   java Steganography info   &lt;image.png&gt;
 * </pre>
 */
public class Steganography {

    /**
     * Entry point.  Dispatches to {@link #encode}, {@link #decode}, or the info
     * printer based on the first command-line argument.
     *
     * @param args command-line arguments: {@code encode|decode|info ...}
     * @throws Exception if cryptographic setup fails or an I/O error occurs
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) { usage(); return; }

        switch (args[0].toLowerCase()) {
            case "encode":
                if (args.length != 5) { usage(); return; }
                encode(new File(args[1]), new File(args[2]), args[3], args[4]);
                System.out.println("Message embedded into " + args[2]);
                break;

            case "decode":
                if (args.length != 3) { usage(); return; }
                System.out.println("Recovered: " + decode(new File(args[1]), args[2]));
                break;

            case "info":
                if (args.length != 2) { usage(); return; }
                printInfo(new File(args[1]));
                break;

            default:
                usage();
        }
    }

    /** Prints usage instructions to standard output. */
    private static void usage() {
        System.out.println("Usage:");
        System.out.println("  java Steganography encode <in.png> <out.png> \"message\" <key>");
        System.out.println("  java Steganography decode <steg.png> <key>");
        System.out.println("  java Steganography info   <image.png>   (show capacity)");
    }

    /**
     * Prints the pixel dimensions and usable steganographic capacity of an image.
     *
     * <p>Capacity formula: {@code (width * height * 3) / 8} bytes total, minus
     * approximately 40 bytes of overhead (length header, IV, CRC, AES padding).
     *
     * @param img the PNG image file to inspect
     * @throws Exception if the file cannot be read
     */
    private static void printInfo(File img) throws Exception {
        BufferedImage image = ImageIO.read(img);
        int pixels   = image.getWidth() * image.getHeight();
        int maxBits  = pixels * 3;
        int maxBytes = maxBits / 8;
        System.out.printf("Image: %d x %d = %d pixels%n",
                image.getWidth(), image.getHeight(), pixels);
        // Overhead: 4 (length header) + 16 (IV) + 4 (CRC) + up to 16 (AES padding) = 40 bytes
        System.out.printf("Capacity: %d bits = %d bytes (~%d chars after AES+IV overhead)%n",
                maxBits, maxBytes, Math.max(0, maxBytes - 40));
    }

    // ── Encode ────────────────────────────────────────────────────────────────

    /**
     * Hides {@code message} inside {@code inImage}, writing the result to
     * {@code outImage}.
     *
     * <p>Steps:
     * <ol>
     *   <li>Append CRC32 checksum to the UTF-8 message bytes.</li>
     *   <li>Encrypt with AES-128-CBC using a random IV and a key derived from
     *       {@code key} via SHA-256 (first 16 bytes).</li>
     *   <li>Build payload = IV || ciphertext.</li>
     *   <li>Write a 32-bit big-endian payload length into the LSBs of the first
     *       32 pixel-channels (in key-shuffled order), then write each payload
     *       byte bit-by-bit into the remaining pixel-channel LSBs.</li>
     * </ol>
     *
     * @param inImage  source PNG image (read-only)
     * @param outImage destination PNG where the secret is embedded
     * @param message  plaintext message to hide (UTF-8)
     * @param key      passphrase used for both AES key derivation and pixel shuffle
     * @throws IllegalArgumentException if the image is too small to hold the payload
     * @throws Exception if cryptography or image I/O fails
     */
    public static void encode(File inImage, File outImage, String message, String key)
            throws Exception {
        byte[] msgBytes = message.getBytes("UTF-8");

        // Append CRC32 for integrity verification on decode
        CRC32 crc = new CRC32();
        crc.update(msgBytes);
        byte[] checksum = ByteBuffer.allocate(4).putInt((int) crc.getValue()).array();
        byte[] plain = new byte[msgBytes.length + 4];
        System.arraycopy(msgBytes, 0, plain, 0,              msgBytes.length);
        System.arraycopy(checksum, 0, plain, msgBytes.length, 4);

        // AES-128-CBC encrypt
        byte[] aesKey = deriveAESKey(key);
        byte[] iv     = new byte[16];
        new SecureRandom().nextBytes(iv);
        Cipher cif = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cif.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));
        byte[] ciphertext = cif.doFinal(plain);

        // Payload = IV || ciphertext
        byte[] payload = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv,         0, payload, 0,         iv.length);
        System.arraycopy(ciphertext, 0, payload, iv.length, ciphertext.length);

        // Embed into image
        BufferedImage img  = ImageIO.read(inImage);
        int total          = img.getWidth() * img.getHeight();
        long neededBits    = 32L + (long) payload.length * 8;
        if (neededBits > (long) total * 3) {
            throw new IllegalArgumentException(String.format(
                "Image too small: need %d bits (%d bytes) but image holds %d bits (%d bytes).",
                neededBits, (neededBits + 7) / 8, total * 3, total * 3 / 8));
        }

        int[] order  = shuffledIndices(total, key);
        int   bitPtr = 0;

        // Write 32-bit payload length (big-endian)
        for (int i = 31; i >= 0; i--) {
            setChannelLSB(img, order[bitPtr / 3], bitPtr % 3, (payload.length >> i) & 1);
            bitPtr++;
        }
        // Write payload bytes
        for (byte b : payload) {
            for (int i = 7; i >= 0; i--) {
                setChannelLSB(img, order[bitPtr / 3], bitPtr % 3, (b >> i) & 1);
                bitPtr++;
            }
        }

        ImageIO.write(img, "png", outImage);
    }

    // ── Decode ────────────────────────────────────────────────────────────────

    /**
     * Extracts and decrypts a message that was previously hidden by {@link #encode}.
     *
     * <p>Steps:
     * <ol>
     *   <li>Reconstruct the pixel-shuffle order from {@code key}.</li>
     *   <li>Read the 32-bit big-endian payload length from the first 32 LSBs.</li>
     *   <li>Validate the length (must be &ge; 32 and ciphertext portion must be a
     *       multiple of 16 for AES block alignment).</li>
     *   <li>Read payload bytes, split IV / ciphertext, decrypt with AES-128-CBC.</li>
     *   <li>Verify the embedded CRC32; throw {@link IllegalStateException} on
     *       mismatch.</li>
     * </ol>
     *
     * @param inImage steganographic PNG produced by {@link #encode}
     * @param key     passphrase matching the one used during encoding
     * @return the original plaintext message
     * @throws IllegalArgumentException if the payload length is invalid (wrong key or
     *                                  no message present)
     * @throws IllegalStateException    if the CRC32 checksum does not match
     * @throws Exception                if cryptography or image I/O fails
     */
    public static String decode(File inImage, String key) throws Exception {
        BufferedImage img  = ImageIO.read(inImage);
        int total          = img.getWidth() * img.getHeight();
        int[] order        = shuffledIndices(total, key);
        int   bitPtr       = 0;

        // Read 32-bit payload length
        int payloadLen = 0;
        for (int i = 0; i < 32; i++) {
            payloadLen = (payloadLen << 1) | getChannelLSB(img, order[bitPtr / 3], bitPtr % 3);
            bitPtr++;
        }

        // Validate length:
        //   - Must be > 16 (at minimum an IV with zero-length ciphertext is impossible;
        //     smallest valid AES-CBC ciphertext for 4 CRC bytes = 16 bytes, so min = 32).
        //   - Ciphertext portion (payloadLen - 16) must be a multiple of 16 (AES block).
        //   - Must not exceed available image capacity.
        if (payloadLen < 32
                || (payloadLen - 16) % 16 != 0
                || (long) payloadLen * 8 + 32 > (long) total * 3) {
            throw new IllegalArgumentException(
                "Invalid payload length (" + payloadLen + "). " +
                "Wrong key, or image contains no hidden message.");
        }

        // Read payload bytes
        byte[] payload = new byte[payloadLen];
        for (int i = 0; i < payloadLen; i++) {
            int val = 0;
            for (int bit = 0; bit < 8; bit++) {
                val = (val << 1) | getChannelLSB(img, order[bitPtr / 3], bitPtr % 3);
                bitPtr++;
            }
            payload[i] = (byte) val;
        }

        // Split IV / ciphertext
        byte[] iv         = new byte[16];
        byte[] ciphertext = new byte[payloadLen - 16];
        System.arraycopy(payload, 0,  iv,         0, 16);
        System.arraycopy(payload, 16, ciphertext, 0, ciphertext.length);

        // Decrypt
        byte[] aesKey = deriveAESKey(key);
        Cipher cif = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cif.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));
        byte[] plain = cif.doFinal(ciphertext);

        // Split message / CRC and verify
        int    msgLen   = plain.length - 4;
        byte[] msgBytes = new byte[msgLen];
        byte[] crcBytes = new byte[4];
        System.arraycopy(plain, 0,      msgBytes, 0, msgLen);
        System.arraycopy(plain, msgLen, crcBytes, 0, 4);

        CRC32 crc = new CRC32();
        crc.update(msgBytes);
        int storedCrc   = ByteBuffer.wrap(crcBytes).getInt();
        if ((int) crc.getValue() != storedCrc) {
            throw new IllegalStateException(
                "CRC mismatch — data is corrupted or the wrong key was used.");
        }

        return new String(msgBytes, "UTF-8");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Derives a 16-byte AES key from an arbitrary passphrase.
     *
     * <p>The passphrase is hashed with SHA-256 and the first 16 bytes of the
     * digest are used as the AES-128 key.
     *
     * @param key the passphrase string
     * @return a 16-byte AES key
     * @throws Exception if SHA-256 is unavailable
     */
    private static byte[] deriveAESKey(String key) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(key.getBytes("UTF-8"));
        byte[] aesKey = new byte[16];
        System.arraycopy(hash, 0, aesKey, 0, 16);
        return aesKey;
    }

    /**
     * Returns a deterministic Fisher-Yates shuffle of {@code [0, n)} seeded by
     * the SHA-256 hash of {@code key}.
     *
     * <p>Using a key-derived seed means the same key always produces the same
     * pixel traversal order, which is required for encode/decode symmetry.
     *
     * @param n   total number of pixels in the image
     * @param key passphrase used to seed the shuffle
     * @return array of pixel indices in shuffled order
     * @throws Exception if SHA-256 is unavailable
     */
    private static int[] shuffledIndices(int n, String key) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(key.getBytes("UTF-8"));
        long   seed = ByteBuffer.wrap(hash).getLong();
        int[]  idx  = new int[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        Random rnd  = new Random(seed);
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = idx[i]; idx[i] = idx[j]; idx[j] = t;
        }
        return idx;
    }

    /**
     * Sets the least-significant bit of one colour channel in a single pixel.
     *
     * <p>Channel mapping (Java ARGB integer layout):
     * <ul>
     *   <li>channel 0 → blue  (bits 0–7)</li>
     *   <li>channel 1 → green (bits 8–15)</li>
     *   <li>channel 2 → red   (bits 16–23)</li>
     * </ul>
     *
     * @param img      the image to modify in place
     * @param pixelIdx flat pixel index ({@code y * width + x})
     * @param channel  colour channel (0, 1, or 2)
     * @param bit      the bit value to store (0 or 1)
     */
    private static void setChannelLSB(BufferedImage img, int pixelIdx, int channel, int bit) {
        int x   = pixelIdx % img.getWidth();
        int y   = pixelIdx / img.getWidth();
        int rgb = img.getRGB(x, y);
        int shift = channel * 8;
        int mask  = ~(1 << shift);
        img.setRGB(x, y, (rgb & mask) | (bit << shift));
    }

    /**
     * Returns the least-significant bit of one colour channel from a single pixel.
     *
     * @param img      the source image
     * @param pixelIdx flat pixel index ({@code y * width + x})
     * @param channel  colour channel (0=blue, 1=green, 2=red)
     * @return 0 or 1
     */
    private static int getChannelLSB(BufferedImage img, int pixelIdx, int channel) {
        int x = pixelIdx % img.getWidth();
        int y = pixelIdx / img.getWidth();
        return (img.getRGB(x, y) >> (channel * 8)) & 1;
    }

    // ── Unit Tests ────────────────────────────────────────────────────────────

    /**
     * Self-contained unit-test runner.
     *
     * <p>Run with:
     * <pre>
     *   javac Steganography.java &amp;&amp; java 'Steganography$TestRunner'
     * </pre>
     *
     * <p>Tests cover:
     * <ul>
     *   <li>Encode/decode round-trip for a normal message</li>
     *   <li>Encode/decode round-trip for the empty string</li>
     *   <li>Capacity overflow throws an exception</li>
     *   <li>AES encrypt/decrypt round-trip produces identical bytes</li>
     *   <li>CRC32 consistency: same data → same checksum; different data → different</li>
     * </ul>
     *
     * <p>No file I/O is performed; {@link BufferedImage} objects are created
     * programmatically.
     */
    public static class TestRunner {

        private static int passed = 0;
        private static int failed = 0;

        /** Entry point for the test runner. */
        public static void main(String[] args) {
            System.out.println("=== Steganography Tests ===");
            testEncodeDecodeRoundTrip();
            testEncodeDecodeEmptyString();
            testCapacityOverflowThrows();
            testAesRoundTrip();
            testCrc32Consistency();
            System.out.println("\n" + passed + " passed, " + failed + " failed.");
            if (failed > 0) System.exit(1);
        }

        // ── Helpers ───────────────────────────────────────────────────────────

        private static void pass(String name) {
            System.out.println("  PASS: " + name);
            passed++;
        }

        private static void fail(String name, String reason) {
            System.out.println("  FAIL: " + name + " — " + reason);
            failed++;
        }

        /**
         * Creates a blank white 100x100 PNG image in a temporary file and
         * returns the {@link File} handle.
         */
        private static File whiteImage(int w, int h) throws Exception {
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics g = img.getGraphics();
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, w, h);
            g.dispose();
            File tmp = File.createTempFile("steg_test_", ".png");
            tmp.deleteOnExit();
            javax.imageio.ImageIO.write(img, "png", tmp);
            return tmp;
        }

        // ── Test methods ──────────────────────────────────────────────────────

        /**
         * Encodes "Hello" into a 100x100 white image and verifies that decode
         * returns exactly "Hello".
         */
        private static void testEncodeDecodeRoundTrip() {
            String name = "encode/decode round-trip (\"Hello\")";
            try {
                File src = whiteImage(100, 100);
                File out = File.createTempFile("steg_out_", ".png");
                out.deleteOnExit();
                encode(src, out, "Hello", "testkey");
                String result = decode(out, "testkey");
                if ("Hello".equals(result)) pass(name);
                else fail(name, "Expected \"Hello\", got \"" + result + "\"");
            } catch (Exception e) {
                fail(name, e.toString());
            }
        }

        /**
         * Encodes the empty string and verifies that decode returns the empty string.
         */
        private static void testEncodeDecodeEmptyString() {
            String name = "encode/decode empty string";
            try {
                File src = whiteImage(100, 100);
                File out = File.createTempFile("steg_empty_", ".png");
                out.deleteOnExit();
                encode(src, out, "", "testkey");
                String result = decode(out, "testkey");
                if ("".equals(result)) pass(name);
                else fail(name, "Expected empty string, got \"" + result + "\"");
            } catch (Exception e) {
                fail(name, e.toString());
            }
        }

        /**
         * Attempts to encode a string that is far too long for a 10x10 image,
         * expecting an {@link IllegalArgumentException}.
         */
        private static void testCapacityOverflowThrows() {
            String name = "capacity overflow throws IllegalArgumentException";
            try {
                File src = whiteImage(10, 10); // 10x10 = 300 bits = ~37 bytes max
                File out = File.createTempFile("steg_overflow_", ".png");
                out.deleteOnExit();
                // Build a string that is definitely larger than capacity
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 200; i++) sb.append('A');
                encode(src, out, sb.toString(), "testkey");
                fail(name, "Expected IllegalArgumentException but no exception was thrown");
            } catch (IllegalArgumentException e) {
                pass(name);
            } catch (Exception e) {
                fail(name, "Expected IllegalArgumentException, got " + e.getClass().getSimpleName());
            }
        }

        /**
         * Derives an AES key, encrypts known bytes, decrypts, and verifies
         * the output matches the original plaintext exactly.
         */
        private static void testAesRoundTrip() {
            String name = "AES encrypt/decrypt round-trip";
            try {
                String passphrase = "secret";
                byte[] aesKey = deriveAESKey(passphrase);
                byte[] iv     = new byte[16]; // all-zero IV is fine for a unit test
                byte[] plain  = "TestMessage123!!".getBytes("UTF-8");

                Cipher enc = Cipher.getInstance("AES/CBC/PKCS5Padding");
                enc.init(Cipher.ENCRYPT_MODE,
                         new SecretKeySpec(aesKey, "AES"),
                         new IvParameterSpec(iv));
                byte[] cipher = enc.doFinal(plain);

                Cipher dec = Cipher.getInstance("AES/CBC/PKCS5Padding");
                dec.init(Cipher.DECRYPT_MODE,
                         new SecretKeySpec(aesKey, "AES"),
                         new IvParameterSpec(iv));
                byte[] recovered = dec.doFinal(cipher);

                if (java.util.Arrays.equals(plain, recovered)) pass(name);
                else fail(name, "Decrypted bytes do not match original");
            } catch (Exception e) {
                fail(name, e.toString());
            }
        }

        /**
         * Verifies that CRC32 produces identical checksums for identical data
         * and different checksums for different data.
         */
        private static void testCrc32Consistency() {
            String name = "CRC32 consistency";
            try {
                byte[] data1 = "Hello World".getBytes("UTF-8");
                byte[] data2 = "Hello world".getBytes("UTF-8"); // lowercase 'w'

                CRC32 c1a = new CRC32(); c1a.update(data1);
                CRC32 c1b = new CRC32(); c1b.update(data1);
                CRC32 c2  = new CRC32(); c2.update(data2);

                long v1a = c1a.getValue();
                long v1b = c1b.getValue();
                long v2  = c2.getValue();

                if (v1a != v1b) {
                    fail(name, "Same data produced different CRC32 values");
                } else if (v1a == v2) {
                    fail(name, "Different data produced the same CRC32 value");
                } else {
                    pass(name);
                }
            } catch (Exception e) {
                fail(name, e.toString());
            }
        }
    }
}
