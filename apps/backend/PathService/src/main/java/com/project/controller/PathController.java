package com.project.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.dto.request.RestaurantRequestDto;
import com.project.dto.response.RestaurantResponseDto;
import com.project.service.GraphService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class PathController {
    private final GraphService graphService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/getRestaurants")
    public ResponseEntity<?> getNearestRestaurants(HttpEntity<String> request) {
        RestaurantResponseDto dto_resp;
        try{
            String body = request.getBody();
            JsonNode jsonNode = objectMapper.readTree(body);
            String radius = jsonNode.get("radius").asText();
            String latitude = jsonNode.get("latitude").asText();
            String longitude = jsonNode.get("longitude").asText();

            RestaurantRequestDto dto_req = new RestaurantRequestDto(radius, latitude, longitude);
            dto_resp = graphService.getRestaurants(dto_req);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error");
        }
        if(dto_resp != null){
            return ResponseEntity.ok(dto_resp);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Null Response");
    }
}
