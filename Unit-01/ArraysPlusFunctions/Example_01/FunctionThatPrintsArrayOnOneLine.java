public class FunctionThatPrintsArrayOnOneLine {
    static void printArrayOneLine(int[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i]);
            if (i < a.length - 1) System.out.print(", ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int[] nums = {10, 20, 30, 40};
        printArrayOneLine(nums);
    }
}

