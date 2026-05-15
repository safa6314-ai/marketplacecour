package org.example.entities;

import java.time.LocalDateTime;

public class CourseSummary {
    private final int id;
    private final int studentId;
    private final String studentName;
    private final String studentEmail;
    private final int courseId;
    private final String courseName;
    private final String fileUrl;
    private final String originalFileName;
    private final LocalDateTime sentAt;
    private String status;

    public CourseSummary(int id, int studentId, String studentName, String studentEmail, int courseId,
                         String courseName, String fileUrl, String originalFileName,
                         LocalDateTime sentAt, String status) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.courseId = courseId;
        this.courseName = courseName;
        this.fileUrl = fileUrl;
        this.originalFileName = originalFileName;
        this.sentAt = sentAt;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getOriginalFileName() {
        return originalFileName;
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
