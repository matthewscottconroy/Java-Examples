public class GuardClauses {
    static double average(int[] xs) {
        if (xs == null) {
            throw new IllegalArgumentException("xs is null");
        }
        if (xs.length == 0) {
            throw new IllegalArgumentException("xs is empty");
        }

        int total = 0;
        for (int x : xs) {
            total += x;
        }
        return (double) total / xs.length;
    }

    public static void main(String[] args) {
        int[] scores = { 80, 90, 100 };
        System.out.println("Average: " + average(scores));
    }
}

