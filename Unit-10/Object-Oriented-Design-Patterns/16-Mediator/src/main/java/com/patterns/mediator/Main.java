package com.patterns.mediator;

/**
 * Demonstrates the Mediator pattern with an air traffic control tower.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Air Traffic Control Tower (Mediator Pattern) ===\n");

        ControlTower tower = new ControlTower();

        Aircraft aa101 = new Aircraft("AA-101", tower);
        Aircraft ua202 = new Aircraft("UA-202", tower);
        Aircraft dl303 = new Aircraft("DL-303", tower);

        // AA-101 takes off
        aa101.requestTakeoff();
        System.out.println("AA-101 status: " + aa101.getStatus() + "\n");

        // UA-202 tries to take off while runway is free
        ua202.requestTakeoff();
        System.out.println("UA-202 status: " + ua202.getStatus() + "\n");

        // DL-303 tries to land while UA-202 is departing — denied
        dl303.requestLanding();
        System.out.println("DL-303 status: " + dl303.getStatus() + "\n");

        // UA-202 notifies departure complete — runway is now free
        tower.notifyComplete(ua202, "DEPARTED");

        // DL-303 tries again
        dl303.requestLanding();
        System.out.println("DL-303 status: " + dl303.getStatus());
    }
}
