package com.project.repository;

import org.springframework.stereotype.Repository;
import com.project.datastructures.Linkedlist;
import com.project.datastructures.Node;
import com.project.dto.request.RestaurantRequestDto;
import com.project.datastructures.Graph;
import com.project.entity.Location;

@Repository
public class GraphRepository {
    private Graph graph = null;

    public Location[] fetch_restaurants(RestaurantRequestDto dto) {
        graph = Graph.getInstance(dto.getRadius(), dto.getLatitude(), dto.getLongitude());
        Linkedlist<Location> restaurants = graph.getRestaurants();
        Location[] restaurant_locations = new Location[restaurants.length()];
        Node<Location> current = restaurants.head;

        for(int i = 0; i<restaurants.length(); ++i) {
            restaurant_locations[i] = current.value;

            current = current.next;
        }
        
        return restaurant_locations;
    }
}
