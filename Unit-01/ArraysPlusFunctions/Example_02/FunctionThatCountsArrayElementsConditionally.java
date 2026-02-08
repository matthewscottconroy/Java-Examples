public class FunctionThatCountsArrayElementsConditionally {
    static int countEvens(int[] a) {
        int count = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] % 2 == 0) count++;
        }
        return count;
    }

    public static void main(String[] args) {
        int[] nums = {2, 7, 4, 9, 6, 1};
        System.out.println("Evens: " + countEvens(nums));
    }
}

