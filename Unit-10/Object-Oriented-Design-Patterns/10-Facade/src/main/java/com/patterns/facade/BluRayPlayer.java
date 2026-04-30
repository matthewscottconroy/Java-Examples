package com.patterns.facade;

/** Subsystem component — the Blu-ray player. */
public class BluRayPlayer {
    private boolean on   = false;
    private String  disc = "(none)";

    /** Powers on the player. */
    public void powerOn()  { on = true;  System.out.println("  Blu-ray: power ON"); }

    /** Powers off the player. */
    public void powerOff() { on = false; System.out.println("  Blu-ray: power OFF"); }

    /**
     * Loads and plays a disc.
     * @param title the disc title
     */
    public void play(String title) {
        disc = title;
        System.out.println("  Blu-ray: playing \"" + title + "\"");
    }

    /** Stops playback and ejects the disc. */
    public void eject() { System.out.println("  Blu-ray: ejecting \"" + disc + "\""); disc = "(none)"; }
}
