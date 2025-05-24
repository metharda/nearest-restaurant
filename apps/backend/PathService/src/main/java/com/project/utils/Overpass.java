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

    public static Linkedlist<Way> fetchWays(String radius, String lat, String lon) throws Exception {
        String overpassUrl = "https://overpass-api.de/api/interpreter";
        String query = String.format(
    "[out:json];way(around:%s,%s,%s)[\"highway\"];out body;>;out skel qt;",
            radius, lat, lon
        );
        String jsonString = null;
        Linkedlist<Way> ways = new Linkedlist<>();

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
            JsonNode nodesNode = element.get("nodes");
            Long[] nodes = new Long[1];

            if(nodesNode != null) {
                nodes = new Long[nodesNode.size()];
                for(int i = 0; i<nodesNode.size() ; ++i) {
                    nodes[i] = nodesNode.get(i).asLong();
                }
            }

            ways.add(new Way(id, nodes));
        }

        return ways;
    }

    public static Linkedlist<Location> fetchJunctions(String radius, String lat, String lon) throws Exception {
        String overpassUrl = "https://overpass-api.de/api/interpreter";
        String query = String.format(
            "[out:json];node(around:%s,%s,%s)[\"highway\"];out body;",
            radius, lat, lon
        );

        String jsonString;
        Linkedlist<Location> junctions = new Linkedlist<>();

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
            JsonNode nameNode = element.get("tags").get("name");
            String name = "Junction";

            if (nameNode != null) {
                name = nameNode.asText();
            }

            double latNode = element.get("lat").asDouble();
            double lonNode = element.get("lon").asDouble();

            junctions.add(new Location(id, name, latNode, lonNode));
        }

        return junctions;
    }

    public static Linkedlist<Location> fetchRestaurants(String radius, String lat, String lon) throws Exception {
        String overpassUrl = "https://overpass-api.de/api/interpreter";
        String query = String.format(
            "[out:json];node(around:%s,%s,%s)[\"amenity\"=\"restaurant\"];out body;",
            radius, lat, lon
        );

        System.out.println(query);

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
