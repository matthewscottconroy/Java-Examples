package com.examples.app;  // the entry point lives in its own "app" package

// Import statements bring classes from other packages into scope.
// Without these, the compiler would not know where Greeter or Fareweller are.
import com.examples.greeting.Greeter;
import com.examples.farewell.Fareweller;

/**
 * Entry point for the HelloPackages project.
 *
 * <p>This class imports and uses classes from two other packages in the same
 * project, showing how packages divide a codebase into named groups.
 */
public class Main {

    public static void main(String[] args) {
        Greeter    greeter    = new Greeter();
        Fareweller fareweller = new Fareweller();

        System.out.println(greeter.greet("World"));
        System.out.println(fareweller.farewell("World"));
    }
}
