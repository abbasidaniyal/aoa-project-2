#!/usr/bin/env python3
"""
Generate simulation data for the Greedy Set Cover Algorithm.

Creates CSV files with varying numbers of food items and nutrients
to test algorithm performance at different scales.

Output format:
    #UNIVERSE,nutrient1;nutrient2;...
    #MAX_CALORIES,value
    food_name,calories,nutrient1;nutrient2;...
    ...

Files are saved to data/setcover_f{num_foods}_n{num_nutrients}.csv
"""

import os
import random
import csv

# Configuration
DATA_DIR = "data"
RANDOM_SEED = 42  # For reproducibility

# Test configurations: (num_foods, num_nutrients)
# Varying both dimensions for comprehensive benchmarking
TEST_CONFIGS = [
    # Small sizes
    (10, 5),
    (20, 10),
    (50, 15),
    # Medium sizes
    (100, 20),
    (200, 30),
    (500, 50),
    # Large sizes
    (1000, 75),
    (2000, 100),
    (5000, 150),
    # Extra large (stress test)
    (10000, 200),
    (10000, 500),
    (50000, 500),
]

# Food item generation parameters
MIN_CALORIES = 2000
MAX_CALORIES = 2500
MIN_NUTRIENTS_PER_FOOD = 3  # Minimum nutrients a food provides
MAX_NUTRIENT_FRACTION = 0.2  # Maximum fraction of universe a food can cover
MAX_NUTRIENT_UPPER_LIMIT = 10  # Maximum number of nutrients a food can cover


def generate_universe(num_nutrients):
    """Generate a universe of nutrient names."""
    nutrients = []
    
    # Common nutrient prefixes for realistic naming
    prefixes = [
        "Vitamin", "Mineral", "Protein", "Fiber", "Omega", 
        "Amino", "Enzyme", "Antioxidant", "Electrolyte", "Nutrient"
    ]
    
    for i in range(num_nutrients):
        prefix = prefixes[i % len(prefixes)]
        suffix = chr(ord('A') + (i // len(prefixes))) if i >= len(prefixes) else ""
        index = (i % len(prefixes)) + 1
        nutrient_name = f"{prefix}{suffix}{index}"
        nutrients.append(nutrient_name)
    
    return nutrients


def generate_food_item(food_id, universe, min_nutrients, max_nutrients):
    """Generate a random food item with calories and nutrient coverage."""
    # Random calorie value
    calories = random.randint(MIN_CALORIES, MAX_CALORIES)
    
    # Random number of nutrients this food provides
    num_nutrients = random.randint(min_nutrients, max_nutrients)
    num_nutrients = min(num_nutrients, len(universe))
    
    # Randomly select nutrients
    nutrients = random.sample(universe, num_nutrients)
    
    # Generate food name
    food_types = [
        "Apple", "Banana", "Carrot", "Spinach", "Chicken", "Beef", "Salmon",
        "Rice", "Pasta", "Bread", "Milk", "Cheese", "Yogurt", "Eggs", "Tofu",
        "Broccoli", "Tomato", "Potato", "Orange", "Grape", "Lettuce", "Onion",
        "Garlic", "Pepper", "Corn", "Beans", "Lentils", "Almonds", "Walnuts"
    ]
    food_name = f"{random.choice(food_types)}_{food_id}"
    
    return {
        "name": food_name,
        "calories": calories,
        "nutrients": nutrients
    }


def calculate_max_calories(food_items, universe):
    """
    Calculate a reasonable max calorie limit.
    
    Strategy: Set limit high enough to potentially cover all nutrients,
    but low enough to require careful selection.
    Approximately 40-60% of total available calories.
    """
    total_calories = sum(f["calories"] for f in food_items)
    
    # Use 50% of total as a baseline, with some randomness
    fraction = random.uniform(0.4, 0.6)
    max_calories = total_calories * fraction
    
    # Ensure it's at least enough for the minimum number of foods needed
    # to cover all nutrients (rough estimate)
    min_foods_estimate = len(universe) // 3 + 1
    avg_calories = total_calories / len(food_items)
    min_needed = min_foods_estimate * avg_calories
    
    return max(max_calories, min_needed)


def ensure_coverage(food_items, universe):
    """
    Ensure that the generated food items can potentially cover all nutrients.
    If any nutrient is not covered, add it to a random food item.
    """
    covered = set()
    for food in food_items:
        covered.update(food["nutrients"])
    
    uncovered = set(universe) - covered
    
    # Add uncovered nutrients to random food items
    for nutrient in uncovered:
        random_food = random.choice(food_items)
        random_food["nutrients"].append(nutrient)


def generate_test_file(num_foods, num_nutrients):
    """Generate a complete test file with the given configuration."""
    # Generate universe
    universe = generate_universe(num_nutrients)
    
    # Calculate nutrient coverage range for foods
    min_nutrients = MIN_NUTRIENTS_PER_FOOD
    max_nutrients = max(MAX_NUTRIENT_UPPER_LIMIT, int(num_nutrients * MAX_NUTRIENT_FRACTION))
    
    # Generate food items
    food_items = []
    for i in range(num_foods):
        food = generate_food_item(i + 1, universe, min_nutrients, max_nutrients)
        food_items.append(food)
    
    # Ensure all nutrients can be covered
    ensure_coverage(food_items, universe)
    
    # Calculate max calories
    max_calories = calculate_max_calories(food_items, universe)
    
    # Create output filename
    filename = f"setcover_f{num_foods}_n{num_nutrients}.csv"
    filepath = os.path.join(DATA_DIR, filename)
    
    # Write to CSV
    with open(filepath, 'w', newline='') as f:
        # Write universe header
        universe_str = ";".join(universe)
        f.write(f"#UNIVERSE,{universe_str}\n")
        
        # Write max calories header
        f.write(f"#MAX_CALORIES,{max_calories:.2f}\n")
        
        # Write food items
        for food in food_items:
            nutrients_str = ";".join(food["nutrients"])
            f.write(f"{food['name']},{food['calories']},{nutrients_str}\n")
    
    return filename, num_foods, num_nutrients


def main():
    """Main function to generate all test data files."""
    # Set random seed for reproducibility
    random.seed(RANDOM_SEED)
    
    # Create data directory if it doesn't exist
    if not os.path.exists(DATA_DIR):
        os.makedirs(DATA_DIR)
        print(f"Created directory: {DATA_DIR}/")
    
    print("=== Generating Set Cover Test Data ===")
    print(f"Output directory: {DATA_DIR}/")
    print(f"Random seed: {RANDOM_SEED}")
    print()
    
    generated_files = []
    
    for num_foods, num_nutrients in TEST_CONFIGS:
        filename, nf, nn = generate_test_file(num_foods, num_nutrients)
        generated_files.append((filename, nf, nn))
        print(f"Generated: {filename} (foods={nf}, nutrients={nn})")
    
    print()
    print(f"Total files generated: {len(generated_files)}")
    print()
    print("To run the simulation:")
    print("  javac *.java")
    print("  java SimulateSetCoverGreedy")


if __name__ == "__main__":
    main()
