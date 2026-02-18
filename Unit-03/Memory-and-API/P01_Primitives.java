// File: P01_Primitives.java
public class P01_Primitives {
    public static void main(String[] args) {
        Debug.sep("primitives (values live in the stack frame for main)");

        boolean z = true;
        byte b = 7;
        short s = 32000;
        char c = 'A';
        int i = 123456;
        long l = 9_000_000_000L;
        float f = 3.14f;
        double d = 2.718281828;

        System.out.println("boolean z = " + z);
        System.out.println("byte    b = " + b);
        System.out.println("short   s = " + s);
        System.out.println("char    c = " + c + " (code=" + (int)c + ")");
        System.out.println("int     i = " + i);
        System.out.println("long    l = " + l);
        System.out.println("float   f = " + f);
        System.out.println("double  d = " + d);

        // Key point: these are just bits/values, not references to heap objects.
    }
}
