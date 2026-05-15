package org.example.entities;

import java.time.LocalDateTime;

public class Meeting {
    private final int id;
    private final int courseId;
    private final String courseName;
    private final String topic;
    private final String startTime;
    private final int duration;
    private final String joinUrl;
    private final String startUrl;
    private final String meetingId;
    private final String password;
    private final boolean createdByTeacher;
    private final LocalDateTime createdAt;

    public Meeting(int id, int courseId, String courseName, String topic, String startTime, int duration,
                   String joinUrl, String startUrl, String meetingId, String password,
                   boolean createdByTeacher, LocalDateTime createdAt) {
        this.id = id;
        this.courseId = courseId;
        this.courseName = courseName;
        this.topic = topic;
        this.startTime = startTime;
        this.duration = duration;
        this.joinUrl = joinUrl;
        this.startUrl = startUrl;
        this.meetingId = meetingId;
        this.password = password;
        this.createdByTeacher = createdByTeacher;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getTopic() {
        return topic;
    }

    public String getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }

    public String getJoinUrl() {
        return joinUrl;
    }

    public String getStartUrl() {
        return startUrl;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public String getPassword() {
        return password;
    }

    public boolean isCreatedByTeacher() {
        return createdByTeacher;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
