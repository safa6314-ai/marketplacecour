package org.example.services;

import org.example.entities.Vente;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class MetMuseumApiService {
    private static final String BASE_URL = "https://collectionapi.metmuseum.org/public/collection/v1";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public Vente importFirstArtwork(String query) throws IOException, InterruptedException {
        String encodedQuery = URLEncoder.encode(query == null || query.isBlank() ? "painting" : query, StandardCharsets.UTF_8);
        String searchJson = get(BASE_URL + "/search?hasImages=true&q=" + encodedQuery);
        int objectId = firstObjectId(searchJson);
        if (objectId <= 0) {
            throw new IOException("Aucune oeuvre MET trouvee pour: " + query);
        }

        String objectJson = get(BASE_URL + "/objects/" + objectId);
        String title = value(objectJson, "title", "Oeuvre MET " + objectId);
        String artist = value(objectJson, "artistDisplayName", "The Metropolitan Museum of Art");
        String category = value(objectJson, "classification", "Met Museum");
        String image = value(objectJson, "primaryImageSmall", "");
        String date = value(objectJson, "objectDate", "");
        double price = calculatePrice(objectId, title, artist, category, date);
        String description = "Import MET Museum API: " + (date.isBlank() ? "oeuvre referencee" : date) + ".";
        return new Vente(title, description, price, category, artist, 1, image.isBlank() ? null : image);
    }

    private String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Erreur API MET HTTP " + response.statusCode());
        }
        return response.body();
    }

    private int firstObjectId(String json) {
        String token = "\"objectIDs\":[";
        int index = json.indexOf(token);
        if (index < 0) {
            return -1;
        }
        int start = index + token.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (Exception ex) {
            return -1;
        }
    }

    private String value(String json, String key, String fallback) {
        String token = "\"" + key + "\":";
        int index = json.indexOf(token);
        if (index < 0) {
            return fallback;
        }
        int startQuote = json.indexOf('"', index + token.length());
        if (startQuote < 0) {
            return fallback;
        }
        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int i = startQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                value.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                String text = value.toString().trim();
                return text.isEmpty() ? fallback : text;
            } else {
                value.append(c);
            }
        }
        return fallback;
    }

    private double calculatePrice(int objectId, String title, String artist, String category, String date) {
        int hash = Math.abs((title + artist + category + date + objectId).hashCode());
        double base = 120 + (hash % 480);
        if (contains(category, "Painting") || contains(category, "Paintings")) {
            base += 180;
        } else if (contains(category, "Photograph")) {
            base += 90;
        } else if (contains(category, "Sculpture")) {
            base += 220;
        }
        if (date != null && date.matches(".*\\b1[5-8][0-9]{2}\\b.*")) {
            base += 140;
        }
        return Math.round(base / 10.0) * 10.0;
    }

    private boolean contains(String value, String part) {
        return value != null && value.toLowerCase().contains(part.toLowerCase());
    }
}
