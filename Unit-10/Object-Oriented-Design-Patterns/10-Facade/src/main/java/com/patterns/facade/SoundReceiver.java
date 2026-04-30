package com.patterns.facade;

/** Subsystem component — the AV sound receiver / amplifier. */
public class SoundReceiver {
    private int volume = 0;

    /** Powers on the receiver. */
    public void powerOn()  { System.out.println("  Receiver: power ON"); }

    /** Powers off the receiver. */
    public void powerOff() { System.out.println("  Receiver: power OFF"); volume = 0; }

    /**
     * Sets the listening volume.
     * @param level volume 0–100
     */
    public void setVolume(int level) {
        volume = Math.max(0, Math.min(100, level));
        System.out.println("  Receiver: volume → " + volume);
    }

    /** Selects surround-sound mode. */
    public void setSurroundMode() { System.out.println("  Receiver: surround mode ON"); }
}
