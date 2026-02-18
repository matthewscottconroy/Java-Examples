// File: P05_ReassignReference.java
public class P05_ReassignReference {

    static class Box {
        int value;
        Box(int value) { this.value = value; }
        public String toString() { return "Box{value=" + value + "}"; }
    }

    static void reassign(Box b) {
        Debug.sep("inside reassign(b)");
        System.out.println("reassign: initially b -> " + Debug.id(b) + " " + b);

        b = new Box(999); // REASSIGN local parameter (stack frame for reassign)
        System.out.println("reassign: after b = new Box(999), b -> " + Debug.id(b) + " " + b);

        b.value = 1000;
        System.out.println("reassign: after b.value=1000, b -> " + Debug.id(b) + " " + b);
    }

    public static void main(String[] args) {
        Debug.sep("reassigning the parameter does NOT change caller's reference");
        Box box = new Box(10);

        System.out.println("main before: box -> " + Debug.id(box) + " " + box);
        reassign(box);
        System.out.println("main after:  box -> " + Debug.id(box) + " " + box);
    }
}
