// File: P07_ArrayObjects.java
import java.util.Arrays;

public class P07_ArrayObjects {

    static class Box {
        int value;
        Box(int value) { this.value = value; }
        public String toString() { return "Box{value=" + value + "}"; }
    }

    public static void main(String[] args) {
        Debug.sep("Box[] stores REFERENCES; each Box is a separate heap object");

        Box[] boxes = new Box[3]; // array object on heap; elements start null
        System.out.println("boxes array -> " + Debug.id(boxes));
        System.out.println("boxes initially: " + Arrays.toString(boxes));

        boxes[0] = new Box(10);
        boxes[1] = new Box(20);
        boxes[2] = new Box(30);

        System.out.println("boxes after fills: " + Arrays.toString(boxes));
        System.out.println("boxes[0] -> " + Debug.id(boxes[0]));
        System.out.println("boxes[1] -> " + Debug.id(boxes[1]));
        System.out.println("boxes[2] -> " + Debug.id(boxes[2]));

        boxes[1].value = 999; // mutate the object referenced by element 1
        System.out.println("after boxes[1].value=999: " + Arrays.toString(boxes));

        boxes[1] = new Box(555); // replace the reference stored in the array slot
        System.out.println("after boxes[1]=new Box(555): " + Arrays.toString(boxes));
        System.out.println("new boxes[1] -> " + Debug.id(boxes[1]));
    }
}
