public class ArraysOfObjects {
    static class Student {
        String name;
        int score;

        Student(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    public static void main(String[] args) {
        Student[] students = new Student[3];
        students[0] = new Student("Alice", 85);
        students[1] = new Student("Bob", 92);
        students[2] = new Student("Carol", 78);

        for (int i = 0; i < students.length; i++) {
            System.out.println(students[i].name + ": " + students[i].score);
        }
    }
}

