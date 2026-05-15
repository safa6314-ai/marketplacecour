package org.example.services;

import org.example.entities.StudentProgress;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Collections;

public class ProgressService {
    private final Map<String, Map<String, StudentProgress>> progressByEmail = new LinkedHashMap<>();

    public void updateProgressByStudentEmail(String email, String chapterKey, String status) {
        String normalizedEmail = normalizeEmail(email);
        Map<String, StudentProgress> progressions = progressByEmail.computeIfAbsent(normalizedEmail, key -> new LinkedHashMap<>());
        StudentProgress existing = progressions.get(chapterKey);
        if (existing == null) {
            progressions.put(chapterKey, new StudentProgress(normalizedEmail, chapterKey, status));
        } else {
            existing.setStatus(status);
        }
    }

    public void updateProgressByStudentEmail(String email, int chapterId, String status) {
        updateProgressByStudentEmail(email, String.valueOf(chapterId), status);
    }

    public List<StudentProgress> getProgressByStudentEmail(String email) {
        return progressByEmail.getOrDefault(normalizeEmail(email), Collections.emptyMap())
                .values()
                .stream()
                .toList();
    }

    public double calculateProgressPercentByEmail(String email) {
        List<StudentProgress> progressions = getProgressByStudentEmail(email);
        if (progressions.isEmpty()) {
            return 0;
        }
        long completed = progressions.stream()
                .filter(progress -> "Termine".equalsIgnoreCase(progress.getStatus()))
                .count();
        return (double) completed / progressions.size() * 100;
    }

    public void clear() {
        progressByEmail.clear();
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
