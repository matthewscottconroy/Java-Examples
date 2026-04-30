package com.patterns.facade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Facade pattern — Home Theater.
 */
class FacadeTest {

    @Test
    @DisplayName("watchMovie turns the TV on")
    void watchMovieTurnsOnTv() {
        Television tv = new Television();
        HomeTheaterFacade facade = new HomeTheaterFacade(
                tv, new SoundReceiver(), new BluRayPlayer(), new SmartLights());
        facade.watchMovie("Test Movie");
        assertTrue(tv.isOn());
    }

    @Test
    @DisplayName("endMovie turns the TV off")
    void endMovieTurnsTvOff() {
        Television tv = new Television();
        HomeTheaterFacade facade = new HomeTheaterFacade(
                tv, new SoundReceiver(), new BluRayPlayer(), new SmartLights());
        facade.watchMovie("Test Movie");
        facade.endMovie();
        assertFalse(tv.isOn());
    }

    @Test
    @DisplayName("Facade coordinates multiple subsystems without throwing")
    void facadeDoesNotThrow() {
        HomeTheaterFacade facade = new HomeTheaterFacade(
                new Television(), new SoundReceiver(),
                new BluRayPlayer(), new SmartLights());
        assertDoesNotThrow(() -> {
            facade.watchMovie("Interstellar");
            facade.endMovie();
        });
    }
}
