package com.markovmonopoly.examples;

import com.markovmonopoly.ui.ConsoleMenu;
import com.markovmonopoly.ui.TableFormatter;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * Menu for selecting and running the classic Markov chain examples.
 */
public final class ExampleRunner {

    private ExampleRunner() {}

    public static void run(Scanner in, PrintStream out) {
        ConsoleMenu menu = new ConsoleMenu(
            "Classic Markov Chain Examples",
            List.of(
                new ConsoleMenu.MenuItem("1", "Weather Model — intro to chains, stationary distribution, convergence"),
                new ConsoleMenu.MenuItem("2", "Gambler's Ruin — absorbing chains, ruin probability, game duration"),
                new ConsoleMenu.MenuItem("3", "PageRank — Markov chains and Google's search algorithm"),
                new ConsoleMenu.MenuItem("4", "Ehrenfest Urn — birth-death chain, detailed balance, thermodynamics"),
                new ConsoleMenu.MenuItem("a", "Run all examples in sequence"),
                new ConsoleMenu.MenuItem("b", "Back to main menu")
            ),
            in, out
        );

        while (true) {
            String choice = menu.prompt();
            switch (choice) {
                case "1" -> { WeatherExample.run(out);   menu.pressEnterToContinue(); }
                case "2" -> { GamblersRuinExample.run(out); menu.pressEnterToContinue(); }
                case "3" -> { PageRankExample.run(out);  menu.pressEnterToContinue(); }
                case "4" -> { EhrenfestExample.run(out); menu.pressEnterToContinue(); }
                case "a" -> {
                    out.println(TableFormatter.sectionHeader("ALL EXAMPLES"));
                    out.println("Running all examples. This may take a moment...\n");
                    WeatherExample.run(out);
                    GamblersRuinExample.run(out);
                    PageRankExample.run(out);
                    EhrenfestExample.run(out);
                    menu.pressEnterToContinue();
                }
                case "b" -> { return; }
            }
        }
    }
}
