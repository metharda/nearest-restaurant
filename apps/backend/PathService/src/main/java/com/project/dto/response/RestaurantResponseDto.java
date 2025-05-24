package com.project.dto.response;

import com.project.entity.Location;
import com.project.datastructures.Linkedlist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantResponseDto {
    Linkedlist<Location> restaurants;
}
