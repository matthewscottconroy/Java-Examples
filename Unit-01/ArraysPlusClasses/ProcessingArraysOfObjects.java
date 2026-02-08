public class ProcessingArraysOfObjects {
    static class Student {
        String name;
        int score;

        Student(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    static double averageScore(Student[] students) {
        int total = 0;
        for (int i = 0; i < students.length; i++) {
            total += students[i].score;
        }
        return (double) total / students.length;
    }

    public static void main(String[] args) {
        Student[] students = {
            new Student("Alice", 85),
            new Student("Bob", 92),
            new Student("Carol", 78),
            new Student("Dan", 90)
        };

        System.out.println("Average: " + averageScore(students));
    }
}

