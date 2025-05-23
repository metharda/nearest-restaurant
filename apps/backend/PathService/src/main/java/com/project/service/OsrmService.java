package com.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OsrmService {

    private final RestTemplate restTemplate = new RestTemplate();

    public double getRouteDistance(double fromLat, double fromLon, double toLat, double toLon) {
        String url = String.format("http://router.project-osrm.org/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=false",
                fromLon, fromLat, toLon, toLat);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) restTemplate.getForObject(url, Map.class);
            if (response != null && response.get("routes") instanceof List) {
                List<?> routesRaw = (List<?>) response.get("routes");
                if (!routesRaw.isEmpty() && routesRaw.get(0) instanceof Map) {
                    Map<?, ?> firstRoute = (Map<?, ?>) routesRaw.get(0);
                    Object distanceObj = firstRoute.get("distance");
                    if (distanceObj instanceof Number) {
                        return ((Number) distanceObj).doubleValue();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Double.MAX_VALUE;
    }
}
