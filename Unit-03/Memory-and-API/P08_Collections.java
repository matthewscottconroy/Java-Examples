// File: P08_Collections.java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class P08_Collections {

    static class Box {
        int value;
        Box(int value) { this.value = value; }
        public String toString() { return "Box{value=" + value + "}"; }
    }

    public static void main(String[] args) {
        Debug.sep("collections are heap objects that store REFERENCES to other objects");

        List<Box> list = new ArrayList<>();
        System.out.println("list -> " + Debug.id(list));

        Box a = new Box(10);
        Box b = new Box(20);

        list.add(a);
        list.add(b);

        System.out.println("a -> " + Debug.id(a) + " " + a);
        System.out.println("b -> " + Debug.id(b) + " " + b);
        System.out.println("list.get(0) -> " + Debug.id(list.get(0)) + " " + list.get(0));

        list.get(0).value = 999; // mutates a
        System.out.println("after list.get(0).value=999:");
        System.out.println("a = " + a);
        System.out.println("list = " + list);

        Map<String, Box> map = new HashMap<>();
        System.out.println("map -> " + Debug.id(map));
        map.put("first", a);
        map.put("second", b);

        System.out.println("map.get(\"first\") -> " + Debug.id(map.get("first")) + " " + map.get("first"));
    }
}
