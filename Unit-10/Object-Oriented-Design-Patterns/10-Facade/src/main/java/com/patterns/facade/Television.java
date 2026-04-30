package com.patterns.facade;

/** Subsystem component — the television set. */
public class Television {
    private boolean on = false;
    private String  input = "TV";

    /** Powers on the TV. */
    public void powerOn()  { on = true;  System.out.println("  TV: power ON"); }

    /** Powers off the TV. */
    public void powerOff() { on = false; System.out.println("  TV: power OFF"); }

    /**
     * Switches to the specified HDMI input.
     * @param hdmi the HDMI port number
     */
    public void setInput(int hdmi) {
        input = "HDMI " + hdmi;
        System.out.println("  TV: switched to " + input);
    }

    /** @return true if the TV is on */
    public boolean isOn() { return on; }
}
