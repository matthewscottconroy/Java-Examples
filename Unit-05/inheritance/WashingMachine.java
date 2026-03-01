public class WashingMachine extends Appliance {
    public WashingMachine(String name) {
        super(name);
    }

    @Override
    public void performFunction() {
        if (!isPoweredOn()) {
            System.out.println(getName() + " is off. Cannot wash clothes.");
            return;
        }
        System.out.println(getName() + " is washing clothes...");
    }
}

