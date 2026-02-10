public class Main {
    public static void main(String[] args) {
        Temperature t = new Temperature(20);
        t.increase(5);

        System.out.println(t.toFahrenheit());
    }
}

