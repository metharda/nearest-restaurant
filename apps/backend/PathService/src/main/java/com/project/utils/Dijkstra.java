package com.project.utils;

import com.project.datastructures.Graph;
import com.project.datastructures.HashMap;
import com.project.datastructures.Linkedlist;
import com.project.datastructures.Node;
import com.project.entity.Location;

import java.util.PriorityQueue;
import java.util.Comparator;

/**
 * Dijkstra algorithm implementation for finding the shortest path in a graph
 */
public class Dijkstra {
    
    /**
     * Find the shortest path between start and end locations using Dijkstra's algorithm
     * 
     * @param graph The graph containing locations and edges
     * @param start Starting location
     * @param end Destination location
     * @return Linkedlist of Locations representing the shortest path, or null if no path exists
     */
    public static Linkedlist<Location> findPath(Graph graph, Location start, Location end) {
        if (start == null || end == null) {
            System.out.println("Start or end location is null");
            return null;
        }

        // Map to store the distance to each location from start
        HashMap<Location, Double> distances = new HashMap<>();
        
        // Map to store the previous location in the optimal path
        HashMap<Location, Location> previousLocations = new HashMap<>();
        
        // Priority queue to get the location with the smallest distance
        PriorityQueue<LocationWithDistance> queue = new PriorityQueue<>(
            Comparator.comparingDouble(LocationWithDistance::getDistance)
        );
        
        // Initialize distances with infinity except for the start location
        HashMap<Location, Linkedlist<Location>> adjacencyMap = graph.getMap();
        for (HashMap.Entry<Location, Linkedlist<Location>> entry : adjacencyMap.entrySet()) {
            Location location = entry.getKey();
            distances.put(location, Double.MAX_VALUE);
        }
        
        // Distance to start is 0
        distances.put(start, 0.0);
        
        // Add start location to the queue
        queue.add(new LocationWithDistance(start, 0.0));
        
        while (!queue.isEmpty()) {
            LocationWithDistance current = queue.poll();
            Location currentLocation = current.getLocation();
            double currentDistance = current.getDistance();
            
            // Skip if we've found a better path already
            if (currentDistance > distances.get(currentLocation)) {
                continue;
            }
            
            // If we've reached the destination, we can build the path and return
            if (currentLocation.equals(end)) {
                return buildPath(previousLocations, start, end);
            }
            
            // Check all neighboring locations
            Linkedlist<Location> neighbors = adjacencyMap.get(currentLocation);
            if (neighbors != null) {
                Node<Location> neighbor = neighbors.head;
                while (neighbor != null) {
                    Location neighborLocation = neighbor.value;
                    
                    // Calculate distance between current location and neighbor
                    double distance = calculateDistance(currentLocation, neighborLocation);
                    double newDistance = distances.get(currentLocation) + distance;
                    
                    // If we found a better path to the neighbor
                    if (newDistance < distances.get(neighborLocation)) {
                        // Update distance and previous location
                        distances.put(neighborLocation, newDistance);
                        previousLocations.put(neighborLocation, currentLocation);
                        
                        // Add to queue with new distance
                        queue.add(new LocationWithDistance(neighborLocation, newDistance));
                    }
                    
                    neighbor = neighbor.next;
                }
            }
        }
        
        // No path found
        return null;
    }
    
    /**
     * Build the path from start to end using the previousLocations map
     */
    private static Linkedlist<Location> buildPath(HashMap<Location, Location> previousLocations, Location start, Location end) {
        Linkedlist<Location> path = new Linkedlist<>();
        
        // Start from the end location
        Location current = end;
        path.addFirst(current);
        
        // Traverse the path backward
        while (!current.equals(start)) {
            current = previousLocations.get(current);
            if (current == null) {
                // This shouldn't happen if a path exists, but just in case
                return null;
            }
            path.addFirst(current);
        }
        
        return path;
    }
    
    /**
     * Calculate the distance between two locations using the Haversine formula
     */
    private static double calculateDistance(Location loc1, Location loc2) {
        final int R = 6371; // Earth's radius in kilometers
        
        double lat1 = Math.toRadians(loc1.getLatitude());
        double lon1 = Math.toRadians(loc1.getLongitude());
        double lat2 = Math.toRadians(loc2.getLatitude());
        double lon2 = Math.toRadians(loc2.getLongitude());
        
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    /**
     * Helper class to store a location with its distance for the priority queue
     */
    private static class LocationWithDistance {
        private final Location location;
        private final double distance;
        
        public LocationWithDistance(Location location, double distance) {
            this.location = location;
            this.distance = distance;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public double getDistance() {
            return distance;
        }
    }
}
