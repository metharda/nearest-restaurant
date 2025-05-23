
package com.project.controller;

import com.project.dto.Location;
import com.project.dto.RestaurantResponse;
import com.project.service.PathService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/path")

public class PathController {

    private final PathService pathService;

    public PathController(PathService pathService) {
        this.pathService = pathService;
    }

    @PostMapping("/nearest")
    public List<RestaurantResponse> getNearestRestaurants(@RequestBody Location userLocation) {
        return pathService.findNearestByRoad(userLocation);
    }
}
