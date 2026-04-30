package com.patterns.singleton;

/**
 * Demonstrates the Singleton pattern using a sports arena scoreboard.
 *
 * <p>Three separate observers — the TV broadcast booth, the stats app, and the
 * PA announcer — each retrieve the scoreboard independently. Every reference
 * points to the same object, so when the play-by-play caller updates the score,
 * the stats app and the broadcast instantly reflect the change.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Stadium Scoreboard (Singleton Pattern) ===\n");

        // Three separate systems each get "their" scoreboard
        Scoreboard broadcastBooth = Scoreboard.getInstance();
        Scoreboard statsApp       = Scoreboard.getInstance();
        Scoreboard paAnnouncer    = Scoreboard.getInstance();

        System.out.println("All three systems have the same scoreboard: "
                + (broadcastBooth == statsApp && statsApp == paAnnouncer));
        System.out.println();

        // The broadcast booth starts the game
        broadcastBooth.startGame("Lakers", "Celtics");
        System.out.println("Game starts — " + broadcastBooth);

        // The stats app records a home team basket
        statsApp.homeScores(3);
        System.out.println("Stats app logs 3-pointer — PA announcer reads: " + paAnnouncer);

        // The PA system records the away team scoring
        paAnnouncer.awayScores(2);
        System.out.println("PA logs 2-pointer  — Broadcast reads: " + broadcastBooth);

        // End of quarter
        broadcastBooth.nextPeriod();
        broadcastBooth.homeScores(2);
        broadcastBooth.awayScores(3);
        System.out.println("End Q2 — " + statsApp);   // statsApp, but same board
    }
}
