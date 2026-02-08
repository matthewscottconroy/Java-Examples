public class Accumulation {
    public static void main(String[] args) {
        int[] numbers = {3, 5, 7, 9};

        int sum = 0;

        for (int i = 0; i < numbers.length; i++) {
            sum = sum + numbers[i];
        }

        System.out.println("Sum: " + sum);
    }
}

