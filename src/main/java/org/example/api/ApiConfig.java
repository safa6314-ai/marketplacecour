package org.example.api;

public final class ApiConfig {

    private ApiConfig() {
    }

    public static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new ApiException("Variable d'environnement manquante : " + name);
        }
        return value.trim();
    }

    public static String optionalEnv(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
