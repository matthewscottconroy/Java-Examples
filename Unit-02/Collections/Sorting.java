import java.util.*;

public class Sorting{
    public static void main(String[] args) {
        List<Integer> xs = new ArrayList<>(List.of(5, 1, 9, 2));
        Collections.sort(xs); // natural order
        System.out.println(xs);
    }
}

