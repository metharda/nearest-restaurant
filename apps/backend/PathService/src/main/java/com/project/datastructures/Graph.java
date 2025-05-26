package com.project.datastructures;

import java.util.Objects;
import com.project.entity.Location;
import com.project.entity.Way;
import com.project.utils.Overpass;
import lombok.Data;

@Data
public class Graph {
    private final HashMap<Location, Linkedlist<Location>> map = new HashMap<>();
    private Linkedlist<Location> nodes;
    private Linkedlist<Way> ways;
    private Linkedlist<Location> restaurants;
    private Location user_location;

    private static Graph instance = null;

    @SuppressWarnings("unchecked")
    private Graph(String radius, String lat, String lon) {
        try {
            Object[] lists = Overpass.fetchWaysWithNodes(radius, lat, lon);

            this.ways = (Linkedlist<Way>) lists[0];
            this.nodes = (Linkedlist<Location>) lists[1];
            this.restaurants = Overpass.fetchRestaurants(radius, lat, lon);

            buildGraph();
            //System.out.println(toDotFormat());
        }
        catch (Exception e) {
            this.nodes = null;
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

    public void removeAllEdges(Location loc1) {
        if(!map.containsKey(loc1)) {
            map.remove(loc1);
        }
    }

    private void buildGraph() {
        if (this.nodes == null || this.ways == null) {
            System.out.println("Junctionlar veya yollar yüklenmemiş!");
            return;
        }

        Node<Way> current1 = ways.head;

        while(current1 != null) {
            Long[] nodeIds = current1.value.getNodes();
            for (int i = 0; i < nodeIds.length - 1; i++) {
                Location loc1 = findLocationById(this.nodes, nodeIds[i]);
                Location loc2 = findLocationById(this.nodes, nodeIds[i + 1]);

                if (loc1 != null && loc2 != null) {
                    addEdge(loc1, loc2);
                }
            }

            current1 = current1.next;
        }

        Node<Location> current2 = restaurants.head;
        Location closest_temp = null;

        while(current2 != null) {
            closest_temp = findClosestJunction(current2.value, nodes);

            if(closest_temp != null) {
                addEdge(current2.value, closest_temp);
            }

            closest_temp = null;
            current2 = current2.next;
        }  
    }

    public String toDotFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("graph G {\n"); 

        for (HashMap.Entry<Location, Linkedlist<Location>> entry : map.entrySet()) {
            Location from = entry.getKey();
            Node<Location> current = entry.getValue().head;

            while (current != null) {
                Location to = current.value;

                sb.append("  \"" + from.getId() + "\" -- \"" + to.getId() + "\";\n");

                current = current.next;
            }
        }

        sb.append("}");
        return sb.toString();
    }

    public Location findClosestJunction(Location restaurant, Linkedlist<Location> nodes) {
        double minDistance = Double.MAX_VALUE;
        Location closest = null;

        Node<Location> current = nodes.head;

        if(restaurant != null) {
            while (current != null) {
                double distance = simpleDistance(
                    restaurant.getLatitude(), restaurant.getLongitude(),
                    current.value.getLatitude(), current.value.getLongitude()
                );

                if (distance < minDistance) {
                    minDistance = distance;
                    closest = current.value;
                }

                current = current.next;
            }
        }

        return closest;
    }

    public void implement_user_location_to_graph(Location user_location) {
        removeAllEdges(user_location);
        setUser_location(user_location);
        Location closest = findClosestJunction(user_location, nodes);
        addEdge(user_location, closest);
    }

    private Location findLocationById(Linkedlist<Location> nodes, Long id) {
        Node<Location> current = nodes.head;

        while(current != null) {
            if (Objects.equals(current.value.getId(), id)) {
                return current.value;
            }

            current = current.next;
        }
        
        return null;
    }

    public static double simpleDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.abs(lat1 - lat2);
        double dLon = Math.abs(lon1 - lon2);
        return dLat + dLon; 
    }
}
