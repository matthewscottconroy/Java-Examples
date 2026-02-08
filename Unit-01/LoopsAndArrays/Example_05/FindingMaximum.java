public class FindingMaximum {
    public static void main(String[] args) {
        int[] numbers = {42, 17, 88, 3, 29};

        int max = numbers[0];

        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] > max) {
                max = numbers[i];
            }
        }

        System.out.println("Max: " + max);
    }
}

