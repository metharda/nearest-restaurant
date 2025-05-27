package com.project.controller;

import com.project.dto.request.PathRequestDto;
import com.project.dto.request.RestaurantRequestDto;
import com.project.dto.response.RestaurantResponseDto;
import com.project.service.GraphService;
import com.project.service.QueueClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import com.project.entity.Location;
import com.project.datastructures.Graph;
import com.project.datastructures.HashMap;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class PathController {
    private final GraphService graphService;
    private final QueueClientService queueClientService;

    @GetMapping("/getRestaurants")
    public ResponseEntity<?> getNearestRestaurants(HttpEntity<String> request) {
        RestaurantResponseDto dto_resp;
        try{
            HttpHeaders headers = request.getHeaders();
            String radius = headers.get("Radius").getFirst();
            String latitude = headers.get("Latitude").getFirst();
            String longitude = headers.get("Longitude").getFirst();

            RestaurantRequestDto dto_req = new RestaurantRequestDto(radius, latitude, longitude);
            dto_resp = graphService.getRestaurants(dto_req);
            
            // Queue the restaurant request
            queueClientService.enqueueMessage(dto_req);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error");
        }
        if(dto_resp != null){
            return ResponseEntity.ok(dto_resp);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Null Response");
    }

    @GetMapping("/getPath")
    public ResponseEntity<?> getPath(HttpEntity<String> request){
        try{
            HttpHeaders headers = request.getHeaders();
            String restaurantID = headers.get("Restaurant-Id").getFirst();
            String user_latitude = headers.get("User-Latitude").getFirst();
            String user_longitude = headers.get("User-Longitude").getFirst();

            Location user_location = new Location(1L, "User", Double.parseDouble(user_latitude), Double.parseDouble(user_longitude));
            Graph graph = Graph.getInstance();
            graph.implement_user_location_to_graph(user_location);
            
            // Generate a unique requestId for this path request
            String requestId = java.util.UUID.randomUUID().toString();
            
            // Create the path request DTO
            PathRequestDto pathRequestDto = new PathRequestDto(requestId, restaurantID, user_latitude, user_longitude);
            
            // Send to queue service
            queueClientService.enqueueMessage(pathRequestDto);
            
            // Start polling for results
            queueClientService.startPollingQueue(requestId);
            
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("requestId", requestId);
            responseMap.put("message", "Path request queued successfully. Results will be sent via WebSocket.");
            
            return ResponseEntity.ok(responseMap);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: " + e.getMessage());
        }
    }
}
