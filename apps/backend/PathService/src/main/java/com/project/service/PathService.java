/*package com.project.service;

import com.project.dto.Location;
import com.project.dto.response.RestaurantResponseDto;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PathService {

    private final OsrmService osrmService;

    public PathService(OsrmService osrmService) {
        this.osrmService = osrmService;
    }

    private static final List<Location> restaurantList = List.of(
            new Location("Kebapçı Mehmet", 41.015137, 28.979530),
            new Location("BurgerLand", 41.017123, 28.975650),
            new Location("Sushi Time", 41.019000, 28.971000),
            new Location("Tostçu", 41.010000, 28.980000),
            new Location("Mantı House", 41.021000, 28.970000)
    );

    public List<RestaurantResponseDto> findNearestByRoad(Location userLocation) {
        List<RestaurantResponseDto> results = new ArrayList<>();

        for (Location restaurant : restaurantList) {
            double distance = osrmService.getRouteDistance(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    restaurant.getLatitude(), restaurant.getLongitude()
            );
            results.add(new RestaurantResponseDto(restaurant.getName(), restaurant.getLatitude(), restaurant.getLongitude(), distance));
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(RestaurantResponseDto::getDistance))
                .limit(10)
                .toList();
    }
}*/
