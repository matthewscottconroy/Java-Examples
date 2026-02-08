public class Overloading {
    static double area(double radius) {
        return Math.PI * radius * radius;
    }

    static double area(double width, double height) {
        return width * height;
    }

    public static void main(String[] args) {
        System.out.println("Circle area: " + area(3.0));
        System.out.println("Rect area:   " + area(4.0, 5.0));
    }
}

