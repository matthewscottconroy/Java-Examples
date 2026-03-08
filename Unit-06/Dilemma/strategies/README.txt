Submitting a Custom Strategy
=============================

Any .class file in this directory that implements the Strategy interface
will be automatically discovered and entered in the tournament.

Steps
-----
1. Write your strategy in a Java file. It must:
   - Be a public class with a public no-arg constructor
   - Implement com.examples.dilemma.strategy.Strategy

Example skeleton:
   import com.examples.dilemma.strategy.*;
   public class MyStrategy implements Strategy {
       @Override public String getName() { return "My Strategy"; }
       @Override public String getDescription() { return "..."; }
       @Override public Move choose(GameHistory history) {
           // Your logic here
           return Move.COOPERATE;
       }
       @Override public void reset() { /* clear any state */ }
   }

2. Compile it against the project jar:
   javac -cp target/dilemma-1.0-SNAPSHOT.jar MyStrategy.java

3. Move the .class file here:
   mv MyStrategy.class strategies/

4. Run the tournament:
   mvn exec:java          (CLI)
   mvn javafx:run         (GUI)

Your strategy will appear in the standings alongside the built-in ones.
