public class ArraysAndFunctions {
    static int sum(int[] xs) {
        int total = 0;
        for (int x : xs) {
            total += x;
        }
        return total;
    }

    public static void main(String[] args) {
        int[] scores = { 10, 20, 30, 40 };
        System.out.println("Sum: " + sum(scores));
    }
}

