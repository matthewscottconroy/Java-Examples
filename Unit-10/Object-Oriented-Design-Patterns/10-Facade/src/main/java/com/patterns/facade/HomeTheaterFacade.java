package com.patterns.facade;

/**
 * Facade — a single, simple interface over the entire home theater subsystem.
 *
 * <p>Without this facade, the homeowner would need to know the correct order
 * of operations: power on the receiver before the TV, switch HDMI input after
 * the TV boots, set surround mode before pressing play, and so on — six steps
 * across four different remote controls.
 *
 * <p>With the facade, it is two method calls: {@link #watchMovie(String)} and
 * {@link #endMovie()}. The facade knows the order; the homeowner does not need to.
 *
 * <p><b>Pattern roles:</b>
 * <pre>
 *   HomeTheaterFacade — Facade
 *   Television, SoundReceiver, BluRayPlayer, SmartLights — Subsystem classes
 * </pre>
 */
public class HomeTheaterFacade {

    private final Television   tv;
    private final SoundReceiver receiver;
    private final BluRayPlayer  bluRay;
    private final SmartLights   lights;

    /**
     * @param tv       the television to control
     * @param receiver the AV receiver to control
     * @param bluRay   the Blu-ray player to control
     * @param lights   the smart lights to control
     */
    public HomeTheaterFacade(Television tv, SoundReceiver receiver,
                              BluRayPlayer bluRay, SmartLights lights) {
        this.tv       = tv;
        this.receiver = receiver;
        this.bluRay   = bluRay;
        this.lights   = lights;
    }

    /**
     * Performs the full "movie night" startup sequence.
     *
     * <p>Turns on all equipment in the correct order, dims the lights, and
     * begins playback — replacing six manual steps with one method call.
     *
     * @param movieTitle the title of the movie to play
     */
    public void watchMovie(String movieTitle) {
        System.out.println(">> Getting ready to watch \"" + movieTitle + "\"...");
        receiver.powerOn();
        receiver.setVolume(30);
        receiver.setSurroundMode();
        tv.powerOn();
        tv.setInput(1);
        bluRay.powerOn();
        bluRay.play(movieTitle);
        lights.dim(10);
        System.out.println(">> Enjoy the movie!\n");
    }

    /**
     * Performs the full shutdown sequence.
     *
     * <p>Stops playback, brings lights back up, and powers off all equipment
     * in the correct order.
     */
    public void endMovie() {
        System.out.println(">> Shutting down home theater...");
        bluRay.eject();
        bluRay.powerOff();
        tv.powerOff();
        receiver.powerOff();
        lights.fullBrightness();
        System.out.println(">> Good night!\n");
    }
}
