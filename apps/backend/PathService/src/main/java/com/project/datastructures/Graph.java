package com.project.datastructures;

import java.util.Objects;

import com.project.entity.Location;
import com.project.entity.Way;
import com.project.utils.Overpass;
import lombok.Data;

@Data
public class Graph {
    private final HashMap<Location, Linkedlist<Location>> map = new HashMap<>();
    private Linkedlist<Location> junctions;
    private Linkedlist<Way> ways;
    private Linkedlist<Location> restaurants;

    private static Graph instance = null;

    private Graph(String radius, String lat, String lon) {
        try {
            this.junctions = Overpass.fetchJunctions(radius, lat, lon);
            this.ways = Overpass.fetchWays(radius, lat, lon);
            this.restaurants = Overpass.fetchRestaurants(radius, lat, lon);
            buildGraph();
        }
        catch (Exception e) {
            this.junctions = null;
            this.ways = null;
            this.restaurants = null;
            System.out.println(e);
        }
    }

    public static synchronized Graph getInstance(String radius, String lat, String lon) {
        if (instance == null) {
            instance = new Graph(radius, lat, lon);
        }
        return instance;
    }

    public static synchronized Graph getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Graph instance is not initialized. Call getInstance(radius, lat, lon) first.");
        }
        return instance;
    }

    public void addEdge(Location loc1, Location loc2) {
        if (!map.containsKey(loc1)) {
            map.put(loc1, new Linkedlist<>());
        }
        if (map.get(loc1).count(loc2) < 1) {
            map.get(loc1).add(loc2);
        }

        if (!map.containsKey(loc2)) {
            map.put(loc2, new Linkedlist<>());
        }
        if (map.get(loc2).count(loc1) < 1) {
            map.get(loc2).add(loc1);
        }
    }

    private void buildGraph() {
        if (this.junctions == null || this.ways == null) {
            System.out.println("Junctionlar veya yollar yüklenmemiş!");
            return;
        }

        Node<Way> current = ways.head;

        while(current != null) {
            Long[] nodeIds = current.value.getNodes();
            for (int i = 0; i < nodeIds.length - 1; i++) {
                Location loc1 = findLocationById(this.junctions, nodeIds[i]);
                Location loc2 = findLocationById(this.junctions, nodeIds[i + 1]);

                if (loc1 != null && loc2 != null) {
                    addEdge(loc1, loc2);
                }
            }

            current = current.next;
        }
    }

    private Location findLocationById(Linkedlist<Location> junctions, Long id) {
        Node<Location> current = junctions.head;

        while(current != null) {
            if (Objects.equals(current.value.getId(), id)) {
                return current.value;
            }

            current = current.next;
        }
        
        return null;
    }
}
