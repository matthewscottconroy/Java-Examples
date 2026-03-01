public abstract class Appliance {
    private String name;
    private boolean poweredOn;

    public Appliance(String name) {
        this.name = name;
        this.poweredOn = false;
    }

    public void powerOn() {
        if (!poweredOn) {
            poweredOn = true;
            System.out.println(name + " is now powered on.");
        }
    }

    public void powerOff() {
        if (poweredOn) {
            poweredOn = false;
            System.out.println(name + " is now powered off.");
        }
    }

    public String getName() {
        return name;
    }

    public boolean isPoweredOn() {
        return poweredOn;
    }

    // 🔸 Abstract method — must be implemented by all subclasses
    public abstract void performFunction();
}

