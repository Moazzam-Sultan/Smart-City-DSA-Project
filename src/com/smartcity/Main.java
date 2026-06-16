package com.smartcity;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        printBanner();

        Logger logger                       = new Logger();
        CityGraph city                      = CityBuilder.buildCity();
        Map<String, EmergencyFleet> fleets  = CityBuilder.buildFleets();
        TrafficSignalManager signalManager  = new TrafficSignalManager();
        NavigationService navigation        = new NavigationService(city, logger);

        separator("CITY MAP LOADED (Data Structure 1)");
        city.printGraph();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            separator("DYNAMIC ROUTE FINDER");
            System.out.println("  Enter a valid Node ID (from 1 to 10) based on the map.");
            System.out.println("  Press '0' to exit the program.\n");

            System.out.print("  [?] Enter Source Node ID: ");
            int source = scanner.nextInt();

            if (source == 0) {
                System.out.println("\n  [!] System Shutting Down... Goodbye!");
                break;
            }

            System.out.print("  [?] Enter Destination Node ID: ");
            int dest = scanner.nextInt();

            System.out.print("  [?] Is this an emergency? (1 = Yes, 2 = No): ");
            int emergencyInput = scanner.nextInt();
            boolean isEmergency = (emergencyInput == 1);

            separator("CALCULATING SHORTEST PATH...");

            navigation.startJourney(source, dest, isEmergency, false);

            System.out.println("\n  (Press Enter to search for another route...)");
            scanner.nextLine();
            scanner.nextLine();
        }

        scanner.close();
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("  ==========================================================");
        System.out.println("     Smart City Traffic & Emergency Navigation System       ");
        System.out.println("  ----------------------------------------------------------");
        System.out.println("     Student  : Moazzam Sultan Baig                         ");
        System.out.println("     Section  : Section 2                                   ");
        System.out.println("  ==========================================================");
        System.out.println();
    }

    private static void separator(String title) {
        System.out.println("\n  -------------------------------------------------------------");
        System.out.println("    " + title);
        System.out.println("  -------------------------------------------------------------");
    }
}

class CityBuilder {
    public static CityGraph buildCity() {
        CityGraph graph = new CityGraph();
        System.out.println("\n  Building city map...");

        graph.addIntersection(new Intersection(1,  "City Center",       Intersection.Type.NORMAL));
        graph.addIntersection(new Intersection(2,  "North Gate",        Intersection.Type.HIGHWAY_ENTRY));
        graph.addIntersection(new Intersection(3,  "South Bridge",      Intersection.Type.NORMAL));
        graph.addIntersection(new Intersection(4,  "General Hospital",  Intersection.Type.HOSPITAL));
        graph.addIntersection(new Intersection(5,  "Fire Station HQ",   Intersection.Type.FIRE_STATION));
        graph.addIntersection(new Intersection(6,  "East Market",       Intersection.Type.NORMAL));
        graph.addIntersection(new Intersection(7,  "West University",   Intersection.Type.SCHOOL));
        graph.addIntersection(new Intersection(8,  "Police HQ",         Intersection.Type.POLICE_STATION));
        graph.addIntersection(new Intersection(9,  "Airport Rd",        Intersection.Type.HIGHWAY_ENTRY));
        graph.addIntersection(new Intersection(10, "Residential Zone",  Intersection.Type.NORMAL));

        graph.addBidirectionalRoad(1, 2,  5.0, Road.RoadType.MAIN_ROAD);
        graph.addBidirectionalRoad(1, 3,  4.0, Road.RoadType.MAIN_ROAD);
        graph.addBidirectionalRoad(1, 6,  3.0, Road.RoadType.MAIN_ROAD);
        graph.addBidirectionalRoad(1, 7,  6.0, Road.RoadType.MAIN_ROAD);
        graph.addBidirectionalRoad(2, 4,  3.0, Road.RoadType.MAIN_ROAD);
        graph.addBidirectionalRoad(2, 8,  4.0, Road.RoadType.HIGHWAY);
        graph.addBidirectionalRoad(2, 9,  8.0, Road.RoadType.HIGHWAY);
        graph.addBidirectionalRoad(3, 5,  2.0, Road.RoadType.EMERGENCY_LANE);
        graph.addBidirectionalRoad(3, 10, 3.0, Road.RoadType.SIDE_STREET);
        graph.addBidirectionalRoad(4, 5,  2.5, Road.RoadType.EMERGENCY_LANE);
        graph.addBidirectionalRoad(4, 8,  3.5, Road.RoadType.MAIN_ROAD);
        graph.addBidirectionalRoad(5, 7,  4.0, Road.RoadType.MAIN_ROAD);
        graph.addBidirectionalRoad(6, 9,  5.0, Road.RoadType.HIGHWAY);
        graph.addBidirectionalRoad(6, 10, 2.0, Road.RoadType.SIDE_STREET);
        graph.addBidirectionalRoad(7, 10, 3.5, Road.RoadType.SIDE_STREET);
        graph.addBidirectionalRoad(8, 9,  6.0, Road.RoadType.HIGHWAY);
        graph.addBidirectionalRoad(9, 10, 4.0, Road.RoadType.MAIN_ROAD);

        return graph;
    }

