// File: P09_Composition.java
public class P09_Composition {

    static class Engine {
        int horsepower;
        Engine(int horsepower) { this.horsepower = horsepower; }
        public String toString() { return "Engine{hp=" + horsepower + "}"; }
    }

    static class Car {
        String model;
        Engine engine; // composition: Car has an Engine reference
        Car(String model, Engine engine) { this.model = model; this.engine = engine; }
        public String toString() { return "Car{model=" + model + ", engine=" + engine + "}"; }
    }

    public static void main(String[] args) {
        Debug.sep("composition: one heap object points to another");

        Engine e = new Engine(300);
        Car c = new Car("Roadster", e);

        System.out.println("Engine e -> " + Debug.id(e) + " " + e);
        System.out.println("Car    c -> " + Debug.id(c) + " " + c);
        System.out.println("c.engine -> " + Debug.id(c.engine));

        c.engine.horsepower = 999;
        System.out.println("after c.engine.horsepower=999:");
        System.out.println("e = " + e);
        System.out.println("c = " + c);
    }
}
