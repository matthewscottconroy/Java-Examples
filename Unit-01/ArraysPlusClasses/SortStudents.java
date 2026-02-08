public class SortStudents {
    static class Student {
        String name;
        int score;

        Student(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    static void swap(Student[] a, int i, int j) {
        Student tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    static void selectionSortByScoreDescending(Student[] a) {
        for (int i = 0; i < a.length; i++) {
            int best = i;
            for (int j = i + 1; j < a.length; j++) {
                if (a[j].score > a[best].score) best = j;
            }
            swap(a, i, best);
        }
    }

    static void print(Student[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i].name + ": " + a[i].score);
        }
    }

    public static void main(String[] args) {
        Student[] students = {
            new Student("Alice", 85),
            new Student("Bob", 92),
            new Student("Carol", 78),
            new Student("Dan", 90)
        };

        selectionSortByScoreDescending(students);
        print(students);
    }
}

