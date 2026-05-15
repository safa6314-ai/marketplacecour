package org.example.entities;

import java.time.LocalDateTime;

public class StudentGrade {
    private final String studentEmail;
    private final String chapterKey;
    private double grade;
    private String teacherComment;
    private LocalDateTime updatedAt;

    public StudentGrade(String studentEmail, String chapterKey, double grade, String teacherComment) {
        this.studentEmail = studentEmail;
        this.chapterKey = chapterKey;
        this.grade = grade;
        this.teacherComment = teacherComment;
        this.updatedAt = LocalDateTime.now();
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getChapterKey() {
        return chapterKey;
    }

    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        this.grade = grade;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTeacherComment() {
        return teacherComment;
    }

    public void setTeacherComment(String teacherComment) {
        this.teacherComment = teacherComment;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
