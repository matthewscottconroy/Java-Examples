import java.util.*;

class Student {
    String name;
    int credits;

    Student(String name, int credits) {
        this.name = name;
        this.credits = credits;
    }

    @Override
    public String toString() {
        return name + "(" + credits + ")";
    }
}

public class SortingWithComparator {
    public static void main(String[] args) {
        List<Student> roster = new ArrayList<>();
        roster.add(new Student("Ada", 90));
        roster.add(new Student("Grace", 120));
        roster.add(new Student("Linus", 60));

        roster.sort(Comparator.comparingInt(s -> s.credits));
        System.out.println("by credits: " + roster);

        roster.sort(Comparator.comparing((Student s) -> s.name).reversed());
        System.out.println("by name desc: " + roster);
    }
}

