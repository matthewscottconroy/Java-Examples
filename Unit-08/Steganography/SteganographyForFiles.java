import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;
import java.util.zip.CRC32;

public class SteganographyForFiles {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) usage();

        String mode = args[0].toLowerCase();
        switch (mode) {
            case "encode":
                if (args.length != 5) usage();
                File inImage  = new File(args[1]);
                File outImage = new File(args[2]);
                Path inFile   = Paths.get(args[3]);
                String key    = args[4];
                encode(inImage, outImage, inFile, key);
                System.out.println("✔ Embedded " + inFile.getFileName() + " into " + outImage);
                break;

            case "decode":
                if (args.length != 4) usage();
                File srcImage = new File(args[1]);
                Path outFile  = Paths.get(args[2]);
                String pwd    = args[3];
                decode(srcImage, outFile, pwd);
                System.out.println("🔑 Extracted file to: " + outFile);
                break;

            default:
                usage();
        }
    }

    private static void usage() {
        System.err.println("Usage:");
        System.err.println("  To encode:");
        System.err.println("    java SteganographyForFiles encode <in.png> <out.png> <file_to_embed> <key>");
        System.err.println("  To decode:");
        System.err.println("    java SteganographyForFiles decode <in.png> <output_file> <key>");
        System.exit(1);
    }

    public static void encode(File inImage,
                              File outImage,
                              Path inFile,
                              String key) throws Exception {
        byte[] fileBytes = Files.readAllBytes(inFile);

        CRC32 crc = new CRC32();
        crc.update(fileBytes);
        byte[] checksum = ByteBuffer.allocate(4).putInt((int) crc.getValue()).array();

        byte[] plain = new byte[fileBytes.length + 4];
        System.arraycopy(fileBytes, 0, plain, 0, fileBytes.length);
        System.arraycopy(checksum,  0, plain, fileBytes.length, 4);

        // Encrypt
        byte[] aesKey = deriveAESKey(key);
        byte[] iv     = new byte[16];
        new SecureRandom().nextBytes(iv);
        Cipher cif = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cif.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));
        byte[] cipher = cif.doFinal(plain);

        // Payload = IV || ciphertext
        byte[] payload = new byte[iv.length + cipher.length];
        System.arraycopy(iv,     0, payload, 0,     iv.length);
        System.arraycopy(cipher, 0, payload, iv.length, cipher.length);

        // Embed
        BufferedImage img = ImageIO.read(inImage);
        int w = img.getWidth(), h = img.getHeight(), total = w * h;
        long neededBits = 32L + (long) payload.length * 8;
        if (neededBits > (long) total * 3) {
            throw new IllegalArgumentException("Image too small: need " + neededBits + " bits (" + ((neededBits + 2) / 3) + " pixels)");
        }

        int[] order = shuffledIndices(total, key);
        int bitPtr = 0;

        // Length header
        for (int i = 31; i >= 0; i--) {
            int bit = (payload.length >> i) & 1;
            int pixelIdx = order[bitPtr / 3];
            int channel  = bitPtr % 3;
            setChannelLSB(img, pixelIdx, channel, bit);
            bitPtr++;
        }
        // Payload bits
        for (byte b : payload) {
            for (int i = 7; i >= 0; i--) {
                int bit = (b >> i) & 1;
                int pixelIdx = order[bitPtr / 3];
                int channel  = bitPtr % 3;
                setChannelLSB(img, pixelIdx, channel, bit);
                bitPtr++;
            }
        }

        ImageIO.write(img, "png", outImage);
    }

    public static void decode(File inImage,
                              Path outFile,
                              String key) throws Exception {
        BufferedImage img = ImageIO.read(inImage);
        int w = img.getWidth(), h = img.getHeight(), total = w * h;
        int[] order = shuffledIndices(total, key);

        int bitPtr = 0;
        int payloadLen = 0;
        for (int i = 0; i < 32; i++) {
            int pixelIdx = order[bitPtr / 3];
            int channel  = bitPtr % 3;
            payloadLen = (payloadLen << 1) | getChannelLSB(img, pixelIdx, channel);
            bitPtr++;
        }
        // Minimum valid payload: 16 (IV) + 16 (one AES block for 4-byte CRC + padding) = 32.
        // Ciphertext length (payloadLen - 16) must be a multiple of the AES block size (16).
        if (payloadLen < 32
                || (payloadLen - 16) % 16 != 0
                || (long) payloadLen * 8 + 32 > (long) total * 3) {
            throw new IllegalArgumentException(
                "Invalid payload length (" + payloadLen + "). " +
                "Wrong key, or image contains no hidden message.");
        }

        byte[] payload = new byte[payloadLen];
        for (int i = 0; i < payloadLen; i++) {
            int val = 0;
            for (int bit = 0; bit < 8; bit++) {
                int pixelIdx = order[bitPtr / 3];
                int channel  = bitPtr % 3;
                val = (val << 1) | getChannelLSB(img, pixelIdx, channel);
                bitPtr++;
            }
            payload[i] = (byte) val;
        }

        byte[] iv     = new byte[16];
        byte[] cipher = new byte[payloadLen - 16];
        System.arraycopy(payload, 0,       iv,     0,      16);
        System.arraycopy(payload, 16, cipher, 0, payloadLen - 16);

        Cipher cif = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cif.init(Cipher.DECRYPT_MODE, new SecretKeySpec(deriveAESKey(key), "AES"), new IvParameterSpec(iv));
        byte[] plain = cif.doFinal(cipher);

        int fileLen = plain.length - 4;
        byte[] fileBytes = new byte[fileLen];
        byte[] crcBytes  = new byte[4];
        System.arraycopy(plain, 0, fileBytes, 0, fileLen);
        System.arraycopy(plain, fileLen, crcBytes, 0, 4);

        int storedCrc = ByteBuffer.wrap(crcBytes).getInt();
        CRC32 crc = new CRC32(); crc.update(fileBytes);
        if ((int) crc.getValue() != storedCrc) {
            throw new IllegalStateException("CRC mismatch – data corrupted or wrong key.");
        }

        Files.write(outFile, fileBytes);
    }

    private static byte[] deriveAESKey(String key) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(key.getBytes("UTF-8"));
        byte[] aesKey = new byte[16];
        System.arraycopy(hash, 0, aesKey, 0, 16);
        return aesKey;
    }

    private static int[] shuffledIndices(int n, String key) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(key.getBytes("UTF-8"));
        long seed = ByteBuffer.wrap(hash).getLong();

        int[] idx = new int[n]; for (int i = 0; i < n; i++) idx[i] = i;
        Random rnd = new Random(seed);
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = idx[i]; idx[i] = idx[j]; idx[j] = tmp;
        }
        return idx;
    }

    private static void setChannelLSB(BufferedImage img, int pixelIndex, int channel, int bit) {
        int x = pixelIndex % img.getWidth();
        int y = pixelIndex / img.getWidth();
        int rgb = img.getRGB(x, y);
        int mask = ~(1 << (channel * 8));
        int newRgb = (rgb & mask) | (bit << (channel * 8));
        img.setRGB(x, y, newRgb);
    }

    private static int getChannelLSB(BufferedImage img, int pixelIndex, int channel) {
        int x = pixelIndex % img.getWidth();
        int y = pixelIndex / img.getWidth();
        int rgb = img.getRGB(x, y);
        return (rgb >> (channel * 8)) & 1;
    }
}

