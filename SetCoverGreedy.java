import java.util.*;

/**
 * Greedy Set Cover Algorithm for Nutrition/Food Selection Problem
 * 
 * Given a universe of nutrients U, a collection of food items where each food
 * covers a subset of nutrients and has a calorie cost, find a selection of foods
 * that covers all nutrients while respecting a daily calorie limit.
 * 
 * Uses a greedy heuristic: select food with best (nutrients covered / calories) ratio.
 */
public class SetCoverGreedy {

    /**
     * Represents a food item with its name, calorie cost, and set of nutrients it provides.
     */
    public static class FoodItem {
        public String name;
        public double calories;
        public Set<String> nutrients;

        public FoodItem(String name, double calories, Set<String> nutrients) {
            this.name = name;
            this.calories = calories;
            this.nutrients = nutrients;
        }

        @Override
        public String toString() {
            return name + " (cal=" + calories + ", nutrients=" + nutrients.size() + ")";
        }
    }

    /**
     * Result of the greedy set cover algorithm.
     */
    public static class SetCoverResult {
        public List<FoodItem> selectedFoods;
        public double totalCalories;
        public Set<String> coveredNutrients;
        public boolean fullCoverage;

        public SetCoverResult() {
            this.selectedFoods = new ArrayList<>();
            this.totalCalories = 0.0;
            this.coveredNutrients = new HashSet<>();
            this.fullCoverage = false;
        }
    }

    /**
     * Greedy Set Cover Algorithm with Calorie Constraint
     * 
     * Algorithm:
     * 1. Initialize X = {}, U_rem = U, C_tot = 0
     * 2. While U_rem is not empty:
     *    a. Build candidate set C = {S_i | C_tot + C(S_i) <= C_max AND |S_i ∩ U_rem| > 0}
     *    b. If C is empty, break
     *    c. Select S* = argmax_{S_i in C} (|S_i ∩ U_rem| / C(S_i))
     *    d. Add S* to X, update C_tot and U_rem
     * 3. Return X
     * 
     * @param universe     Set of all nutrients to be covered (U)
     * @param foodItems    List of available food items (S_1, ..., S_m)
     * @param maxCalories  Daily calorie limit (C_max)
     * @return SetCoverResult containing selected foods and coverage info
     */
    public static SetCoverResult greedySetCover(Set<String> universe, 
                                                 List<FoodItem> foodItems, 
                                                 double maxCalories) {
        SetCoverResult result = new SetCoverResult();
        
        // X <- empty set (selected foods)
        // U_rem <- U (remaining uncovered nutrients)
        Set<String> remainingNutrients = new HashSet<>(universe);
        
        // C_tot <- 0 (total calories used)
        double totalCalories = 0.0;
        
        // Track which foods have been selected (to avoid duplicates)
        Set<Integer> selectedIndices = new HashSet<>();

        // While nutrients remain uncovered
        while (!remainingNutrients.isEmpty()) {
            
            // Find best candidate food item
            int bestIndex = -1;
            double bestRatio = -1.0;
            int bestCoverage = 0;
            
            // Construct candidate set C and find S* with best ratio
            for (int i = 0; i < foodItems.size(); i++) {
                // Skip already selected foods
                if (selectedIndices.contains(i)) {
                    continue;
                }
                
                FoodItem food = foodItems.get(i);
                
                // Check calorie constraint: C_tot + C(S_i) <= C_max
                if (totalCalories + food.calories > maxCalories) {
                    continue;
                }
                
                // Calculate |S_i ∩ U_rem| (number of new nutrients covered)
                int newNutrientsCovered = 0;
                for (String nutrient : food.nutrients) {
                    if (remainingNutrients.contains(nutrient)) {
                        newNutrientsCovered++;
                    }
                }
                
                // Must cover at least one new nutrient
                if (newNutrientsCovered == 0) {
                    continue;
                }
                
                // Calculate ratio: |S_i ∩ U_rem| / C(S_i)
                double ratio = (double) newNutrientsCovered / food.calories;
                
                // Select S* = argmax ratio
                if (ratio > bestRatio) {
                    bestRatio = ratio;
                    bestIndex = i;
                    bestCoverage = newNutrientsCovered;
                }
            }
            
            // If no candidates exist, break
            if (bestIndex == -1) {
                break;
            }
            
            // Add S* to X
            FoodItem selected = foodItems.get(bestIndex);
            result.selectedFoods.add(selected);
            selectedIndices.add(bestIndex);
            
            // Update total calories: C_tot <- C_tot + C(S*)
            totalCalories += selected.calories;
            
            // Update remaining nutrients: U_rem <- U_rem - S*
            for (String nutrient : selected.nutrients) {
                remainingNutrients.remove(nutrient);
                result.coveredNutrients.add(nutrient);
            }
        }
        
        result.totalCalories = totalCalories;
        result.fullCoverage = remainingNutrients.isEmpty();
        
        return result;
    }

    /**
     * Parse a CSV line into a FoodItem.
     * Expected format: food_name,calories,nutrient1;nutrient2;nutrient3
     * 
     * @param line CSV line to parse
     * @return FoodItem parsed from the line
     */
    public static FoodItem parseFoodItem(String line) {
        String[] parts = line.split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid food item line: " + line);
        }
        
        String name = parts[0].trim();
        double calories = Double.parseDouble(parts[1].trim());
        
        Set<String> nutrients = new HashSet<>();
        String[] nutrientParts = parts[2].trim().split(";");
        for (String nutrient : nutrientParts) {
            String trimmed = nutrient.trim();
            if (!trimmed.isEmpty()) {
                nutrients.add(trimmed);
            }
        }
        
        return new FoodItem(name, calories, nutrients);
    }

    /**
     * Parse the universe of nutrients from a header line.
     * Expected format: #UNIVERSE,nutrient1;nutrient2;nutrient3;...
     * 
     * @param line Header line starting with #UNIVERSE
     * @return Set of all nutrients in the universe
     */
    public static Set<String> parseUniverse(String line) {
        Set<String> universe = new HashSet<>();
        
        if (!line.startsWith("#UNIVERSE,")) {
            throw new IllegalArgumentException("Invalid universe line: " + line);
        }
        
        String nutrientsPart = line.substring("#UNIVERSE,".length());
        String[] nutrients = nutrientsPart.split(";");
        for (String nutrient : nutrients) {
            String trimmed = nutrient.trim();
            if (!trimmed.isEmpty()) {
                universe.add(trimmed);
            }
        }
        
        return universe;
    }

    /**
     * Parse the max calories from a header line.
     * Expected format: #MAX_CALORIES,value
     * 
     * @param line Header line starting with #MAX_CALORIES
     * @return Maximum calorie limit
     */
    public static double parseMaxCalories(String line) {
        if (!line.startsWith("#MAX_CALORIES,")) {
            throw new IllegalArgumentException("Invalid max calories line: " + line);
        }
        
        String valuePart = line.substring("#MAX_CALORIES,".length()).trim();
        return Double.parseDouble(valuePart);
    }
}
