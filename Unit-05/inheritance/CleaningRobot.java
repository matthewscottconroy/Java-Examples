public class CleaningRobot extends Robot {
    private int dirtCollected;

    public CleaningRobot(String name) {
        super(name); // calls Robot → Appliance constructor chain
        this.dirtCollected = 0;
    }

    // Override the abstract method from Appliance, via Robot
    @Override
    public void performFunction() {
        if (!isPoweredOn()) {
            System.out.println(getName() + " is off. Cannot clean.");
            return;
        }

        System.out.println(getName() + " starts cleaning the room...");
        dirtCollected += 5;
        consumeBattery(10);  // protected method inherited from Robot
    }

    // Add unique behavior
    public void emptyDustBin() {
        if (dirtCollected == 0) {
            System.out.println(getName() + "'s dustbin is already empty.");
        } else {
            System.out.println(getName() + " empties its dustbin. (" + dirtCollected + " units removed)");
            dirtCollected = 0;
        }
    }

    public int getDirtCollected() {
        return dirtCollected;
    }

    // Optional: specialize the powerOff method to save cleaning data
    @Override
    public void powerOff() {
        System.out.println(getName() + " is saving cleaning data...");
        super.powerOff();  // call Robot → Appliance powerOff()
    }
}

