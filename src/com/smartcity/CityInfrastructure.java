package com.smartcity;

import java.util.ArrayList;
import java.util.List;

class Intersection {
    public enum Type { NORMAL, HOSPITAL, FIRE_STATION, POLICE_STATION, SCHOOL, HIGHWAY_ENTRY }

    private final int id;
    private final String name;
    private final Type type;
    private boolean isBlocked;

    public Intersection(int id, String name, Type type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.isBlocked = false;
    }

    public Intersection(int id, String name) {
        this(id, name, Type.NORMAL);
    }

    public int getId()           { return id; }
    public String getName()      { return name; }
    public Type getType()        { return type; }
    public boolean isBlocked()   { return isBlocked; }
    public void setBlocked(boolean blocked) { this.isBlocked = blocked; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s)%s", id, name, type, isBlocked ? " [BLOCKED]" : "");
    }
}

class Road {
    public enum RoadType { HIGHWAY, MAIN_ROAD, SIDE_STREET, EMERGENCY_LANE }

    private final int fromId;
    private final int toId;
    private double weight;
    private final double baseWeight;
    private final RoadType roadType;
    private boolean isBlocked;
    private double congestionFactor;

    public Road(int fromId, int toId, double weight, RoadType roadType) {
        this.fromId = fromId;
        this.toId = toId;
        this.weight = weight;
        this.baseWeight = weight;
        this.roadType = roadType;
        this.isBlocked = false;
        this.congestionFactor = 1.0;
    }

    public int getFromId()           { return fromId; }
    public int getToId()             { return toId; }
    public double getWeight()        { return isBlocked ? Double.MAX_VALUE : weight * congestionFactor; }
    public double getBaseWeight()    { return baseWeight; }
    public RoadType getRoadType()    { return roadType; }
    public boolean isBlocked()       { return isBlocked; }
    public void setBlocked(boolean b){ this.isBlocked = b; }

    public void setCongestionFactor(double factor) { this.congestionFactor = Math.max(1.0, factor); }
    public double getCongestionFactor() { return congestionFactor; }

    /** EMERGENCY ROUTE FIXED: Emergency vehicles can bypass physical blocks */
    public double getEmergencyWeight() {
        return baseWeight * 0.5;
    }

    @Override
    public String toString() {
        return String.format("Road(%d -> %d | %.1f min | %s | congestion: %.1fx%s)",
                fromId, toId, weight, roadType, congestionFactor, isBlocked ? " [BLOCKED]" : "");
    }
}

class EmergencyVehicle {
    public enum VehicleType { AMBULANCE, FIRE_TRUCK, POLICE_CAR, TOW_TRUCK, RESCUE_UNIT }
    public enum Status { STANDBY, DISPATCHED, RETURNING, MAINTENANCE }

    private final String vehicleId;
    private final VehicleType vehicleType;
    private Status status;
    private final int baseIntersectionId;
    private int currentIntersectionId;
    private String assignedMission;

    public EmergencyVehicle(String vehicleId, VehicleType vehicleType, int baseIntersectionId) {
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
        this.baseIntersectionId = baseIntersectionId;
        this.currentIntersectionId = baseIntersectionId;
        this.status = Status.STANDBY;
        this.assignedMission = "None";
    }

    public String getVehicleId()              { return vehicleId; }
    public VehicleType getVehicleType()       { return vehicleType; }
    public Status getStatus()                 { return status; }
    public int getBaseIntersectionId()        { return baseIntersectionId; }
    public int getCurrentIntersectionId()     { return currentIntersectionId; }
    public String getAssignedMission()        { return assignedMission; }

    public void dispatch(int targetIntersection, String mission) {
        this.status = Status.DISPATCHED;
        this.currentIntersectionId = targetIntersection;
        this.assignedMission = mission;
    }

    public void returnToBase() {
        this.status = Status.RETURNING;
        this.currentIntersectionId = baseIntersectionId;
        this.assignedMission = "None";
    }

    public void setStandby() {
        this.status = Status.STANDBY;
        this.currentIntersectionId = baseIntersectionId;
    }

    public boolean isAvailable() { return status == Status.STANDBY; }

    @Override
    public String toString() {
        return String.format("%-10s | %-12s | %-11s | Base: %-3d | Current: %-3d | Mission: %s",
                vehicleId, vehicleType, status, baseIntersectionId, currentIntersectionId, assignedMission);
    }
}