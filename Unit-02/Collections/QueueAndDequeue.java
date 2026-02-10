import java.util.*;

public class QueueAndDequeue{
    public static void main(String[] args) {
        Queue<String> q = new ArrayDeque<>();
        q.add("A");
        q.add("B");
        q.add("C");
        System.out.println("poll: " + q.poll()); // A
        System.out.println("poll: " + q.poll()); // B
        System.out.println("remaining: " + q);

        Deque<String> d = new ArrayDeque<>();
        d.push("first");  // stack push (front)
        d.push("second");
        System.out.println("pop: " + d.pop()); // second
        System.out.println("pop: " + d.pop()); // first
    }
}

