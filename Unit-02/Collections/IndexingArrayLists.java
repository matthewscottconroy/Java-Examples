import java.util.*;

public class IndexingArrayLists {
    public static void main(String[] args) {
        List<String> planets = new ArrayList<>();
        planets.add("Mercury");
        planets.add("Venus");
        planets.add("Earth");

        System.out.println("index 0: " + planets.get(0));
        planets.set(2, "Terra");
        System.out.println(planets);

        planets.add(1, "???"); // insert shifts elements right
        System.out.println(planets);
    }
}

