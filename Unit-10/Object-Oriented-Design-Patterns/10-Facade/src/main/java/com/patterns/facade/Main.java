package com.patterns.facade;

/**
 * Demonstrates the Facade pattern with a home theater system.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Home Theater Facade (Facade Pattern) ===\n");

        HomeTheaterFacade theater = new HomeTheaterFacade(
                new Television(),
                new SoundReceiver(),
                new BluRayPlayer(),
                new SmartLights()
        );

        theater.watchMovie("2001: A Space Odyssey");
        theater.endMovie();
    }
}
