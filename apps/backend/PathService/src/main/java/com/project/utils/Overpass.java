package com.project.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.datastructures.Linkedlist;
import com.project.entity.Location;
import com.project.entity.Way;

public class Overpass {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static Object[] fetchWaysWithNodes(String radius, String lat, String lon) throws Exception {
        String overpassUrl = "https://overpass-api.de/api/interpreter";
        String query = String.format(
            "[out:json];way(around:%s,%s,%s)[\"highway\"];(._;>;);out body;",
            radius, lat, lon
        );

        String jsonString;
        Linkedlist<Way> ways = new Linkedlist<>();
        Linkedlist<Location> nodes = new Linkedlist<>();
        Object[] result = new Object[2];

        result[0] = ways;
        result[1] = nodes;

        @SuppressWarnings("deprecation")
        URL url = new URL(overpassUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = query.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
            jsonString = content.toString();
        } finally {
            con.disconnect();
        }

        JsonNode root = mapper.readTree(jsonString);
        JsonNode elements = root.get("elements");

        for (JsonNode element : elements) {
            String type = element.get("type").asText();

            if (type.equals("way")) {
                Long id = element.get("id").asLong();
                JsonNode nodesNode = element.get("nodes");
                Long[] nodeIds = new Long[nodesNode.size()];

                for (int i = 0; i < nodesNode.size(); i++) {
                    nodeIds[i] = nodesNode.get(i).asLong();
                }

                ways.add(new Way(id, nodeIds));
            }

            if (type.equals("node")) {
                Long id = element.get("id").asLong();
                double latNode = element.get("lat").asDouble();
                double lonNode = element.get("lon").asDouble();

                JsonNode nameNode = element.get("tags") != null ? element.get("tags").get("name") : null;
                String name = nameNode != null ? nameNode.asText() : "Node";

                nodes.add(new Location(id, name, latNode, lonNode));
            }
        }

        return result;
    }

    public static Linkedlist<Location> fetchRestaurants(String radius, String lat, String lon) throws Exception {
        String overpassUrl = "https://overpass-api.de/api/interpreter";
        String query = String.format(
            "[out:json];node(around:%s,%s,%s)[\"amenity\"=\"restaurant\"];out body;",
            radius, lat, lon
        );

        String jsonString;
        Linkedlist<Location> restaurants = new Linkedlist<>();

        @SuppressWarnings("deprecation")
        URL url = new URL(overpassUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = query.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
            jsonString = content.toString();
        } finally {
            con.disconnect();
        }

        JsonNode root = mapper.readTree(jsonString);
        JsonNode elements = root.get("elements");

        for (JsonNode element : elements) {
            Long id = element.get("id").asLong();
            JsonNode nameNode = element.get("tags") != null ? element.get("tags").get("name") : null;
            String name = "Restaurant";

            if (nameNode != null) {
                name = nameNode.asText();
            }

            double latNode = element.get("lat").asDouble();
            double lonNode = element.get("lon").asDouble();

            restaurants.add(new Location(id, name, latNode, lonNode));
        }

        return restaurants;
    }
}
