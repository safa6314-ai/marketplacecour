package org.example.api.storage;

import org.example.api.ApiConfig;
import org.example.api.ApiException;
import org.example.api.JsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GoogleDriveApiService {

    private final HttpClient httpClient;
    private final String accessToken;
    private final String parentFolderId;

    public GoogleDriveApiService() {
        this(HttpClient.newHttpClient(),
                ApiConfig.requiredEnv("GOOGLE_DRIVE_ACCESS_TOKEN"),
                ApiConfig.optionalEnv("GOOGLE_DRIVE_FOLDER_ID", ""));
    }

    public GoogleDriveApiService(HttpClient httpClient, String accessToken, String parentFolderId) {
        this.httpClient = httpClient;
        this.accessToken = accessToken;
        this.parentFolderId = parentFolderId;
    }

    public UploadResult uploadFile(Path filePath) {
        if (!Files.exists(filePath)) {
            throw new ApiException("Fichier introuvable : " + filePath);
        }

        String boundary = "----DriveBoundary" + UUID.randomUUID();
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(
                            "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,webViewLink"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "multipart/related; boundary=" + boundary)
                    .POST(ofMultipartData(filePath, boundary))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException("Erreur Google Drive HTTP " + response.statusCode() + " : " + response.body());
            }

            return new UploadResult(
                    JsonUtils.extractString(response.body(), "id"),
                    JsonUtils.extractString(response.body(), "webViewLink")
            );
        } catch (IOException e) {
            throw new ApiException("Erreur pendant l'upload Google Drive : " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Upload Google Drive interrompu.", e);
        }
    }

    private HttpRequest.BodyPublisher ofMultipartData(Path filePath, String boundary) throws IOException {
        String filename = filePath.getFileName().toString();
        String mimeType = Files.probeContentType(filePath);
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = "application/octet-stream";
        }

        String metadata = parentFolderId.isBlank()
                ? "{\"name\":" + JsonUtils.quote(filename) + "}"
                : "{\"name\":" + JsonUtils.quote(filename) + ",\"parents\":[" + JsonUtils.quote(parentFolderId) + "]}";

        List<byte[]> parts = new ArrayList<>();
        parts.add(("--" + boundary + "\r\n"
                + "Content-Type: application/json; charset=UTF-8\r\n\r\n"
                + metadata + "\r\n").getBytes(StandardCharsets.UTF_8));
        parts.add(("--" + boundary + "\r\n"
                + "Content-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        parts.add(Files.readAllBytes(filePath));
        parts.add(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        return HttpRequest.BodyPublishers.ofByteArrays(parts);
    }
}
