public class FindTheTopStudent {
    static class Student {
        String name;
        int score;

        Student(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    static Student topStudent(Student[] students) {
        Student best = students[0];
        for (int i = 1; i < students.length; i++) {
            if (students[i].score > best.score) {
                best = students[i];
            }
        }
        return best;
    }

    public static void main(String[] args) {
        Student[] students = {
            new Student("Alice", 85),
            new Student("Bob", 92),
            new Student("Carol", 78)
        };

        Student top = topStudent(students);
        System.out.println("Top: " + top.name + " with " + top.score);
    }
}

