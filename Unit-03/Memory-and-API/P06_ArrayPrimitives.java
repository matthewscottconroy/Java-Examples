// File: P06_ArrayPrimitives.java
import java.util.Arrays;

public class P06_ArrayPrimitives {
    public static void main(String[] args) {
        Debug.sep("int[] is ONE heap object containing many primitive values");

        int[] a = { 10, 20, 30 };
        System.out.println("a -> " + Debug.id(a));
        System.out.println("a contents: " + Arrays.toString(a));

        a[1] = 999;
        System.out.println("after a[1]=999: " + Arrays.toString(a));
    }
}
