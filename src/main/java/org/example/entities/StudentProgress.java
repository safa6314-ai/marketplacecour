package org.example.entities;

import java.time.LocalDateTime;

public class StudentProgress {
    private final String studentEmail;
    private final String chapterKey;
    private String status;
    private LocalDateTime updatedAt;

    public StudentProgress(String studentEmail, String chapterKey, String status) {
        this.studentEmail = studentEmail;
        this.chapterKey = chapterKey;
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getChapterKey() {
        return chapterKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
