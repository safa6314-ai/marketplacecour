package org.example.entities;

import java.time.LocalDateTime;

public class StudentSubmission {
    private final String studentEmail;
    private final String studentName;
    private final String courseName;
    private final String fileName;
    private final String fileUrl;
    private final LocalDateTime sentAt;
    private String status;

    public StudentSubmission(String studentEmail, String studentName, String courseName,
                             String fileName, String fileUrl, LocalDateTime sentAt, String status) {
        this.studentEmail = studentEmail;
        this.studentName = studentName;
        this.courseName = courseName;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.sentAt = sentAt;
        this.status = status;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
