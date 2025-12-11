import java.util.*;

/**
 * Network Flow Algorithm for Crew Scheduling Problem
 * 
 * Solves the minimum crew scheduling problem by reducing it to a circulation
 * with demands problem over a time-expanded network. Uses Dinitz's algorithm
 * for maximum flow computation.
 */
public class NetworkFlow {

    /**
     * Represents a directed edge in the flow network with residual capacity.
     */
    static class Edge {
        int to;           // Destination node
        int capacity;     // Current residual capacity
        int flow;         // Current flow on this edge
        Edge reverse;     // Pointer to reverse edge

        Edge(int to, int capacity) {
            this.to = to;
            this.capacity = capacity;
            this.flow = 0;
        }
    }

    /**
     * Represents a flight with departure/arrival airports and times.
     */
    public static class Flight {
        public String departureAirport;
        public String arrivalAirport;
        public int departureTime;
        public int arrivalTime;

        public Flight(String depAirport, String arrAirport, int depTime, int arrTime) {
            this.departureAirport = depAirport;
            this.arrivalAirport = arrAirport;
            this.departureTime = depTime;
            this.arrivalTime = arrTime;
        }

        @Override
        public String toString() {
            return departureAirport + "@" + departureTime + " -> " + 
                   arrivalAirport + "@" + arrivalTime;
        }
    }

    /**
     * Result of the crew scheduling algorithm.
     */
    public static class CrewSchedulingResult {
        public Map<String, Integer> initialCrewCount;
        public int totalCrewRequired;
        public boolean feasible;
        public int maxFlowValue;

        public CrewSchedulingResult() {
            this.initialCrewCount = new HashMap<>();
            this.totalCrewRequired = 0;
            this.feasible = false;
            this.maxFlowValue = 0;
        }
    }

    /**
     * Flow network graph using adjacency list representation.
     */
    static class FlowGraph {
        int numNodes;
        List<List<Edge>> adjacency;
        int[] level;  // For BFS level graph in Dinitz

        FlowGraph(int numNodes) {
            this.numNodes = numNodes;
            this.adjacency = new ArrayList<>(numNodes);
            for (int i = 0; i < numNodes; i++) {
                adjacency.add(new ArrayList<>());
            }
            this.level = new int[numNodes];
        }

        /**
         * Add an edge with given capacity. Automatically creates reverse edge.
         */
        void addEdge(int from, int to, int capacity) {
            Edge forward = new Edge(to, capacity);
            Edge backward = new Edge(from, 0);  // Reverse edge starts with 0 capacity
            
            forward.reverse = backward;
            backward.reverse = forward;
            
            adjacency.get(from).add(forward);
            adjacency.get(to).add(backward);
        }

        /**
         * Build level graph using BFS.
         * Returns true if sink is reachable from source.
         */
        boolean buildLevelGraph(int source, int sink) {
            Arrays.fill(level, -1);
            level[source] = 0;
            
            Queue<Integer> queue = new LinkedList<>();
            queue.add(source);
            
            while (!queue.isEmpty()) {
                int u = queue.poll();
                
                for (Edge edge : adjacency.get(u)) {
                    if (level[edge.to] == -1 && edge.capacity > 0) {
                        level[edge.to] = level[u] + 1;
                        queue.add(edge.to);
                    }
                }
            }
            
            return level[sink] != -1;
        }

        /**
         * Send blocking flow using DFS.
         * Returns the amount of flow pushed.
         */
        int sendFlow(int u, int sink, int pushed, int[] start) {
            if (u == sink) {
                return pushed;
            }
            
            // Try all edges from current start position
            for (; start[u] < adjacency.get(u).size(); start[u]++) {
                Edge edge = adjacency.get(u).get(start[u]);
                
                // Only use edges in level graph with positive capacity
                if (level[edge.to] == level[u] + 1 && edge.capacity > 0) {
                    int flow = sendFlow(edge.to, sink, 
                                       Math.min(pushed, edge.capacity), start);
                    
                    if (flow > 0) {
                        // Update residual capacities
                        edge.capacity -= flow;
                        edge.reverse.capacity += flow;
                        edge.flow += flow;
                        edge.reverse.flow -= flow;
                        return flow;
                    }
                }
            }
            
            return 0;
        }

        /**
         * Dinitz's algorithm for maximum flow.
         * Returns the maximum flow value from source to sink.
         */
        int dinitzMaxFlow(int source, int sink) {
            int maxFlow = 0;
            
            // Repeatedly find blocking flows
            while (buildLevelGraph(source, sink)) {
                int[] start = new int[numNodes];  // Track edge iteration position
                
                while (true) {
                    int flow = sendFlow(source, sink, Integer.MAX_VALUE, start);
                    if (flow == 0) {
                        break;
                    }
                    maxFlow += flow;
                }
            }
            
            return maxFlow;
        }
    }

