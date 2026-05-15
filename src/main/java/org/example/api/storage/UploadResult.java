package org.example.api.storage;

public record UploadResult(
        String publicId,
        String secureUrl
) {
}
