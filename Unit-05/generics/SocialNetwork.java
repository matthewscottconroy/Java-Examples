// Generic class representing a relationship between two entities
class Relationship<T> {
    private T first;
    private T second;
    private String type; // e.g., "Friendship", "Partnership", "Mentorship"

    public Relationship(T first, T second, String type) {
        this.first = first;
        this.second = second;
        this.type = type;
    }

    public void describe() {
        System.out.println(first + " and " + second + " have a " + type + ".");
    }

    public T getFirst() { return first; }
    public T getSecond() { return second; }
}

// Simple Person class
class Person {
    private String name;

    public Person(String name) { this.name = name; }

    @Override
    public String toString() { return name; }
}

// Simple Pet class
class Pet {
    private String name;

    public Pet(String name) { this.name = name; }

    @Override
    public String toString() { return name + " (pet)"; }
}

// Main program demonstrating generics in social relationships
public class SocialNetwork {
    public static void main(String[] args) {
        // Friendship between two people
        Relationship<Person> friendship = new Relationship<>(
            new Person("Alice"),
            new Person("Bob"),
            "friendship"
        );

        // Pet ownership (Person-Pet) requires a generic of a common supertype
        // So we could generalize to Object if we wanted cross-type relationships:
        Relationship<Object> ownership = new Relationship<>(
            new Person("Charlie"),
            new Pet("Whiskers"),
            "ownership"
        );

        friendship.describe();
        ownership.describe();
    }
}

