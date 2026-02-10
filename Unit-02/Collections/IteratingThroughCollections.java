import java.util.*;

public class IteratingThroughCollections {
    public static void main(String[] args) {
        List<String> tasks = new ArrayList<>();
        tasks.add("grade");
        tasks.add("email");
        tasks.add("prep");

        // for-each uses Iterable
        for (String t : tasks) {
            System.out.println("task: " + t);
        }

        // iterator (explicit)
        Iterator<String> it = tasks.iterator();
        while (it.hasNext()) {
            String t = it.next();
            System.out.println("iterator saw: " + t);
        }
    }
}

