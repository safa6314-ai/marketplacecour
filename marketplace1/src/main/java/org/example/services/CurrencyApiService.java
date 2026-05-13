package org.example.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurrencyApiService {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public CurrencyResult convertFromTnd(double amountTnd) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://open.er-api.com/v6/latest/TND"))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                double eurRate = extractNumber(response.body(), "EUR");
                double usdRate = extractNumber(response.body(), "USD");
                if (eurRate > 0 && usdRate > 0) {
                    return new CurrencyResult(amountTnd * eurRate, amountTnd * usdRate, false);
                }
            }
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return new CurrencyResult(amountTnd * 0.30, amountTnd * 0.33, true);
    }

    private double extractNumber(String json, String key) {
        String token = "\"" + key + "\":";
        int index = json.indexOf(token);
        if (index < 0) {
            return 0;
        }
        int start = index + token.length();
        int end = start;
        while (end < json.length() && "0123456789.-".indexOf(json.charAt(end)) >= 0) {
            end++;
        }
        try {
            return Double.parseDouble(json.substring(start, end));
        } catch (Exception ex) {
            return 0;
        }
    }

    public static class CurrencyResult {
        private final double eur;
        private final double usd;
        private final boolean fallback;

        public CurrencyResult(double eur, double usd, boolean fallback) {
            this.eur = eur;
            this.usd = usd;
            this.fallback = fallback;
        }

        public double getEur() {
            return eur;
        }

        public double getUsd() {
            return usd;
        }

        public boolean isFallback() {
            return fallback;
        }
    }
}
