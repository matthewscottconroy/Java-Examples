package com.patterns.mediator;

/**
 * Mediator interface — the control tower's contract.
 *
 * <p>All aircraft communication goes through the mediator. Aircraft
 * never communicate with each other directly.
 */
public interface AirTrafficControl {

    /**
     * Called by an aircraft to request clearance for an action.
     *
     * @param aircraft the aircraft making the request
     * @param request  the type of request (e.g., "TAKEOFF", "LAND", "TAXI")
     * @return true if clearance is granted
     */
    boolean requestClearance(Aircraft aircraft, String request);

    /**
     * Notifies the tower that an aircraft has completed an action.
     *
     * @param aircraft the aircraft that completed the action
     * @param action   what was completed (e.g., "LANDED", "DEPARTED")
     */
    void notifyComplete(Aircraft aircraft, String action);
}
