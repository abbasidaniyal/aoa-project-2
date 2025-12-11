# AOA Project 2 Implementation

In this project, we implement the algorithms presented in our report and run them against synthetic data to simulate and evaluate their performance.

---

## Repository Structure

```
aoa-project-2/
├── SetCoverGreedy.java                  # Core greedy set cover algorithm
├── SimulateSetCoverGreedy.java          # Simulation runner for greedy set cover
├── NetworkFlow.java                     # Core network flow crew scheduling algorithm
├── SimulateNetworkFlow.java             # Simulation runner for crew scheduling
├── data/                                # Generated test data files
│   ├── setcover_f{foods}_n{nutrients}.csv
│   └── crewscheduling_a{airports}_f{flights}.csv
├── results/                             # Simulation output results
│   ├── setcover_results.csv
│   └── crewscheduling_results.csv
├── scripts/                             # Data generation scripts
│   ├── generate_setcover_data.py
│   └── generate_crewscheduling_data.py
└── README.md
```

### Folders

| Folder | Description |
|--------|-------------|
| `data/` | Contains synthetic test data files in CSV format. Files are named using conventions like `setcover_f{num_foods}_n{num_nutrients}.csv` and `crewscheduling_a{num_airports}_f{num_flights}.csv` to indicate problem size. |
| `results/` | Stores simulation output CSV files with runtime measurements and algorithm results. |
| `scripts/` | Python scripts for generating test data with configurable parameters. |

### Data Formats

#### Set Cover Data (`setcover_*.csv`)
- **Line 1**: `#UNIVERSE,nutrient1;nutrient2;...` — The complete set of nutrients to be covered
- **Line 2**: `#MAX_CALORIES,value` — The daily calorie constraint
- **Remaining lines**: `food_name,calories,nutrient1;nutrient2;...` — Food items with their calorie cost and nutrient coverage

#### Crew Scheduling Data (`crewscheduling_*.csv`)
- **Line 1**: `#AIRPORTS,airport1;airport2;...` — List of all airports in the network
- **Remaining lines**: `departure_airport,arrival_airport,departure_time,arrival_time` — Flight schedules

Data is generated with varying sizes to benchmark algorithm performance across different scales. The generation uses a fixed random seed for reproducibility.

---

## Technical Setup

### Prerequisites

- **Java JDK 11+** (for compiling and running Java files)
- **Python 3.x** (for data generation scripts)

### Step 1: Generate Test Data

Run the Python scripts to create synthetic test files in the `data/` directory:

```bash
# Generate set cover test data
python3 scripts/generate_setcover_data.py

# Generate crew scheduling test data
python3 scripts/generate_crewscheduling_data.py
```

The set cover script generates files with varying numbers of food items (10 to 100,000) and nutrients (5 to 1,000). The crew scheduling script generates files with varying numbers of airports (3 to 50) and flights (10 to 10,000).

### Step 2: Compile Java Files

Compile all Java files from the project root:

```bash
javac *.java
```

### Step 3: Run Simulations

Execute the simulation runners for both algorithms:

```bash
# Run set cover simulation
java SimulateSetCoverGreedy

# Run crew scheduling simulation
java SimulateNetworkFlow
```

Results will be written to `results/setcover_results.csv` and `results/crewscheduling_results.csv` respectively.

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

## Algorithm 2: Network Flow for Crew Scheduling

This algorithm determines the minimum number of crew members that must be initially stationed at each airport to staff all scheduled flights. The problem is reduced to a circulation with demands over a time-expanded network, then solved using Dinitz's maximum flow algorithm.

### Implementation Files

#### `NetworkFlow.java`

Contains the core algorithm implementation:

- **`Edge` inner class**: Represents a directed edge in the flow network with residual capacity and flow tracking. Includes a pointer to the reverse edge for efficient residual graph operations.
- **`Flight` inner class**: Represents a flight with departure/arrival airports and times.
- **`CrewSchedulingResult` inner class**: Encapsulates the solution including minimum initial crew count per airport, total crew required, feasibility status, and max flow value.
- **`FlowGraph` inner class**: Implements the flow network using adjacency list representation with methods for building level graphs (BFS) and sending blocking flows (DFS).
- **`dinitzMaxFlow()` method**: Dinitz's algorithm implementation that iteratively builds level graphs and sends blocking flows until no augmenting paths remain.
- **`solveCrewScheduling()` method**: Main algorithm that constructs the time-expanded network, applies lower bound reduction, adds super source/sink, runs max flow, and extracts initial crew counts from injection edge flows.
- **CSV parsing utilities**: Helper methods (`parseFlight()`, `parseAirports()`) for reading test data using `String.split()` without external libraries.

Key algorithmic steps:
1. Create event nodes for each (airport, time) pair from flight schedule
2. Add flight edges with unit lower bound and capacity (representing crew requirement)
3. Add injection edges from injection nodes to first event at each airport
4. Apply lower bound reduction: transform edge constraints to node demands
5. Add super source/sink to satisfy node demands
6. Run Dinitz max flow to check feasibility
7. Extract initial crew counts from injection edge flows

#### `SimulateNetworkFlow.java`

Handles simulation execution and benchmarking:

- **`TestCase` inner class**: Holds loaded test data including airports, flights, and size parameters.
- **`loadTestCase()` method**: Reads and parses CSV files from the `data/` directory using `BufferedReader`.
- **`runAndMeasure()` method**: Executes the algorithm and measures runtime using `System.nanoTime()`.
- **Main simulation loop**: Processes all `crewscheduling_*.csv` files, runs each test case 3 times to average runtime, and outputs results to CSV.

The simulation tracks both `num_airports` and `num_flights` as size parameters, enabling performance analysis across both dimensions.

---

## Results

### Set Cover Results

After running the set cover simulation, results are stored in `results/setcover_results.csv` with the following columns:

| Column | Description |
|--------|-------------|
| `filename` | Name of the input test file |
| `num_foods` | Number of food items in the test case |
| `num_nutrients` | Size of the nutrient universe |
| `avg_runtime_ms` | Average runtime across 3 iterations (milliseconds) |
| `sets_selected` | Number of food items selected by the algorithm |
| `total_calories` | Total calorie cost of selected foods |
| `coverage_complete` | Whether all nutrients were covered (`true`/`false`) |

### Crew Scheduling Results

After running the crew scheduling simulation, results are stored in `results/crewscheduling_results.csv` with the following columns:

| Column | Description |
|--------|-------------|
| `filename` | Name of the input test file |
| `num_airports` | Number of airports in the network |
| `num_flights` | Number of scheduled flights |
| `avg_runtime_ms` | Average runtime across 3 iterations (milliseconds) |
| `total_crew_required` | Minimum total crew members needed across all airports |
| `max_flow_value` | Maximum flow value computed by Dinitz algorithm |
| `feasible` | Whether a feasible crew assignment exists (`true`/`false`) |