    public static Map<String, EmergencyFleet> buildFleets() {
        Map<String, EmergencyFleet> fleets = new HashMap<>();

        EmergencyFleet northFleet = new EmergencyFleet("NORTH-ZONE");
        northFleet.addVehicle(new EmergencyVehicle("AMB-001", EmergencyVehicle.VehicleType.AMBULANCE, 4));
        northFleet.addVehicle(new EmergencyVehicle("AMB-002", EmergencyVehicle.VehicleType.AMBULANCE, 2));
        northFleet.addVehicle(new EmergencyVehicle("POL-001", EmergencyVehicle.VehicleType.POLICE_CAR, 8));
        northFleet.addVehicle(new EmergencyVehicle("TOW-001", EmergencyVehicle.VehicleType.TOW_TRUCK,  2));
        fleets.put("NORTH", northFleet);

        EmergencyFleet southFleet = new EmergencyFleet("SOUTH-ZONE");
        southFleet.addVehicle(new EmergencyVehicle("FIR-001", EmergencyVehicle.VehicleType.FIRE_TRUCK, 5));
        southFleet.addVehicle(new EmergencyVehicle("FIR-002", EmergencyVehicle.VehicleType.FIRE_TRUCK, 5));
        southFleet.addVehicle(new EmergencyVehicle("RES-001", EmergencyVehicle.VehicleType.RESCUE_UNIT, 3));
        southFleet.addVehicle(new EmergencyVehicle("AMB-003", EmergencyVehicle.VehicleType.AMBULANCE,  3));
        fleets.put("SOUTH", southFleet);

        EmergencyFleet centralFleet = new EmergencyFleet("CENTRAL-ZONE");
        centralFleet.addVehicle(new EmergencyVehicle("POL-002", EmergencyVehicle.VehicleType.POLICE_CAR, 1));
        centralFleet.addVehicle(new EmergencyVehicle("POL-003", EmergencyVehicle.VehicleType.POLICE_CAR, 1));
        centralFleet.addVehicle(new EmergencyVehicle("TOW-002", EmergencyVehicle.VehicleType.TOW_TRUCK,  1));
        fleets.put("CENTRAL", centralFleet);

        return fleets;
    }
}

class Logger {
    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String navLogPath;
    private final String emergencyLogPath;
    private final String trafficLogPath;

    public Logger() {
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
        } catch (IOException e) {
            System.err.println("[LOGGER] Could not create logs directory: " + e.getMessage());
        }

        navLogPath       = LOG_DIR + "/navigation.log";
        emergencyLogPath = LOG_DIR + "/emergency.log";
        trafficLogPath   = LOG_DIR + "/traffic.log";

        writeHeader(navLogPath,       "NAVIGATION LOG");
        writeHeader(emergencyLogPath, "EMERGENCY VEHICLE LOG");
        writeHeader(trafficLogPath,   "TRAFFIC SIGNAL LOG");
    }

    private void writeHeader(String path, String title) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path, false))) {
            pw.println("============================================================");
            pw.println("  Smart City Traffic & Emergency Navigation System");
            pw.println("  " + title);
            pw.println("  Session started: " + LocalDateTime.now().format(FORMATTER));
            pw.println("  Student: Moazzam Sultan Baig | Section 2");
            pw.println("============================================================");
        } catch (IOException e) {
            System.err.println("[LOGGER] Header write failed: " + e.getMessage());
        }
    }

    public void log(String category, String message) {
        String path;
        switch (category.toUpperCase()) {
            case "EMERGENCY": path = emergencyLogPath; break;
            case "TRAFFIC":   path = trafficLogPath;   break;
            default:          path = navLogPath;        break;
        }

        String entry = String.format("[%s] [%s] %s", LocalDateTime.now().format(FORMATTER), category, message);

        try (PrintWriter pw = new PrintWriter(new FileWriter(path, true))) {
            pw.println(entry);
        } catch (IOException e) {
            System.err.println("[LOGGER] Write failed: " + e.getMessage());
        }
        System.out.println("  LOG: " + entry);
    }

    public String getNavLogPath()       { return navLogPath; }
    public String getEmergencyLogPath() { return emergencyLogPath; }
    public String getTrafficLogPath()   { return trafficLogPath; }
}