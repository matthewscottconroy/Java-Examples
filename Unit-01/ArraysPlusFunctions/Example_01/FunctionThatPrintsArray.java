public class FunctionThatPrintsArray {
    static void printArray(int[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i]);
        }
    }

    public static void main(String[] args) {
        int[] nums = {10, 20, 30};
        printArray(nums);
    }
}

