package com.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PathRequestDto {
    String requestId;
    String restaurant_id;
    String user_latitude;
    String user_longitude;
}
