public class Main {
    public static void main(String[] args) {
        CleaningRobot roboVac = new CleaningRobot("RoboVac");

        roboVac.powerOn();
        roboVac.performFunction();
        roboVac.emptyDustBin();
        roboVac.powerOff();

        System.out.println("Battery: " + roboVac.getBatteryPercent() + "%");
    }
}

