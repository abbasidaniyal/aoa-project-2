import java.io.*;
import java.util.*;

/**
 * Simulation runner for the Network Flow Crew Scheduling Algorithm.
 * 
 * Reads test data from the data/ directory, runs the algorithm multiple times
 * to get average runtime, and outputs results to results/crewscheduling_results.csv.
 */
public class SimulateNetworkFlow {

    // Number of iterations for averaging runtime
    private static final int NUM_ITERATIONS = 3;

    /**
     * Represents a test case loaded from a CSV file.
     */
    static class TestCase {
        String filename;
        Set<String> airports;
        List<NetworkFlow.Flight> flights;
        int numAirports;
        int numFlights;

        TestCase(String filename) {
            this.filename = filename;
            this.airports = new HashSet<>();
            this.flights = new ArrayList<>();
        }
    }

    /**
     * Load a test case from a CSV file.
     * 
     * Expected format:
     * Line 1: #AIRPORTS,airport1;airport2;...
     * Remaining lines: departure_airport,arrival_airport,departure_time,arrival_time
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
            
            if (line.startsWith("#AIRPORTS,")) {
                testCase.airports = NetworkFlow.parseAirports(line);
            } else if (!line.startsWith("#")) {
                // Flight line
                try {
                    NetworkFlow.Flight flight = NetworkFlow.parseFlight(line);
                    testCase.flights.add(flight);
                } catch (Exception e) {
                    System.err.println("Warning: Could not parse line " + lineNum + " in " + file.getName());
                }
            }
        }
        
        reader.close();
        
        testCase.numAirports = testCase.airports.size();
        testCase.numFlights = testCase.flights.size();
        
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
        
        NetworkFlow.CrewSchedulingResult result = NetworkFlow.solveCrewScheduling(
            testCase.airports,
            testCase.flights
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
            System.err.println("  python3 scripts/generate_crewscheduling_data.py");
            return;
        }

        File[] csvFiles = dataDir.listFiles((dir, name) -> 
            name.startsWith("crewscheduling_") && name.endsWith(".csv"));
        
        if (csvFiles == null || csvFiles.length == 0) {
            System.err.println("Error: No crewscheduling_*.csv files found in data/ directory.");
            return;
        }

        // Sort files for consistent ordering
        Arrays.sort(csvFiles, (a, b) -> a.getName().compareTo(b.getName()));

        System.out.println("=== Network Flow Crew Scheduling Simulation ===");
        System.out.println("Found " + csvFiles.length + " test files");
        System.out.println("Running " + NUM_ITERATIONS + " iterations per test case");
        System.out.println();

        // Prepare results output
        List<String[]> results = new ArrayList<>();
        results.add(new String[]{"filename", "num_airports", "num_flights", "avg_runtime_ms", 
                                  "total_crew_required", "max_flow_value", "feasible"});

        // Process each test file
        for (File file : csvFiles) {
            System.out.println("Processing: " + file.getName());
            
            try {
                TestCase testCase = loadTestCase(file);
                
                // Run multiple iterations and collect runtimes
                long totalRuntime = 0;
                NetworkFlow.CrewSchedulingResult lastResult = null;
                
                for (int i = 0; i < NUM_ITERATIONS; i++) {
                    long runtime = runAndMeasure(testCase);
                    totalRuntime += runtime;
                    
                    // Keep the last result for reporting
                    if (i == NUM_ITERATIONS - 1) {
                        lastResult = NetworkFlow.solveCrewScheduling(
                            testCase.airports,
                            testCase.flights
                        );
                    }
                }
                
                // Calculate average runtime in milliseconds
                double avgRuntimeMs = (totalRuntime / (double) NUM_ITERATIONS) / 1_000_000.0;
                
                // Record results
                results.add(new String[]{
                    testCase.filename,
                    String.valueOf(testCase.numAirports),
                    String.valueOf(testCase.numFlights),
                    String.format("%.4f", avgRuntimeMs),
                    String.valueOf(lastResult.totalCrewRequired),
                    String.valueOf(lastResult.maxFlowValue),
                    String.valueOf(lastResult.feasible)
                });
                
                System.out.println("  Airports: " + testCase.numAirports + 
                                   ", Flights: " + testCase.numFlights +
                                   ", Avg Runtime: " + String.format("%.4f", avgRuntimeMs) + " ms" +
                                   ", Total Crew: " + lastResult.totalCrewRequired +
                                   ", Feasible: " + lastResult.feasible);
                
            } catch (IOException e) {
                System.err.println("  Error loading file: " + e.getMessage());
            }
        }

        // Write results to CSV
        String outputFile = "results/crewscheduling_results.csv";
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