    /**
     * Solve the minimum crew scheduling problem.
     * 
     * Algorithm:
     * 1. Create event nodes for each airport-time pair
     * 2. Add flight edges with lower bound 1 (one crew required)
     * 3. Add injection edges from injection node to first event at each airport
     * 4. Apply lower bound reduction to convert to standard max flow
     * 5. Add super source and super sink based on demands
     * 6. Run Dinitz max flow algorithm
     * 7. Extract initial crew counts from injection edge flows
     * 
     * @param airports Set of airport codes
     * @param flights List of scheduled flights
     * @return CrewSchedulingResult with initial crew counts per airport
     */
    public static CrewSchedulingResult solveCrewScheduling(Set<String> airports, 
                                                           List<Flight> flights) {
        CrewSchedulingResult result = new CrewSchedulingResult();
        
        // Step 1: Create event nodes for each airport
        Map<String, List<Integer>> airportEvents = new HashMap<>();
        for (String airport : airports) {
            airportEvents.put(airport, new ArrayList<>());
        }
        
        // Collect all event times at each airport
        for (Flight flight : flights) {
            airportEvents.get(flight.departureAirport).add(flight.departureTime);
            airportEvents.get(flight.arrivalAirport).add(flight.arrivalTime);
        }
        
        // Sort events and create node mapping
        Map<String, List<Integer>> sortedEvents = new HashMap<>();
        Map<String, Map<Integer, Integer>> eventNodeMap = new HashMap<>();
        
        int nodeId = 0;
        for (String airport : airports) {
            List<Integer> times = airportEvents.get(airport);
            Set<Integer> uniqueTimes = new TreeSet<>(times);
            List<Integer> sortedTimes = new ArrayList<>(uniqueTimes);
            sortedEvents.put(airport, sortedTimes);
            
            Map<Integer, Integer> timeToNode = new HashMap<>();
            for (int time : sortedTimes) {
                timeToNode.put(time, nodeId++);
            }
            eventNodeMap.put(airport, timeToNode);
        }
        
        int numEventNodes = nodeId;
        
        // Create injection nodes (one per airport)
        Map<String, Integer> injectionNodes = new HashMap<>();
        for (String airport : airports) {
            injectionNodes.put(airport, nodeId++);
        }
        
        // Create super source and super sink
        int superSource = nodeId++;
        int superSink = nodeId++;
        int totalNodes = nodeId;
        
        // Initialize flow graph
        FlowGraph graph = new FlowGraph(totalNodes);
        
        // Track node demands (for lower bound reduction on flight edges)
        int[] demand = new int[totalNodes];
        
        // Step 2: Apply lower bound reduction on flight edges
        // Flight edges have lower=1, upper=1, so after reduction capacity=0
        // Instead, we adjust demands: departure node supplies 1, arrival node needs 1
        for (Flight flight : flights) {
            int depNode = eventNodeMap.get(flight.departureAirport).get(flight.departureTime);
            int arrNode = eventNodeMap.get(flight.arrivalAirport).get(flight.arrivalTime);
            
            // Lower bound reduction: remove edge, adjust demands
            demand[depNode] -= 1;  // Must send out 1 unit
            demand[arrNode] += 1;   // Must receive 1 unit
            
            // Don't add the flight edge with capacity 0
        }
        
        // Step 3: Add waiting edges between consecutive events at same airport
        // This allows crews to wait at an airport between flights
        for (String airport : airports) {
            List<Integer> events = sortedEvents.get(airport);
            Map<Integer, Integer> timeToNode = eventNodeMap.get(airport);
            
            for (int i = 0; i < events.size() - 1; i++) {
                int currentNode = timeToNode.get(events.get(i));
                int nextNode = timeToNode.get(events.get(i + 1));
                graph.addEdge(currentNode, nextNode, flights.size());  // Capacity = number of flights
            }
        }
        
        // Step 4: Add injection edges
        // Injection nodes -> first event at each airport
        Map<String, Edge> injectionEdges = new HashMap<>();
        for (String airport : airports) {
            int injNode = injectionNodes.get(airport);
            List<Integer> events = sortedEvents.get(airport);
            
            if (!events.isEmpty()) {
                int firstEventNode = eventNodeMap.get(airport).get(events.get(0));
                
                // Connect injection node to first event
                int edgesBefore = graph.adjacency.get(injNode).size();
                graph.addEdge(injNode, firstEventNode, flights.size());
                
                // Store reference to injection edge
                injectionEdges.put(airport, graph.adjacency.get(injNode).get(edgesBefore));
            }
        }
        
        // Step 5: Add super source and sink connections based on demands
        // Nodes with positive demand connect FROM super source
        // Nodes with negative demand connect TO super sink
        int totalDemand = 0;
        for (int i = 0; i < totalNodes; i++) {
            if (demand[i] > 0) {
                graph.addEdge(superSource, i, demand[i]);
                totalDemand += demand[i];
            } else if (demand[i] < 0) {
                graph.addEdge(i, superSink, -demand[i]);
            }
        }
        
        // Also connect super source to injection nodes (crew enters network here)
        for (String airport : airports) {
            int injNode = injectionNodes.get(airport);
            graph.addEdge(superSource, injNode, flights.size());
        }
        
        // Step 6: Run max flow from super source to super sink
        int maxFlow = graph.dinitzMaxFlow(superSource, superSink);
        
        result.maxFlowValue = maxFlow;
        
        // Step 7: Check feasibility
        // The flow must equal the total demand (sum of all positive demands)
        // This ensures all flight lower bounds are satisfied
        result.feasible = (maxFlow == totalDemand);
        
        // Step 8: Extract initial crew counts from injection edge flows
        if (result.feasible) {
            for (String airport : airports) {
                Edge injectionEdge = injectionEdges.get(airport);
                if (injectionEdge != null) {
                    int crewCount = injectionEdge.flow;
                    result.initialCrewCount.put(airport, crewCount);
                    result.totalCrewRequired += crewCount;
                }
            }
        }
        
        return result;
    }

