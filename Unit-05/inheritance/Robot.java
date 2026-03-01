public class Robot extends Appliance {
    private int batteryPercent;

    public Robot(String name) {
        super(name); // calls the Appliance constructor
        this.batteryPercent = 100;
    }

    @Override
    public void performFunction() {
        if (!isPoweredOn()) {
            System.out.println(getName() + " is off. Cannot perform function.");
            return;
        }
        System.out.println(getName() + " is performing general robotic duties.");
        consumeBattery(10);
    }

    public void moveForward(int steps) {
        if (!isPoweredOn()) {
            System.out.println(getName() + " is off. Cannot move.");
            return;
        }
        System.out.println(getName() + " moves forward " + steps + " steps.");
        consumeBattery(steps);
    }

    protected void consumeBattery(int amount) {
        batteryPercent = Math.max(0, batteryPercent - amount);
    }

    public int getBatteryPercent() {
        return batteryPercent;
    }
}

