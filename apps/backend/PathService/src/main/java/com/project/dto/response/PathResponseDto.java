package com.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PathResponseDto {
    private String requestId;
    private String restaurantId;
    private List<PathPointDto> path;
    private String status;
    private Double distance;
    private Integer duration;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class PathPointDto {
    private Double latitude;
    private Double longitude;
}
