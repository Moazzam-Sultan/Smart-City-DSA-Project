# Smart City Traffic & Emergency Navigation System 🚦🚑

A robust, console-based Java application designed to simulate real-world urban traffic routing, emergency vehicle dispatching, and dynamic traffic signal management. This project was developed to demonstrate the practical application of core Data Structures and Algorithms (DSA) in solving complex logistical problems.

## 🌟 Overview

Modern cities face significant challenges in managing traffic congestion and ensuring rapid response times for emergency services. This system models a city map—specifically tailored with prominent locations from Lahore (e.g., Liberty Market, Gaddafi Stadium, Johar Town)—and calculates optimal routes dynamically. It features a fully interactive CLI where users can test normal routing, simulate road blockages, and trigger emergency priority overrides.

## 🧠 Data Structures & Algorithms Implemented

This project is built purely on fundamental data structures, completely avoiding external frameworks to emphasize core algorithmic logic:

1. **Graph (Adjacency List)** 🗺️
   - **Use:** Represents the city's road network.
   - **Why:** Urban road networks are sparse. An Adjacency List reduces memory consumption to `O(V + E)` and drastically speeds up neighbor iteration compared to an Adjacency Matrix.

2. **Dijkstra’s Algorithm with Min-Heap (Priority Queue)** ⚡
   - **Use:** Calculates the shortest/fastest path between intersections.
   - **Why:** Guarantees the most optimal route based on travel time. Uses a Min-Heap to achieve `O((V + E) log V)` time complexity.
   - *Features an "Emergency Mode" that bypasses physical blockages and reduces travel time by 50%.*

3. **Custom Stack (Linked-List Based)** 🔙
   - **Use:** Turn-by-turn navigation and backtracking.
   - **Why:** When a driver hits a dead-end or blocked road, the stack allows them to undo moves (`pop`) and backtrack step-by-step in `O(1)` time to recalculate a new route.

4. **Custom Circular Queue** 🚥
   - **Use:** Manages traffic signal rotations at busy 4-way intersections.
   - **Why:** Ensures fair, continuous cycling (North → East → South → West) without memory waste or array shifting. Features an `O(N)` emergency override to force a green light for approaching ambulances.

5. **Dynamic Array (ArrayList)** 🚒
   - **Use:** Managing localized emergency fleets (North Zone, South Zone, etc.).
   - **Why:** Fleet sizes fluctuate dynamically. ArrayLists provide `O(1)` indexed access for rapid dispatching while auto-resizing when new vehicles are acquired or sent for maintenance.

## 🚀 Key Features

- **Interactive Navigation Menu:** Continuously search for routes without restarting the program.
- **Dynamic Road Blocks:** Simulate construction or accidents; the system automatically computes alternative routes.
- **Emergency Dispatch System:** Dispatches nearest fire trucks or ambulances with routing priority.
- **Persistent Logging:** Automatically generates detailed `navigation.log`, `emergency.log`, and `traffic.log` files using Java File I/O.
- **Localized Map:** Nodes are mapped to familiar Lahore intersections for a realistic simulation feel.

## 💻 How to Run

### Prerequisites
- Java Development Kit (JDK) 8 or higher installed.

### Execution
1. Clone the repository:
   ```bash
   git clone [https://github.com/Moazzam-Sultan/Smart-City-DSA-Project.git](https://github.com/Moazzam-Sultan/Smart-City-DSA-Project.git)
