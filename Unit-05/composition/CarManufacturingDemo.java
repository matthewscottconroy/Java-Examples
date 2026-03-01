// ===== Engine Behavior =====
interface Engine {
    void start();
    int getHorsepower();
}

class ElectricEngine implements Engine {
    @Override
    public void start() {
        System.out.println("⚡ Electric motor whirs silently to life.");
    }

    @Override
    public int getHorsepower() {
        return 250;
    }
}

class GasEngine implements Engine {
    @Override
    public void start() {
        System.out.println("🔥 Gas engine roars to life with combustion.");
    }

    @Override
    public int getHorsepower() {
        return 400;
    }
}

class HybridEngine implements Engine {
    @Override
    public void start() {
        System.out.println("🔋 Hybrid engine engages both motor and combustion components.");
    }

    @Override
    public int getHorsepower() {
        return 350;
    }
}

// ===== Transmission Behavior =====
interface Transmission {
    void shiftUp();
    void shiftDown();
    String getType();
}

class ManualTransmission implements Transmission {
    @Override
    public void shiftUp() { System.out.println("Manually shifting up."); }
    @Override
    public void shiftDown() { System.out.println("Manually shifting down."); }
    @Override
    public String getType() { return "Manual"; }
}

class AutomaticTransmission implements Transmission {
    @Override
    public void shiftUp() { System.out.println("Automatic system shifts up."); }
    @Override
    public void shiftDown() { System.out.println("Automatic system shifts down."); }
    @Override
    public String getType() { return "Automatic"; }
}

// ===== Paint Job Behavior =====
interface PaintJob {
    void applyPaint();
}

class MatteBlack implements PaintJob {
    @Override
    public void applyPaint() {
        System.out.println("Applying matte black finish — sleek and understated.");
    }
}

class MetallicRed implements PaintJob {
    @Override
    public void applyPaint() {
        System.out.println("Applying glossy metallic red finish — sporty and eye-catching.");
    }
}

class CustomWrap implements PaintJob {
    private final String design;
    public CustomWrap(String design) { this.design = design; }
    @Override
    public void applyPaint() {
        System.out.println("Applying custom vinyl wrap: " + design + ".");
    }
}

// ===== The Composed Car Class =====
class Car {
    private final String modelName;
    private Engine engine;
    private Transmission transmission;
    private PaintJob paintJob;

    public Car(String modelName, Engine engine, Transmission transmission, PaintJob paintJob) {
        this.modelName = modelName;
        this.engine = engine;
        this.transmission = transmission;
        this.paintJob = paintJob;
    }

    public void assemble() {
        System.out.println("Assembling " + modelName + "...");
        engine.start();
        transmission.shiftUp();
        paintJob.applyPaint();
        System.out.println(modelName + " ready! Horsepower: " + engine.getHorsepower() + " | Transmission: " + transmission.getType());
        System.out.println("✅ Manufacturing complete.\n");
    }

    // Composition = flexible replacement of parts
    public void setEngine(Engine engine) { this.engine = engine; }
    public void setTransmission(Transmission transmission) { this.transmission = transmission; }
    public void setPaintJob(PaintJob paintJob) { this.paintJob = paintJob; }
}

// ===== Demo =====
public class CarManufacturingDemo {
    public static void main(String[] args) {
        Car sedan = new Car("EcoSedan", new ElectricEngine(), new AutomaticTransmission(), new MatteBlack());
        sedan.assemble();

        Car sports = new Car("RoadBlazer", new GasEngine(), new ManualTransmission(), new MetallicRed());
        sports.assemble();

        // Dynamic reconfiguration — same car, new composition
        System.out.println("Upgrading the EcoSedan with a hybrid engine and custom paint!");
        sedan.setEngine(new HybridEngine());
        sedan.setPaintJob(new CustomWrap("Blue Nebula Galaxy Theme"));
        sedan.assemble();
    }
}

