import java.util.*;

// --- Domain ---
class Person { final String name; Person(String n){ name=n; } public String toString(){ return name; } }
class Student extends Person { Student(String n){ super(n); } }
class Teacher extends Person { Teacher(String n){ super(n); } }

// --- Generic container with bounds ---
class Group<T extends Person> {                           // upper bound: only Persons or subclasses
    private final List<T> members = new ArrayList<>();

    public void add(T t) { members.add(t); }
    public void addAll(Collection<? extends T> src) {     // producer: ? extends T
        members.addAll(src);
    }
    public void moveAllTo(Group<? super T> dest) {        // consumer: ? super T
        for (T m : members) dest.add(m);
        members.clear();
    }
    public List<T> snapshot() { return List.copyOf(members); }
}

// --- Utilities ---
class SchoolOps {
    // Pair a teacher with a student (simple bounded type params)
    public static <T extends Teacher, S extends Student> void assignMentor(T teacher, S student) {
        System.out.println("Mentor " + teacher + " ←→ Student " + student);
    }

    // Reassign from a source that PRODUCES T to a destination that CONSUMES T
    public static <T extends Person> void reassign(Group<? extends T> src, Group<? super T> dst) {
        src.moveAllTo(dst); // thanks to PECS above, this is type-safe
    }
}

// --- Demo ---
public class ClassroomGenericsDemo {
    public static void main(String[] args) {
        Group<Student> homeroom = new Group<>();
        homeroom.addAll(List.of(new Student("Ada"), new Student("Lin")));

        Group<Teacher> faculty = new Group<>();
        faculty.add(new Teacher("Mr. Kim"));

        Group<Person> assemblyHall = new Group<>();

        // PECS in action: move Students (producer) into a Group that can consume Persons
        SchoolOps.reassign(homeroom, assemblyHall);          // <? extends T> → <? super T>

        // Simple bounded generics: Teacher ↔ Student
        SchoolOps.assignMentor(new Teacher("Ms. Patel"), new Student("Ada"));

        // Also legal: move Teachers into Persons
        SchoolOps.reassign(faculty, assemblyHall);

        System.out.println("Assembly: " + assemblyHall.snapshot());
    }
}

