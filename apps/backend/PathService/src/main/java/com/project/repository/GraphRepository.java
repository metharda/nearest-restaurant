package com.project.repository;

import org.springframework.stereotype.Repository;
import com.project.datastructures.Linkedlist;
import com.project.dto.request.RestaurantRequestDto;
import com.project.datastructures.Graph;
import com.project.entity.Location;

@Repository
public class GraphRepository {
    private Graph graph = null;

    public Linkedlist<Location> fetch_restaurants(RestaurantRequestDto dto) {
        graph = Graph.getInstance(dto.getRadius(), dto.getLatitude(), dto.getLongitude());

        return graph.getRestaurants();
    }
}
