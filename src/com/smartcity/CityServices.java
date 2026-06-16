package com.smartcity;

import java.util.*;
import java.util.stream.Collectors;

// ─── DATA STRUCTURE 1: CITY GRAPH ───
class CityGraph {
    private final Map<Integer, Intersection> intersections = new HashMap<>();
    private final Map<Integer, List<Road>> adjacencyList = new HashMap<>();
    private int totalRoads = 0;

    public void addIntersection(Intersection intersection) {
        if (intersections.containsKey(intersection.getId())) return;
        intersections.put(intersection.getId(), intersection);
        adjacencyList.put(intersection.getId(), new ArrayList<>());
        System.out.println("  [GRAPH] Added intersection: " + intersection);
    }

    /** FIXED: State sync fix when removing intersection */
    public boolean removeIntersection(int id) {
        if (!intersections.containsKey(id)) return false;
        totalRoads -= adjacencyList.get(id).size();
        intersections.remove(id);
        adjacencyList.remove(id);
        for (List<Road> roads : adjacencyList.values()) {
            int before = roads.size();
            roads.removeIf(r -> r.getToId() == id);
            totalRoads -= (before - roads.size());
        }
        System.out.println("  [GRAPH] Removed intersection ID: " + id);
        return true;
    }

    public void addRoad(Road road) {
        if (!intersections.containsKey(road.getFromId()) || !intersections.containsKey(road.getToId())) return;
        adjacencyList.get(road.getFromId()).add(road);
        totalRoads++;
    }

    public void addBidirectionalRoad(int from, int to, double weight, Road.RoadType type) {
        addRoad(new Road(from, to, weight, type));
        addRoad(new Road(to, from, weight, type));
    }

    public boolean blockRoad(int from, int to) {
        for (Road road : getNeighbors(from)) {
            if (road.getToId() == to) {
                road.setBlocked(true);
                System.out.println("  [GRAPH] Road BLOCKED: " + from + " -> " + to);
                return true;
            }
        }
        return false;
    }

    public boolean unblockRoad(int from, int to) {
        for (Road road : getNeighbors(from)) {
            if (road.getToId() == to) {
                road.setBlocked(false);
                System.out.println("  [GRAPH] Road UNBLOCKED: " + from + " -> " + to);
                return true;
            }
        }
        return false;
    }

    public Intersection getIntersection(int id) { return intersections.get(id); }
    public List<Road> getNeighbors(int intersectionId) { return adjacencyList.getOrDefault(intersectionId, new ArrayList<>()); }
    public Map<Integer, Intersection> getAllIntersections() { return intersections; }
    public boolean hasIntersection(int id) { return intersections.containsKey(id); }
    public int getTotalIntersections() { return intersections.size(); }
    public int getTotalRoads()         { return totalRoads; }

    public void printGraph() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║         CITY GRAPH - ADJACENCY LIST               ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.printf("  Total Intersections : %d%n", getTotalIntersections());
        System.out.printf("  Total Roads         : %d%n", getTotalRoads());
        System.out.println("  ─────────────────────────────────────────────────");

        for (Map.Entry<Integer, Intersection> entry : intersections.entrySet()) {
            int id = entry.getKey();
            Intersection node = entry.getValue();
            System.out.printf("  %s%n", node);
            List<Road> roads = adjacencyList.get(id);
            if (roads.isEmpty()) {
                System.out.println("      └── (no outgoing roads)");
            } else {
                for (int i = 0; i < roads.size(); i++) {
                    String prefix = (i == roads.size() - 1) ? "      └──" : "      ├──";
                    Road r = roads.get(i);
                    Intersection dest = intersections.get(r.getToId());
                    System.out.printf("  %s --> %s (%.1f min, %s)%s%n",
                            prefix, (dest != null ? dest.getName() : "Unknown"), r.getWeight(), r.getRoadType(),
                            r.isBlocked() ? " [BLOCKED]" : "");
                }
            }
        }
    }
}

// ─── DATA STRUCTURE 2 & ALGORITHM: DIJKSTRA WITH MIN-HEAP ───
class DijkstraAlgorithm {
    private static class PQEntry implements Comparable<PQEntry> {
        int intersectionId;
        double distance;
        PQEntry(int id, double dist) { this.intersectionId = id; this.distance = dist; }
        @Override public int compareTo(PQEntry other) { return Double.compare(this.distance, other.distance); }
    }

