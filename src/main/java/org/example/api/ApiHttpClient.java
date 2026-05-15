package org.example.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiHttpClient {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public String send(HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                throw new ApiException("Erreur API HTTP " + status + " : " + response.body());
            }
            return response.body();
        } catch (IOException e) {
            throw new ApiException("Erreur reseau pendant l'appel API : " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Appel API interrompu.", e);
        }
    }

    public String postJson(String url, String body, String... headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        addHeaders(builder, headers);
        return send(builder.build());
    }

    public String postForm(String url, String body, String... headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        addHeaders(builder, headers);
        return send(builder.build());
    }

    private static void addHeaders(HttpRequest.Builder builder, String... headers) {
        if (headers.length % 2 != 0) {
            throw new IllegalArgumentException("Les headers doivent etre fournis par paires nom/valeur.");
        }

        for (int i = 0; i < headers.length; i += 2) {
            builder.header(headers[i], headers[i + 1]);
        }
    }
}
