// File: P10_ShallowDeepCopy.java
public class P10_ShallowDeepCopy {

    static class Engine {
        int horsepower;
        Engine(int horsepower) { this.horsepower = horsepower; }
        Engine(Engine other) { this.horsepower = other.horsepower; } // copy constructor
        public String toString() { return "Engine{hp=" + horsepower + "}"; }
    }

    static class Car {
        String model;
        Engine engine;

        Car(String model, Engine engine) {
            this.model = model;
            this.engine = engine;
        }

        // SHALLOW: copies references (engine reference shared)
        Car shallowCopy() {
            return new Car(this.model, this.engine);
        }

        // DEEP: copies nested object(s)
        Car deepCopy() {
            return new Car(this.model, new Engine(this.engine));
        }

        public String toString() { return "Car{model=" + model + ", engine=" + engine + "}"; }
    }

    public static void main(String[] args) {
        Debug.sep("shallow vs deep");

        Car original = new Car("Roadster", new Engine(300));
        Car shallow = original.shallowCopy();
        Car deep = original.deepCopy();

        System.out.println("original -> " + Debug.id(original) + " engine -> " + Debug.id(original.engine) + " " + original);
        System.out.println("shallow  -> " + Debug.id(shallow)  + " engine -> " + Debug.id(shallow.engine)  + " " + shallow);
        System.out.println("deep     -> " + Debug.id(deep)     + " engine -> " + Debug.id(deep.engine)     + " " + deep);

        Debug.sep("mutate original.engine.horsepower = 999");
        original.engine.horsepower = 999;

        System.out.println("original: " + original);
        System.out.println("shallow : " + shallow + " (changed because engine shared)");
        System.out.println("deep    : " + deep + " (unchanged because engine copied)");
    }
}
