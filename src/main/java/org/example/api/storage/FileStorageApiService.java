package org.example.api.storage;

import org.example.api.ApiConfig;
import org.example.api.ApiException;
import org.example.api.JsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileStorageApiService {

    private final HttpClient httpClient;
    private final String cloudName;
    private final String uploadPreset;

    public FileStorageApiService() {
        this(HttpClient.newHttpClient(),
                ApiConfig.requiredEnv("CLOUDINARY_CLOUD_NAME"),
                ApiConfig.requiredEnv("CLOUDINARY_UPLOAD_PRESET"));
    }

    public FileStorageApiService(HttpClient httpClient, String cloudName, String uploadPreset) {
        this.httpClient = httpClient;
        this.cloudName = cloudName;
        this.uploadPreset = uploadPreset;
    }

    public UploadResult uploadCourseFile(Path filePath) {
        return uploadFile(filePath);
    }

    public UploadResult uploadStudentSummary(Path filePath) {
        String filename = filePath.getFileName().toString().toLowerCase();
        boolean allowed = filename.endsWith(".pdf")
                || filename.endsWith(".docx")
                || filename.endsWith(".doc")
                || filename.endsWith(".png")
                || filename.endsWith(".jpg")
                || filename.endsWith(".jpeg")
                || filename.endsWith(".webp");
        if (!allowed) {
            throw new ApiException("Format resume non accepte. Utilisez PDF, DOCX, DOC, PNG, JPG, JPEG ou WEBP.");
        }
        return uploadFile(filePath);
    }

    private UploadResult uploadFile(Path filePath) {
        if (!Files.exists(filePath)) {
            throw new ApiException("Fichier introuvable : " + filePath);
        }

        String boundary = "----CourseBoundary" + UUID.randomUUID();
        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/auto/upload";

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(ofMultipartData(filePath, boundary))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException("Erreur Cloudinary HTTP " + response.statusCode() + " : " + response.body());
            }

            return new UploadResult(
                    JsonUtils.extractString(response.body(), "public_id"),
                    JsonUtils.extractString(response.body(), "secure_url")
            );
        } catch (IOException e) {
            throw new ApiException("Erreur pendant l'upload du fichier : " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Upload interrompu.", e);
        }
    }

    private HttpRequest.BodyPublisher ofMultipartData(Path filePath, String boundary) throws IOException {
        List<byte[]> byteArrays = new ArrayList<>();
        String filename = filePath.getFileName().toString();
        String mimeType = Files.probeContentType(filePath);
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = "application/octet-stream";
        }

        addTextPart(byteArrays, boundary, "upload_preset", uploadPreset);
        addFilePart(byteArrays, boundary, "file", filename, mimeType, Files.readAllBytes(filePath));
        byteArrays.add(("--" + boundary + "--\r\n").getBytes());

        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    private static void addTextPart(List<byte[]> byteArrays, String boundary, String name, String value) {
        byteArrays.add(("--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n"
                + value + "\r\n").getBytes());
    }

    private static void addFilePart(List<byte[]> byteArrays, String boundary, String name, String filename,
                                    String mimeType, byte[] content) {
        byteArrays.add(("--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n"
                + "Content-Type: " + mimeType + "\r\n\r\n").getBytes());
        byteArrays.add(content);
        byteArrays.add("\r\n".getBytes());
    }
}
