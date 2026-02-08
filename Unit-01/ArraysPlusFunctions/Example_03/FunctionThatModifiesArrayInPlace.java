public class FunctionThatModifiesArrayInPlace {
    static void doubleInPlace(int[] a) {
        for (int i = 0; i < a.length; i++) {
            a[i] *= 2;
        }
    }

    static void printArrayOneLine(int[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i]);
            if (i < a.length - 1) System.out.print(", ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int[] nums = {1, 2, 3, 4, 5};
        printArrayOneLine(nums);
        doubleInPlace(nums);
        printArrayOneLine(nums);
    }
}