    /**
     * Parse a flight from CSV line.
     * Expected format: departure_airport,arrival_airport,departure_time,arrival_time
     */
    public static Flight parseFlight(String line) {
        String[] parts = line.split(",");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid flight line: " + line);
        }
        
        String depAirport = parts[0].trim();
        String arrAirport = parts[1].trim();
        int depTime = Integer.parseInt(parts[2].trim());
        int arrTime = Integer.parseInt(parts[3].trim());
        
        return new Flight(depAirport, arrAirport, depTime, arrTime);
    }

    /**
     * Parse airports from header line.
     * Expected format: #AIRPORTS,airport1;airport2;...
     */
    public static Set<String> parseAirports(String line) {
        Set<String> airports = new HashSet<>();
        
        if (!line.startsWith("#AIRPORTS,")) {
            throw new IllegalArgumentException("Invalid airports line: " + line);
        }
        
        String airportsPart = line.substring("#AIRPORTS,".length());
        String[] airportArray = airportsPart.split(";");
        for (String airport : airportArray) {
            String trimmed = airport.trim();
            if (!trimmed.isEmpty()) {
                airports.add(trimmed);
            }
        }
        
        return airports;
    }

    /**
     * Main method for testing the algorithm with a simple example.
     */
    public static void main(String[] args) {
        // Example: Simple test case with 3 airports and 4 flights
        Set<String> airports = new HashSet<>();
        airports.add("JFK");
        airports.add("LAX");
        airports.add("ORD");

        List<Flight> flights = new ArrayList<>();
        flights.add(new Flight("JFK", "LAX", 100, 400));  // JFK->LAX at time 100-400
        flights.add(new Flight("LAX", "ORD", 500, 700));  // LAX->ORD at time 500-700
        flights.add(new Flight("JFK", "ORD", 200, 500));  // JFK->ORD at time 200-500
        flights.add(new Flight("ORD", "JFK", 800, 1000)); // ORD->JFK at time 800-1000

        System.out.println("=== Crew Scheduling Algorithm ===");
        System.out.println("Airports: " + airports);
        System.out.println("Flights: " + flights.size());
        System.out.println();

        CrewSchedulingResult result = solveCrewScheduling(airports, flights);

        System.out.println("Feasible: " + result.feasible);
        System.out.println("Max Flow: " + result.maxFlowValue);
        System.out.println();
        
        if (result.feasible) {
            System.out.println("Minimum Initial Crew Required:");
            for (String airport : airports) {
                int crew = result.initialCrewCount.getOrDefault(airport, 0);
                System.out.println("  " + airport + ": " + crew);
            }
            System.out.println();
            System.out.println("Total Crew Required: " + result.totalCrewRequired);
        } else {
            System.out.println("No feasible solution found!");
        }
    }
}
