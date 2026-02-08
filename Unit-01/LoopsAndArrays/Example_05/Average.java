public class Average{
    public static void main(String[] args) {
        int[] numbers = {10, 20, 30, 40};

        int sum = 0;

        for (int i = 0; i < numbers.length; i++) {
            sum = sum + numbers[i];
        }

        double average = (double) sum / numbers.length;
        System.out.println("Average: " + average);
    }
}

