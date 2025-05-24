package com.project.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.project.dto.request.RestaurantRequestDto;
import com.project.dto.response.RestaurantResponseDto;
import com.project.repository.GraphRepository;

@Service
@RequiredArgsConstructor
public class GraphService {
    private final GraphRepository graphRepository;

    public RestaurantResponseDto getRestaurants(RestaurantRequestDto dto) {
        return new RestaurantResponseDto(graphRepository.fetch_restaurants(dto));
    }
}
