import java.util.*;

// ====== Behavioral Interfaces ======
interface CookingMethod {
    void cook(String dishName);
}

class Bake implements CookingMethod {
    @Override
    public void cook(String dishName) {
        System.out.println("Baking the " + dishName + " in the oven...");
    }
}

class Boil implements CookingMethod {
    @Override
    public void cook(String dishName) {
        System.out.println("Boiling the " + dishName + " in hot water...");
    }
}

class Saute implements CookingMethod {
    @Override
    public void cook(String dishName) {
        System.out.println("Sautéing the " + dishName + " in a pan with oil...");
    }
}

interface SeasoningStrategy {
    void season(String dishName);
}

class ItalianSeasoning implements SeasoningStrategy {
    @Override
    public void season(String dishName) {
        System.out.println("Adding basil, oregano, and olive oil to the " + dishName + ".");
    }
}

class AsianSeasoning implements SeasoningStrategy {
    @Override
    public void season(String dishName) {
        System.out.println("Adding soy sauce, ginger, and sesame oil to the " + dishName + ".");
    }
}

class NoSeasoning implements SeasoningStrategy {
    @Override
    public void season(String dishName) {
        System.out.println("Leaving the " + dishName + " unseasoned for now.");
    }
}

// ====== Core Domain Class Using Composition ======
class Recipe {
    private final String name;
    private final List<String> ingredients;
    private CookingMethod cookingMethod;
    private SeasoningStrategy seasoningStrategy;

    public Recipe(String name, List<String> ingredients,
                  CookingMethod cookingMethod, SeasoningStrategy seasoningStrategy) {
        this.name = name;
        this.ingredients = ingredients;
        this.cookingMethod = cookingMethod;
        this.seasoningStrategy = seasoningStrategy;
    }

    // Composition allows swapping parts freely
    public void setCookingMethod(CookingMethod method) {
        this.cookingMethod = method;
    }

    public void setSeasoningStrategy(SeasoningStrategy strategy) {
        this.seasoningStrategy = strategy;
    }

    public void prepare() {
        System.out.println("Preparing recipe for: " + name);
        System.out.println("Ingredients: " + String.join(", ", ingredients));
        seasoningStrategy.season(name);
        cookingMethod.cook(name);
        System.out.println("✅ " + name + " is ready!\n");
    }
}

// ====== Demo ======
public class RecipeCompositionDemo {
    public static void main(String[] args) {
        Recipe pasta = new Recipe(
            "Pasta Primavera",
            List.of("pasta", "vegetables", "olive oil"),
            new Boil(),
            new ItalianSeasoning()
        );

        Recipe stirFry = new Recipe(
            "Vegetable Stir Fry",
            List.of("broccoli", "tofu", "soy sauce"),
            new Saute(),
            new AsianSeasoning()
        );

        Recipe plainCake = new Recipe(
            "Plain Cake",
            List.of("flour", "eggs", "sugar", "milk"),
            new Bake(),
            new NoSeasoning()
        );

        pasta.prepare();
        stirFry.prepare();
        plainCake.prepare();

        // Dynamically change behavior (composition flexibility)
        System.out.println("Chef decides to boil the stir fry ingredients instead!");
        stirFry.setCookingMethod(new Boil());
        stirFry.prepare();
    }
}

