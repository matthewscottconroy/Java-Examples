public class Encapsulation {
    private int count;

    public Encapsulation(int initialCount) {
        count = initialCount;
    }

    public void increment() {
        count++;
    }

    public int getCount() {
        return count;
    }

    public static void main(String[] args) {
        Encapsulation e = new Encapsulation(3);
        e.increment();
        e.increment();

        System.out.println(e.getCount());
    }
}

