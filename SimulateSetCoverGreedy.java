import java.io.*;
import java.util.*;

/**
 * Simulation runner for the Greedy Set Cover Algorithm.
 * 
 * Reads test data from the data/ directory, runs the algorithm multiple times
 * to get average runtime, and outputs results to results/setcover_results.csv.
 */
public class SimulateSetCoverGreedy {

    // Number of iterations for averaging runtime
    private static final int NUM_ITERATIONS = 3;

    /**
     * Represents a test case loaded from a CSV file.
     */
    static class TestCase {
        String filename;
        Set<String> universe;
        List<SetCoverGreedy.FoodItem> foodItems;
        double maxCalories;
        int numFoods;
        int numNutrients;

        TestCase(String filename) {
            this.filename = filename;
            this.universe = new HashSet<>();
            this.foodItems = new ArrayList<>();
            this.maxCalories = 0;
        }
    }

    /**
     * Load a test case from a CSV file.
     * 
     * Expected format:
     * Line 1: #UNIVERSE,nutrient1;nutrient2;...
     * Line 2: #MAX_CALORIES,value
     * Remaining lines: food_name,calories,nutrient1;nutrient2;...
     */
    public static TestCase loadTestCase(File file) throws IOException {
        TestCase testCase = new TestCase(file.getName());
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int lineNum = 0;
        
        while ((line = reader.readLine()) != null) {
            lineNum++;
            line = line.trim();
            
            if (line.isEmpty()) {
                continue;
            }
            
            if (line.startsWith("#UNIVERSE,")) {
                testCase.universe = SetCoverGreedy.parseUniverse(line);
            } else if (line.startsWith("#MAX_CALORIES,")) {
                testCase.maxCalories = SetCoverGreedy.parseMaxCalories(line);
            } else if (!line.startsWith("#")) {
                // Food item line
                try {
                    SetCoverGreedy.FoodItem food = SetCoverGreedy.parseFoodItem(line);
                    testCase.foodItems.add(food);
                } catch (Exception e) {
                    System.err.println("Warning: Could not parse line " + lineNum + " in " + file.getName());
                }
            }
        }
        
        reader.close();
        
        testCase.numFoods = testCase.foodItems.size();
        testCase.numNutrients = testCase.universe.size();
        
        return testCase;
    }

    /**
     * Run the algorithm on a test case and measure execution time.
     * 
     * @param testCase The test case to run
     * @return Runtime in nanoseconds
     */
    public static long runAndMeasure(TestCase testCase) {
        long startTime = System.nanoTime();
        
        SetCoverGreedy.SetCoverResult result = SetCoverGreedy.greedySetCover(
            testCase.universe,
            testCase.foodItems,
            testCase.maxCalories
        );
        
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    /**
     * Run simulation on all test files in the data/ directory.
     */
    public static void main(String[] args) {
        // Create results directory if it doesn't exist
        File resultsDir = new File("results");
        if (!resultsDir.exists()) {
            resultsDir.mkdirs();
        }

        // Find all CSV files in data/ directory
        File dataDir = new File("data");
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            System.err.println("Error: data/ directory not found.");
            System.err.println("Please run the data generation script first:");
            System.err.println("  python3 scripts/generate_setcover_data.py");
            return;
        }

        File[] csvFiles = dataDir.listFiles((dir, name) -> 
            name.startsWith("setcover_") && name.endsWith(".csv"));
        
        if (csvFiles == null || csvFiles.length == 0) {
            System.err.println("Error: No setcover_*.csv files found in data/ directory.");
            return;
        }

        // Sort files for consistent ordering
        Arrays.sort(csvFiles, (a, b) -> a.getName().compareTo(b.getName()));

        System.out.println("=== Greedy Set Cover Simulation ===");
        System.out.println("Found " + csvFiles.length + " test files");
        System.out.println("Running " + NUM_ITERATIONS + " iterations per test case");
        System.out.println();

        // Prepare results output
        List<String[]> results = new ArrayList<>();
        results.add(new String[]{"filename", "num_foods", "num_nutrients", "avg_runtime_ms", 
                                  "sets_selected", "total_calories", "coverage_complete"});

        // Process each test file
        for (File file : csvFiles) {
            System.out.println("Processing: " + file.getName());
            
            try {
                TestCase testCase = loadTestCase(file);
                
                // Run multiple iterations and collect runtimes
                long totalRuntime = 0;
                SetCoverGreedy.SetCoverResult lastResult = null;
                
                for (int i = 0; i < NUM_ITERATIONS; i++) {
                    long runtime = runAndMeasure(testCase);
                    totalRuntime += runtime;
                    
                    // Keep the last result for reporting
                    if (i == NUM_ITERATIONS - 1) {
                        lastResult = SetCoverGreedy.greedySetCover(
                            testCase.universe,
                            testCase.foodItems,
                            testCase.maxCalories
                        );
                    }
                }
                
                // Calculate average runtime in milliseconds
                double avgRuntimeMs = (totalRuntime / (double) NUM_ITERATIONS) / 1_000_000.0;
                
                // Record results
                results.add(new String[]{
                    testCase.filename,
                    String.valueOf(testCase.numFoods),
                    String.valueOf(testCase.numNutrients),
                    String.format("%.4f", avgRuntimeMs),
                    String.valueOf(lastResult.selectedFoods.size()),
                    String.format("%.2f", lastResult.totalCalories),
                    String.valueOf(lastResult.fullCoverage)
                });
                
                System.out.println("  Foods: " + testCase.numFoods + 
                                   ", Nutrients: " + testCase.numNutrients +
                                   ", Avg Runtime: " + String.format("%.4f", avgRuntimeMs) + " ms" +
                                   ", Selected: " + lastResult.selectedFoods.size() +
                                   ", Full Coverage: " + lastResult.fullCoverage);
                
            } catch (IOException e) {
                System.err.println("  Error loading file: " + e.getMessage());
            }
        }

        // Write results to CSV
        String outputFile = "results/setcover_results.csv";
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
            
            for (String[] row : results) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) sb.append(",");
                    sb.append(row[i]);
                }
                writer.println(sb.toString());
            }
            
            writer.close();
            
            System.out.println();
            System.out.println("Results written to: " + outputFile);
            
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }
}