    public static class ShortestPathResult {
        public final List<Integer> path;
        public final double totalDistance;
        public final boolean isEmergencyRoute;
        public final boolean pathFound;
        public final Map<Integer, Double> distanceMap;

        public ShortestPathResult(List<Integer> path, double totalDistance, boolean isEmergency, Map<Integer, Double> distMap) {
            this.path = path;
            this.totalDistance = totalDistance;
            this.isEmergencyRoute = isEmergency;
            this.pathFound = !path.isEmpty();
            this.distanceMap = distMap;
        }
    }

    public static ShortestPathResult findShortestPath(CityGraph graph, int sourceId, int destId, boolean emergency) {
        if (!graph.hasIntersection(sourceId) || !graph.hasIntersection(destId)) {
            return new ShortestPathResult(Collections.emptyList(), Double.MAX_VALUE, emergency, new HashMap<>());
        }

        Map<Integer, Double> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();

        for (int id : graph.getAllIntersections().keySet()) {
            dist.put(id, Double.MAX_VALUE);
            prev.put(id, -1);
        }
        dist.put(sourceId, 0.0);

        PriorityQueue<PQEntry> pq = new PriorityQueue<>();
        pq.offer(new PQEntry(sourceId, 0.0));
        Set<Integer> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            PQEntry current = pq.poll();
            int u = current.intersectionId;
            if (visited.contains(u)) continue;
            visited.add(u);

            if (u == destId) break;

            for (Road road : graph.getNeighbors(u)) {
                int v = road.getToId();
                if (visited.contains(v)) continue;

                Intersection dest = graph.getIntersection(v);
                if (dest != null && dest.isBlocked() && !emergency) continue;

                double edgeWeight = emergency ? road.getEmergencyWeight() : road.getWeight();
                if (edgeWeight == Double.MAX_VALUE) continue;

                double newDist = dist.get(u) + edgeWeight;
                if (newDist < dist.get(v)) {
                    dist.put(v, newDist);
                    prev.put(v, u);
                    pq.offer(new PQEntry(v, newDist));
                }
            }
        }

        List<Integer> path = reconstructPath(prev, sourceId, destId);
        double totalDist = dist.getOrDefault(destId, Double.MAX_VALUE);
        return new ShortestPathResult(path, totalDist, emergency, dist);
    }

    private static List<Integer> reconstructPath(Map<Integer, Integer> prev, int src, int dest) {
        LinkedList<Integer> path = new LinkedList<>();
        int current = dest;
        while (current != -1) {
            path.addFirst(current);
            if (current == src) break;
            current = prev.getOrDefault(current, -1);
            if (current == -1 && path.getFirst() != src) return Collections.emptyList();
        }
        if (path.isEmpty() || path.getFirst() != src) return Collections.emptyList();
        return path;
    }

    public static void printResult(ShortestPathResult result, CityGraph graph) {
        System.out.println("\n  ╔═══════════════════════════════════════════════════╗");
        System.out.println("  ║           SHORTEST PATH RESULT                    ║");
        System.out.println("  ║═══════════════════════════════════════════════════║");
        if (!result.pathFound) {
            System.out.println("  ✗ No path available (all routes blocked).");
            return;
        }
        System.out.println("  Mode    : " + (result.isEmergencyRoute ? "🚨 EMERGENCY" : "🚗 NORMAL"));
        System.out.printf("  Time    : %.2f minutes%n", result.totalDistance);
        System.out.print("  Route   : ");
        for (int i = 0; i < result.path.size(); i++) {
            Intersection node = graph.getIntersection(result.path.get(i));
            System.out.print(node != null ? node.getName() : "ID-" + result.path.get(i));
            if (i < result.path.size() - 1) System.out.print(" ──► ");
        }
        System.out.println("\n");
    }
}

// ─── DATA STRUCTURE 3: CUSTOM NAVIGATION STACK ───
class NavigationStack<T> {
    private static class Node<T> {
        T data;
        Node<T> next;
        Node(T data) { this.data = data; }
    }

    private Node<T> top = null;
    private int size = 0;
    private final String stackName;

    public NavigationStack(String name) { this.stackName = name; }

    public void push(T item) {
        Node<T> newNode = new Node<>(item);
        newNode.next = top;
        top = newNode;
        size++;
    }

    public T pop() {
        if (isEmpty()) throw new EmptyStackException();
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }

