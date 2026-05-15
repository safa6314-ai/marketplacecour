package org.example.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WeatherService {

    public static class WeatherData {
        public final String emoji;
        public final String condition;
        public final double tempMin;
        public final double tempMax;
        public final int precipProb;

        public WeatherData(String emoji, String condition, double tempMin, double tempMax, int precipProb) {
            this.emoji = emoji;
            this.condition = condition;
            this.tempMin = tempMin;
            this.tempMax = tempMax;
            this.precipProb = precipProb;
        }

        public String toDisplayText() {
            return emoji + " " + condition + " | " + Math.round(tempMin) + "-" + Math.round(tempMax) + " C | pluie " + precipProb + "%";
        }
    }

    public static WeatherData getWeatherForecast(double lat, double lon, LocalDate date) {
        if (date == null) {
            return null;
        }

        try {
            String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String urlStr = String.format(java.util.Locale.US,
                    "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&daily=weathercode,temperature_2m_max,temperature_2m_min,precipitation_probability_max&timezone=auto&start_date=%s&end_date=%s",
                    lat, lon, dateStr, dateStr);

            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                String response = readResponse(conn);
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                JsonObject daily = json.getAsJsonObject("daily");

                int code = daily.getAsJsonArray("weathercode").get(0).getAsInt();
                double tMax = daily.getAsJsonArray("temperature_2m_max").get(0).getAsDouble();
                double tMin = daily.getAsJsonArray("temperature_2m_min").get(0).getAsDouble();
                int precip = daily.getAsJsonArray("precipitation_probability_max").get(0).getAsInt();

                String[] result = mapWeatherCode(code);
                return new WeatherData(result[0], result[1], tMin, tMax, precip);
            }
        } catch (Exception e) {
            System.err.println("[EVENT] Meteo indisponible : " + e.getMessage());
        }

        return null;
    }

    public String getWeatherSummary(String city) {
        double[] coordinates = GeocodingService.getCoordinates(city);
        if (coordinates == null) {
            return "Meteo indisponible.";
        }

        WeatherData data = getWeatherForecast(coordinates[0], coordinates[1], LocalDate.now());
        return data == null ? "Meteo indisponible." : data.toDisplayText();
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

    private static String[] mapWeatherCode(int code) {
        if (code == 0) return new String[]{"Soleil", "Ciel degage"};
        if (code <= 3) return new String[]{"Nuages", "Partiellement nuageux"};
        if (code <= 48) return new String[]{"Brouillard", "Nuageux / Brouillard"};
        if (code <= 67) return new String[]{"Pluie", "Pluie"};
        if (code <= 77) return new String[]{"Neige", "Neige"};
        if (code <= 82) return new String[]{"Averses", "Averses de pluie"};
        if (code <= 86) return new String[]{"Neige", "Averses de neige"};
        if (code >= 95) return new String[]{"Orage", "Orages"};
        return new String[]{"Meteo", "Inconnu"};
    }
}
