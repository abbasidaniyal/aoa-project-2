# AOA Project 2 Implementation

In this project, we implement the algorithms presented in our report and run them against synthetic data to simulate and evaluate their performance.

---

## Repository Structure

```
aoa-project-2/
├── SetCoverGreedy.java           # Core greedy set cover algorithm implementation
├── SimulateSetCoverGreedy.java   # Simulation runner for greedy set cover
├── data/                         # Generated test data files
│   └── setcover_f{foods}_n{nutrients}.csv
├── results/                      # Simulation output results
│   └── setcover_results.csv
├── scripts/                      # Data generation scripts
│   └── generate_setcover_data.py
└── README.md
```

### Folders

| Folder | Description |
|--------|-------------|
| `data/` | Contains synthetic test data files in CSV format. Files are named using the convention `setcover_f{num_foods}_n{num_nutrients}.csv` to indicate the problem size. |
| `results/` | Stores simulation output CSV files with runtime measurements and algorithm results. |
| `scripts/` | Python scripts for generating test data with configurable parameters. |

### Data Format

Each data file follows this CSV structure:
- **Line 1**: `#UNIVERSE,nutrient1;nutrient2;...` — The complete set of nutrients to be covered
- **Line 2**: `#MAX_CALORIES,value` — The daily calorie constraint
- **Remaining lines**: `food_name,calories,nutrient1;nutrient2;...` — Food items with their calorie cost and nutrient coverage

Data is generated with varying sizes to benchmark algorithm performance across different scales. The generation uses a fixed random seed for reproducibility.

---

## Technical Setup

### Prerequisites

- **Java JDK 11+** (for compiling and running Java files)
- **Python 3.x** (for data generation scripts)

### Step 1: Generate Test Data

Run the Python script to create synthetic test files in the `data/` directory:

```bash
python3 scripts/generate_setcover_data.py
```

This generates multiple CSV files with varying numbers of food items (10 to 100,000) and nutrients (5 to 1,000).

### Step 2: Compile Java Files

Compile all Java files from the project root:

```bash
javac *.java
```

### Step 3: Run Simulations

Execute the simulation runner:

```bash
java SimulateSetCoverGreedy
```

Results will be written to `results/setcover_results.csv`.

---

## Algorithm 1: Greedy Set Cover with Calorie Constraint

This algorithm solves the nutrient coverage problem using a greedy heuristic. Given a universe of nutrients, a collection of food items (each covering a subset of nutrients with an associated calorie cost), and a daily calorie limit, the algorithm selects foods to maximize nutrient coverage while respecting the calorie constraint.

### Implementation Files

#### `SetCoverGreedy.java`

Contains the core algorithm implementation:

- **`FoodItem` inner class**: Represents a food item with its name, calorie cost, and set of nutrients it provides.
- **`SetCoverResult` inner class**: Encapsulates the algorithm output including selected foods, total calories used, covered nutrients, and whether full coverage was achieved.
- **`greedySetCover()` method**: The main algorithm that iteratively selects the food item with the best ratio of new nutrients covered to calories until all nutrients are covered or no valid candidates remain.
- **CSV parsing utilities**: Helper methods (`parseFoodItem()`, `parseUniverse()`, `parseMaxCalories()`) for reading test data using `String.split()` without external libraries.

#### `SimulateSetCoverGreedy.java`

Handles simulation execution and benchmarking:

- **`TestCase` inner class**: Holds loaded test data including universe, food items, max calories, and size parameters.
- **`loadTestCase()` method**: Reads and parses CSV files from the `data/` directory using `BufferedReader`.
- **`runAndMeasure()` method**: Executes the algorithm and measures runtime using `System.nanoTime()`.
- **Main simulation loop**: Processes all `setcover_*.csv` files, runs each test case 3 times to average runtime, and outputs results to CSV.

The simulation tracks both `num_foods` and `num_nutrients` as size parameters, enabling performance analysis across both dimensions.

---

## Algorithm 2: (To be added)

*Implementation details will be added here.*

---

## Results

After running the simulation, results are stored in `results/setcover_results.csv` with the following columns:

| Column | Description |
|--------|-------------|
| `filename` | Name of the input test file |
| `num_foods` | Number of food items in the test case |
| `num_nutrients` | Size of the nutrient universe |
| `avg_runtime_ms` | Average runtime across 3 iterations (milliseconds) |
| `sets_selected` | Number of food items selected by the algorithm |
| `total_calories` | Total calorie cost of selected foods |
| `coverage_complete` | Whether all nutrients were covered (`true`/`false`) |
