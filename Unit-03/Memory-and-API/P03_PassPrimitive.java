// File: P03_PassPrimitive.java
public class P03_PassPrimitive {

    static void bump(int x) {
        Debug.sep("inside bump(x)");
        System.out.println("bump received x = " + x);
        x = x + 1;
        System.out.println("bump changed x to " + x);
        System.out.println("(this x is a local in bump's stack frame)");
    }

    public static void main(String[] args) {
        Debug.sep("pass-by-value for primitives");
        int a = 10;
        System.out.println("main: a = " + a);
        bump(a);
        System.out.println("main after bump(a): a = " + a + " (unchanged)");
    }
}
