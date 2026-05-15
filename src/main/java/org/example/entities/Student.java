package org.example.entities;

import java.time.LocalDate;

public class Student {
    private final int id;
    private String fullName;
    private String email;
    private String gender;
    private String course;
    private String attendanceStatus;
    private final LocalDate enrollmentDate;

    public Student(int id, String fullName, String email, String gender, String course,
                   String attendanceStatus, LocalDate enrollmentDate) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.gender = gender;
        this.course = course;
        this.attendanceStatus = attendanceStatus;
        this.enrollmentDate = enrollmentDate;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }
}
