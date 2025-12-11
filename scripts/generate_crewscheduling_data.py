#!/usr/bin/env python3
"""
Generate simulation data for the Crew Scheduling Algorithm.

Creates CSV files with synthetic flight schedules with varying numbers
of airports and flights to test algorithm performance at different scales.

Output format:
    #AIRPORTS,airport1;airport2;...
    departure_airport,arrival_airport,departure_time,arrival_time
    ...

Files are saved to data/crewscheduling_a{num_airports}_f{num_flights}.csv
"""

import os
import random

# Configuration
DATA_DIR = "data"
RANDOM_SEED = 42  # For reproducibility

# Test configurations: (num_airports, num_flights)
# Varying both dimensions for comprehensive benchmarking
TEST_CONFIGS = [
    # Small sizes
    (3, 10),
    (5, 20),
    (5, 50),
    # Medium sizes
    (10, 100),
    (10, 200),
    (15, 500),
    # Large sizes
    (20, 1000),
    (25, 2000)
]

# Flight generation parameters
MIN_FLIGHT_DURATION = 60    # Minimum flight duration in time units
MAX_FLIGHT_DURATION = 600   # Maximum flight duration in time units
TIME_HORIZON = 10000        # Total time span for scheduling


def generate_airports(num_airports):
    """Generate airport codes."""
    airports = []
    
    # Use common 3-letter airport codes
    airport_codes = [
        "JFK", "LAX", "ORD", "DFW", "DEN", "SFO", "SEA", "LAS", "MCO", "EWR",
        "CLT", "PHX", "IAH", "MIA", "BOS", "MSP", "FLL", "DTW", "PHL", "LGA",
        "BWI", "SLC", "SAN", "DCA", "IAD", "MDW", "TPA", "PDX", "STL", "HNL",
        "AUS", "BNA", "OAK", "MSY", "RDU", "SJC", "SMF", "SNA", "PIT", "CVG",
        "CMH", "IND", "MCI", "BDL", "BUF", "OMA", "ONT", "ANC", "ABQ", "TUS"
    ]
    
    # Use predefined codes or generate new ones
    for i in range(num_airports):
        if i < len(airport_codes):
            airports.append(airport_codes[i])
        else:
            # Generate synthetic codes
            airports.append(f"AP{i+1:02d}")
    
    return airports


def generate_flight_schedule(airports, num_flights):
    """
    Generate a random flight schedule.
    
    Strategy: Ensure temporal consistency (arrival before next departure)
    and create a connected network where flights can potentially chain.
    """
    flights = []
    
    for i in range(num_flights):
        # Random departure and arrival airports (no self-loops)
        dep_airport = random.choice(airports)
        arr_airport = random.choice([a for a in airports if a != dep_airport])
        
        # Random departure time
        dep_time = random.randint(0, TIME_HORIZON - MAX_FLIGHT_DURATION)
        
        # Random flight duration
        duration = random.randint(MIN_FLIGHT_DURATION, MAX_FLIGHT_DURATION)
        arr_time = dep_time + duration
        
        flights.append({
            "departure_airport": dep_airport,
            "arrival_airport": arr_airport,
            "departure_time": dep_time,
            "arrival_time": arr_time
        })
    
    # Sort flights by departure time for better readability
    flights.sort(key=lambda f: (f["departure_time"], f["departure_airport"]))
    
    return flights


def ensure_connectivity(airports, flights):
    """
    Ensure the flight network has some basic connectivity.
    Add flights if needed to connect isolated airports.
    """
    # Track which airports have at least one departure
    has_departure = set()
    has_arrival = set()
    
    for flight in flights:
        has_departure.add(flight["departure_airport"])
        has_arrival.add(flight["arrival_airport"])
    
    # Add flights for airports with no departures
    for airport in airports:
        if airport not in has_departure:
            # Add a departure from this airport
            other_airport = random.choice([a for a in airports if a != airport])
            dep_time = random.randint(0, TIME_HORIZON - MAX_FLIGHT_DURATION)
            duration = random.randint(MIN_FLIGHT_DURATION, MAX_FLIGHT_DURATION)
            
            flights.append({
                "departure_airport": airport,
                "arrival_airport": other_airport,
                "departure_time": dep_time,
                "arrival_time": dep_time + duration
            })


def generate_test_file(num_airports, num_flights):
    """Generate a complete test file with the given configuration."""
    # Generate airports
    airports = generate_airports(num_airports)
    
    # Generate flight schedule
    flights = generate_flight_schedule(airports, num_flights)
    
    # Ensure basic connectivity
    ensure_connectivity(airports, flights)
    
    # Create output filename
    filename = f"crewscheduling_a{num_airports}_f{num_flights}.csv"
    filepath = os.path.join(DATA_DIR, filename)
    
    # Write to CSV
    with open(filepath, 'w', newline='') as f:
        # Write airports header
        airports_str = ";".join(airports)
        f.write(f"#AIRPORTS,{airports_str}\n")
        
        # Write flight records
        for flight in flights:
            f.write(f"{flight['departure_airport']},"
                   f"{flight['arrival_airport']},"
                   f"{flight['departure_time']},"
                   f"{flight['arrival_time']}\n")
    
    return filename, num_airports, len(flights)


def main():
    """Main function to generate all test data files."""
    # Set random seed for reproducibility
    random.seed(RANDOM_SEED)
    
    # Create data directory if it doesn't exist
    if not os.path.exists(DATA_DIR):
        os.makedirs(DATA_DIR)
        print(f"Created directory: {DATA_DIR}/")
    
    print("=== Generating Crew Scheduling Test Data ===")
    print(f"Output directory: {DATA_DIR}/")
    print(f"Random seed: {RANDOM_SEED}")
    print()
    
    generated_files = []
    
    for num_airports, num_flights in TEST_CONFIGS:
        filename, na, nf = generate_test_file(num_airports, num_flights)
        generated_files.append((filename, na, nf))
        print(f"Generated: {filename} (airports={na}, flights={nf})")
    
    print()
    print(f"Total files generated: {len(generated_files)}")
    print()
    print("To run the simulation:")
    print("  javac *.java")
    print("  java SimulateNetworkFlow")


if __name__ == "__main__":
    main()