    public T peek() {
        if (isEmpty()) throw new EmptyStackException();
        return top.data;
    }

    public boolean isEmpty() { return top == null; }
    public int getSize()     { return size; }

    public void clear() {
        top = null;
        size = 0;
    }

    /** FIXED: Optimized to O(N) instead of O(N^2) shifting */
    public void printPath() {
        if (isEmpty()) return;
        List<T> items = new ArrayList<>();
        Node<T> curr = top;
        while (curr != null) {
            items.add(curr.data);
            curr = curr.next;
        }
        Collections.reverse(items);
        StringBuilder sb = new StringBuilder("  ");
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i));
            if (i < items.size() - 1) sb.append(" → ");
        }
        sb.append("  ◄ (current)");
        System.out.println(sb);
    }
}

// ─── DATA STRUCTURE 4: CUSTOM CIRCULAR QUEUE ───
class CircularQueue<T> {
    private final Object[] data;
    private int front = 0;
    private int rear = -1;
    private int size = 0;
    private final int capacity;
    private final String queueName;

    public CircularQueue(int capacity, String name) {
        this.capacity = capacity;
        this.data = new Object[capacity];
        this.queueName = name;
    }

    public boolean enqueue(T item) {
        if (isFull()) return false;
        rear = (rear + 1) % capacity;
        data[rear] = item;
        size++;
        return true;
    }

    public T dequeue() {
        if (isEmpty()) return null;
        T item = (T) data[front];
        data[front] = null;
        front = (front + 1) % capacity;
        size--;
        return item;
    }

    public T rotate() {
        if (isEmpty()) return null;
        T item = (T) data[front];
        front = (front + 1) % capacity;
        rear  = (rear  + 1) % capacity;
        data[rear] = item;
        return item;
    }

    public T peek() { return isEmpty() ? null : (T) data[front]; }
    public boolean isEmpty()  { return size == 0; }
    public boolean isFull()   { return size == capacity; }
    public int getSize()      { return size; }

    public void printQueue() {
        if (isEmpty()) return;
        for (int i = 0; i < size; i++) {
            System.out.print(data[(front + i) % capacity]);
            if (i == 0) System.out.print(" ◄GREEN");
            if (i < size - 1) System.out.print(" | ");
        }
        System.out.println();
    }
}

// ─── DATA STRUCTURE 5: DYNAMIC EMERGENCY FLEET ───
class EmergencyFleet {
    private final ArrayList<EmergencyVehicle> vehicles = new ArrayList<>();
    private final String zoneName;

    public EmergencyFleet(String zoneName) { this.zoneName = zoneName; }

    public void addVehicle(EmergencyVehicle vehicle) { vehicles.add(vehicle); }

    public EmergencyVehicle dispatch(EmergencyVehicle.VehicleType type, int targetId, String mission) {
        for (EmergencyVehicle v : vehicles) {
            if (v.getVehicleType() == type && v.isAvailable()) {
                v.dispatch(targetId, mission);
                System.out.printf("  [FLEET-%s] DISPATCHED: %s → Intersection %d | Mission: %s%n", zoneName, v.getVehicleId(), targetId, mission);
                return v;
            }
        }
        return null;
    }

    public int getTotalVehicles()  { return vehicles.size(); }
    public int getAvailableCount() { return (int) vehicles.stream().filter(EmergencyVehicle::isAvailable).count(); }

    public void printFleet() {
        System.out.println("\n  ┌─────────────────────────────────────────────────────────────────────────────┐");
        System.out.printf ("  │  EMERGENCY FLEET — Zone: %-50s│%n", zoneName);
        System.out.println("  ├────────────┬──────────────┬─────────────┬────────┬─────────┬────────────────┤");
        for (EmergencyVehicle v : vehicles) {
            System.out.printf("  │ %-10s │ %-12s │ %-11s │ %-6d │ %-7d │ %-14s │%n",
                    v.getVehicleId(), v.getVehicleType(), v.getStatus(), v.getBaseIntersectionId(), v.getCurrentIntersectionId(),
                    v.getAssignedMission().length() > 14 ? v.getAssignedMission().substring(0, 11) + "..." : v.getAssignedMission());
        }
        System.out.println("  └────────────┴──────────────┴─────────────┴────────┴─────────┴────────────────┘");
    }
}

