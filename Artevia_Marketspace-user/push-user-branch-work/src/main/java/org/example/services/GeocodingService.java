package org.example.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GeocodingService {

    public static double[] getCoordinates(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }

        try {
            String encodedAddress = URLEncoder.encode(address.trim(), StandardCharsets.UTF_8);
            String urlStr = "https://nominatim.openstreetmap.org/search?q=" + encodedAddress + "&format=json&limit=1";

            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "ArteviaEventApp/1.0");

            if (conn.getResponseCode() == 200) {
                String response = readResponse(conn);
                JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();
                if (!jsonArray.isEmpty()) {
                    JsonObject obj = jsonArray.get(0).getAsJsonObject();
                    double lat = obj.get("lat").getAsDouble();
                    double lon = obj.get("lon").getAsDouble();
                    return new double[]{lat, lon};
                }
            }
        } catch (Exception e) {
            System.err.println("[EVENT] Geocoding indisponible : " + e.getMessage());
        }

        return null;
    }

    public GeoPoint geocode(String address) {
        double[] coordinates = getCoordinates(address);
        if (coordinates == null) {
            return GeoPoint.mock("Adresse non trouvee.");
        }
        return new GeoPoint(coordinates[0], coordinates[1], "Coordonnees recuperees.");
    }

    private static String readResponse(HttpURLConnection conn) throws Exception {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }

    public record GeoPoint(double latitude, double longitude, String message) {
        public static GeoPoint mock(String message) {
            return new GeoPoint(0, 0, message);
        }
    }
}
