// File: P04_PassObject.java
public class P04_PassObject {

    static class Box {
        int value;
        Box(int value) { this.value = value; }
        public String toString() { return "Box{value=" + value + "}"; }
    }

    static void mutate(Box b) {
        Debug.sep("inside mutate(b)");
        System.out.println("mutate: b points to " + Debug.id(b));
        b.value = b.value + 1;
        System.out.println("mutate after: b = " + b);
    }

    public static void main(String[] args) {
        Debug.sep("passing object references by value");
        Box box = new Box(10);
        System.out.println("main: box points to " + Debug.id(box) + " " + box);

        mutate(box);

        System.out.println("main after mutate(box): box points to " + Debug.id(box) + " " + box);
        System.out.println("(same object mutated on heap)");
    }
}