// ─── SERVICES ───
class NavigationService {
    private final CityGraph graph;
    private final NavigationStack<Integer> pathStack = new NavigationStack<>("NAV");
    private final Logger logger;
    private int currentIntersectionId = -1;
    private List<Integer> plannedRoute = null;
    private int routeIndex = 0;
    private boolean isEmergencyMode = false;

    public NavigationService(CityGraph graph, Logger logger) { this.graph = graph; this.logger = logger; }

    /** FIXED: Added keepHistory flag to preserve history during reRoute stack pops */
    public boolean startJourney(int sourceId, int destId, boolean emergencyMode, boolean keepHistory) {
        this.isEmergencyMode = emergencyMode;
        Intersection src  = graph.getIntersection(sourceId);
        Intersection dest = graph.getIntersection(destId);
        if (src == null || dest == null) return false;

        DijkstraAlgorithm.ShortestPathResult result = DijkstraAlgorithm.findShortestPath(graph, sourceId, destId, emergencyMode);
        DijkstraAlgorithm.printResult(result, graph);
        if (!result.pathFound) return false;

        this.plannedRoute = result.path;
        this.routeIndex   = 0;
        if (!keepHistory) {
            pathStack.clear();
            pathStack.push(sourceId);
        }
        this.currentIntersectionId = sourceId;
        return true;
    }

    public boolean moveNext() {
        if (plannedRoute == null || routeIndex >= plannedRoute.size() - 1) return false;
        routeIndex++;
        int nextId = plannedRoute.get(routeIndex);
        pathStack.push(nextId);
        currentIntersectionId = nextId;
        return true;
    }

    public int backtrack() {
        if (pathStack.getSize() <= 1) return currentIntersectionId;
        int popped = pathStack.pop();
        int previous = pathStack.peek();
        currentIntersectionId = previous;
        if (routeIndex > 0) routeIndex--;
        return previous;
    }

    public boolean reRoute(int destId) {
        return startJourney(currentIntersectionId, destId, isEmergencyMode, true);
    }

    public void printStatus() {
        System.out.println("\n  ── Navigation Status ──────────────────────────");
        pathStack.printPath();
    }
}

class TrafficSignalManager {
    public static class Lane {
        private final String direction;
        private int waitingVehicles = 0;
        public Lane(String intersectionName, String direction) { this.direction = direction; }
        public void addVehicle() { waitingVehicles++; }
        @Override public String toString() { return direction + "[vehicles=" + waitingVehicles + "]"; }
    }

    private final Map<Integer, CircularQueue<Lane>> signalQueues = new HashMap<>();

    public void setupIntersectionSignals(int intersectionId, String intersectionName) {
        CircularQueue<Lane> queue = new CircularQueue<>(4, intersectionName);
        String[] directions = {"NORTH", "EAST", "SOUTH", "WEST"};
        for (String dir : directions) queue.enqueue(new Lane(intersectionName, dir));
        signalQueues.put(intersectionId, queue);
    }

    public void runSignalCycles(int intersectionId, int cycles) {
        CircularQueue<Lane> queue = signalQueues.get(intersectionId);
        if (queue == null) return;
        int totalRotations = cycles * queue.getSize();
        for (int i = 0; i < totalRotations; i++) {
            Lane green = queue.peek();
            System.out.printf("  🟢 GREEN: %-15s (for 30 seconds)%n", green);
            queue.rotate();
        }
    }

    /** FIXED: addVehicleToLane completes rotation cycle to prevent front corruption */
    public void addVehicleToLane(int intersectionId, String direction) {
        CircularQueue<Lane> queue = signalQueues.get(intersectionId);
        if (queue == null) return;
        int size = queue.getSize();
        boolean found = false;
        for (int i = 0; i < size; i++) {
            Lane l = queue.peek();
            if (!found && l.direction.equalsIgnoreCase(direction)) {
                l.addVehicle();
                found = true;
            }
            queue.rotate();
        }
    }

    public void emergencyOverride(int intersectionId, String forceDirection) {
        CircularQueue<Lane> queue = signalQueues.get(intersectionId);
        if (queue == null) return;
        int maxTries = queue.getSize();
        for (int i = 0; i < maxTries; i++) {
            Lane current = queue.peek();
            if (current != null && current.direction.equalsIgnoreCase(forceDirection)) {
                System.out.println("  ✔ Emergency lane GREEN: " + current);
                return;
            }
            queue.rotate();
        }
    }
}