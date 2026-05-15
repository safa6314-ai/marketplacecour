package org.example.services;

import org.example.entities.CourseSummary;
import org.example.entities.Meeting;
import org.example.entities.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class StudentService {
    private final AtomicInteger summaryIdSequence = new AtomicInteger(1);
    private final AtomicInteger meetingIdSequence = new AtomicInteger(1);
    private final List<Student> students = new ArrayList<>();
    private final List<CourseSummary> summaries = new ArrayList<>();
    private final List<Meeting> meetings = new ArrayList<>();

    public void setStudents(List<Student> students) {
        this.students.clear();
        this.students.addAll(students);
    }

    public Student findStudentByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return students.stream()
                .filter(student -> normalizeEmail(student.getEmail()).equals(normalizedEmail))
                .findFirst()
                .orElse(null);
    }

    public int nextSummaryId() {
        return summaryIdSequence.getAndIncrement();
    }

    public int nextMeetingId() {
        return meetingIdSequence.getAndIncrement();
    }

    public void addSummary(CourseSummary summary) {
        summaries.add(summary);
    }

    public List<CourseSummary> summariesForStudent(int studentId) {
        return summaries.stream()
                .filter(summary -> summary.getStudentId() == studentId)
                .toList();
    }

    public List<CourseSummary> summariesForStudentEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return summaries.stream()
                .filter(summary -> normalizeEmail(summary.getStudentEmail()).equals(normalizedEmail))
                .toList();
    }

    public List<CourseSummary> summariesForCourse(int courseId) {
        return summaries.stream()
                .filter(summary -> summary.getCourseId() == courseId)
                .toList();
    }

    public List<CourseSummary> allSummaries() {
        return List.copyOf(summaries);
    }

    public void addMeeting(Meeting meeting) {
        meetings.add(meeting);
    }

    public List<Meeting> meetingsForCourse(int courseId) {
        return meetings.stream()
                .filter(meeting -> meeting.getCourseId() == courseId)
                .toList();
    }

    public List<Meeting> allMeetings() {
        return List.copyOf(meetings);
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
