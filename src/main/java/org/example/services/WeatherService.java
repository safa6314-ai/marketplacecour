package org.example.services;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WeatherService {

    public static class WeatherData {
        public String emoji;
        public String condition;
        public double tempMin;
        public double tempMax;
        public int precipProb;

        public WeatherData(String emoji, String condition, double tempMin, double tempMax, int precipProb) {
            this.emoji = emoji;
            this.condition = condition;
            this.tempMin = tempMin;
            this.tempMax = tempMax;
            this.precipProb = precipProb;
        }
    }

    public static WeatherData getWeatherForecast(double lat, double lon, LocalDate date) {
        try {
            String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String urlStr = String.format(java.util.Locale.US, 
                "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&daily=weathercode,temperature_2m_max,temperature_2m_min,precipitation_probability_max&timezone=auto&start_date=%s&end_date=%s",
                lat, lon, dateStr, dateStr);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject json = new JSONObject(response.toString());
                JSONObject daily = json.getJSONObject("daily");
                
                int code = daily.getJSONArray("weathercode").getInt(0);
                double tMax = daily.getJSONArray("temperature_2m_max").getDouble(0);
                double tMin = daily.getJSONArray("temperature_2m_min").getDouble(0);
                int precip = daily.getJSONArray("precipitation_probability_max").getInt(0);

                String[] result = mapWeatherCode(code);
                return new WeatherData(result[0], result[1], tMin, tMax, precip);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String[] mapWeatherCode(int code) {
        if (code == 0) return new String[]{"☀️", "Ciel dégagé"};
        if (code <= 3) return new String[]{"🌤️", "Partiellement nuageux"};
        if (code <= 48) return new String[]{"☁️", "Nuageux / Brouillard"};
        if (code <= 67) return new String[]{"🌧️", "Pluie"};
        if (code <= 77) return new String[]{"❄️", "Neige"};
        if (code <= 82) return new String[]{"🌧️", "Averses de pluie"};
        if (code <= 86) return new String[]{"❄️", "Averses de neige"};
        if (code >= 95) return new String[]{"⛈️", "Orages"};
        return new String[]{"❓", "Inconnu"};
    }
}
