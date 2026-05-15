package org.example.api.courseai;

public record CourseGenerationRequest(
        String description,
        String language,
        int chapterCount,
        double suggestedPrice
) {

    public CourseGenerationRequest(String description) {
        this(description, "francais", 5, 0.0);
    }

    public CourseGenerationRequest {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description du cours est obligatoire.");
        }
        if (language == null || language.isBlank()) {
            language = "francais";
        }
        if (chapterCount <= 0) {
            chapterCount = 5;
        }
        if (suggestedPrice < 0) {
            suggestedPrice = 0.0;
        }
    }
}
