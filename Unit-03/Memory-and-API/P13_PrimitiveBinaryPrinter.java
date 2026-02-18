public class P13_PrimitiveBinaryPrinter {

    public static void main(String[] args) {

        boolean z = true;
        byte b = -42;
        short s = 12345;
        char c = 'A';
        int i = -123456789;
        long l = 12345678910111213L;
        float f = -3.75f;
        double d = 3.141592653589793;

        printBoolean(z);
        printByte(b);
        printShort(s);
        printChar(c);
        printInt(i);
        printLong(l);
        printFloat(f);
        printDouble(d);
    }

    // ---------- BOOLEAN ----------
    static void printBoolean(boolean value) {
        System.out.println("\n=== boolean ===");
        System.out.println("value: " + value);
        System.out.println("binary (conceptual 1-bit): " + (value ? "1" : "0"));
    }

    // ---------- BYTE ----------
    static void printByte(byte value) {
        System.out.println("\n=== byte (8 bits) ===");
        System.out.println("value: " + value);
        System.out.println("binary: " + pad(Integer.toBinaryString(value & 0xFF), 8));
    }

    // ---------- SHORT ----------
    static void printShort(short value) {
        System.out.println("\n=== short (16 bits) ===");
        System.out.println("value: " + value);
        System.out.println("binary: " + pad(Integer.toBinaryString(value & 0xFFFF), 16));
    }

    // ---------- CHAR ----------
    static void printChar(char value) {
        System.out.println("\n=== char (16 bits, unsigned) ===");
        System.out.println("value: '" + value + "'");
        System.out.println("unicode code point: " + (int) value);
        System.out.println("binary: " + pad(Integer.toBinaryString(value), 16));
    }

    // ---------- INT ----------
    static void printInt(int value) {
        System.out.println("\n=== int (32 bits) ===");
        System.out.println("value: " + value);
        System.out.println("binary: " + pad(Integer.toBinaryString(value), 32));
    }

    // ---------- LONG ----------
    static void printLong(long value) {
        System.out.println("\n=== long (64 bits) ===");
        System.out.println("value: " + value);
        System.out.println("binary: " + pad(Long.toBinaryString(value), 64));
    }

    // ---------- FLOAT ----------
    static void printFloat(float value) {
        System.out.println("\n=== float (32 bits, IEEE 754) ===");
        System.out.println("value: " + value);

        int bits = Float.floatToIntBits(value);
        String binary = pad(Integer.toBinaryString(bits), 32);

        System.out.println("binary: " + binary);
        System.out.println("sign:     " + binary.substring(0,1));
        System.out.println("exponent: " + binary.substring(1,9));
        System.out.println("mantissa: " + binary.substring(9));
    }

    // ---------- DOUBLE ----------
    static void printDouble(double value) {
        System.out.println("\n=== double (64 bits, IEEE 754) ===");
        System.out.println("value: " + value);

        long bits = Double.doubleToLongBits(value);
        String binary = pad(Long.toBinaryString(bits), 64);

        System.out.println("binary: " + binary);
        System.out.println("sign:     " + binary.substring(0,1));
        System.out.println("exponent: " + binary.substring(1,12));
        System.out.println("mantissa: " + binary.substring(12));
    }

    // ---------- Utility ----------
    static String pad(String binary, int width) {
        return String.format("%" + width + "s", binary).replace(' ', '0');
    }
}
