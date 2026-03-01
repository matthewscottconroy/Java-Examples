public class SpaceStationTest {
    public static void main(String[] args) {
        LifeSupportSystem suit = new AstronautSuit();
        LifeSupportSystem base = new SpaceBaseModule();

        suit.regulateOxygenLevel(22.0);
        suit.regulateTemperature(24.0);

        base.regulateOxygenLevel(20.0);
        base.regulateTemperature(22.0);

        System.out.println("Suit stable: " + suit.isStable());
        System.out.println("Base stable: " + base.isStable());
    }
}

