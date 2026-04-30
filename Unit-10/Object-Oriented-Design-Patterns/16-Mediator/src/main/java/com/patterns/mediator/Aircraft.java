package com.patterns.mediator;

/**
 * Colleague — a flight communicating only through the {@link AirTrafficControl} mediator.
 *
 * <p>An {@code Aircraft} never calls methods on other {@code Aircraft} objects.
 * It sends requests to the tower and listens for clearances. This prevents the
 * O(n²) coupling that would arise if every aircraft needed to know about every other.
 */
public class Aircraft {

    private final String           callSign;
    private final AirTrafficControl tower;
    private String                 status = "PARKED";

    /**
     * @param callSign the aircraft's radio call sign (e.g., "AA-101")
     * @param tower    the control tower mediator
     */
    public Aircraft(String callSign, AirTrafficControl tower) {
        this.callSign = callSign;
        this.tower    = tower;
    }

    /**
     * Requests takeoff clearance from the tower and departs if granted.
     */
    public void requestTakeoff() {
        System.out.println(callSign + " requests TAKEOFF clearance.");
        if (tower.requestClearance(this, "TAKEOFF")) {
            status = "AIRBORNE";
            tower.notifyComplete(this, "DEPARTED");
        } else {
            status = "HOLDING";
        }
    }

    /**
     * Requests landing clearance from the tower and lands if granted.
     */
    public void requestLanding() {
        System.out.println(callSign + " requests LAND clearance.");
        if (tower.requestClearance(this, "LAND")) {
            status = "LANDED";
            tower.notifyComplete(this, "LANDED");
        } else {
            status = "CIRCLING";
        }
    }

    /** @return the aircraft's radio call sign */
    public String getCallSign() { return callSign; }

    /** @return the aircraft's current status */
    public String getStatus()   { return status; }
}
